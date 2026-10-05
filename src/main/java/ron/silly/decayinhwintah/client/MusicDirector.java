package ron.silly.decayinhwintah.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import ron.silly.decayinhwintah.CycleManager;
import ron.silly.decayinhwintah.Decayinhwintah;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Client-side title-screen and cycle music player. */
@Mod.EventBusSubscriber(modid = Decayinhwintah.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MusicDirector {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation PLAYLISTS = new ResourceLocation(Decayinhwintah.MODID, "music_pools.json");
    private static CycleManager.Phase phase;
    private static PhaseMusicSound current;
    private static MenuMusicSound menuSound;
    private static int currentInactiveTicks;
    private static CycleManager.Phase playingPhase;
    private static List<ResourceLocation> menuTracks = List.of();
    private static List<ResourceLocation> dayTracks = List.of();
    private static List<ResourceLocation> nightTracks = List.of();
    private static boolean loaded;
    private static final Set<ResourceLocation> warnedMissingSounds = new HashSet<>();
    private static float cooldownFadeSeconds = 4.0f;

    private MusicDirector() {}

    public static void setPhase(CycleManager.Phase newPhase) {
        if (phase != newPhase) {
            LOGGER.info("Received cycle phase sync: {}", newPhase);
            phase = newPhase;
            if (newPhase == CycleManager.Phase.COOLDOWN && current != null) current.fadeOut(cooldownFadeSeconds);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        loadPlaylists(minecraft);

        boolean menuContext = minecraft.level == null && minecraft.screen != null;
        boolean inWorld = minecraft.level != null && minecraft.player != null;
        if (!menuContext && !inWorld) {
            stopCurrent(minecraft);
            phase = null;
            return;
        }

        if (menuContext) {
            phase = null;
            if (current != null) {
                minecraft.getSoundManager().stop(current);
                current = null;
                playingPhase = null;
            }
            startMenuTrack(minecraft);
            return;
        }

        if (menuSound != null) {
            minecraft.getSoundManager().stop(menuSound);
            menuSound = null;
        }
        if (current != null) {
            if (minecraft.getSoundManager().isActive(current)) currentInactiveTicks = 0;
            else if (current.isStopped() || ++currentInactiveTicks > 2) {
                current = null;
                currentInactiveTicks = 0;
            }
        }
        if (phase == null) {
            phase = CycleManager.Phase.PREPARATION;
            LOGGER.warn("No cycle phase sync received yet; using preparation music until sync arrives");
        }
        if (phase == CycleManager.Phase.COOLDOWN) {
            if (current != null) current.fadeOut(cooldownFadeSeconds);
            return;
        }

        List<ResourceLocation> pool = phase == CycleManager.Phase.NIGHT ? nightTracks : dayTracks;
        if (current != null && playingPhase != phase) current.fadeOut(cooldownFadeSeconds);
        startPhaseTrack(minecraft, pool);
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (event.getSound() == null || event.getSound().getSource() != SoundSource.MUSIC) return;
        Minecraft minecraft = Minecraft.getInstance();
        loadPlaylists(minecraft);
        boolean customMusicAvailable;
        if (minecraft.level == null && minecraft.screen != null) {
            customMusicAvailable = !menuTracks.isEmpty();
        } else if (minecraft.level != null) {
            customMusicAvailable = phase == CycleManager.Phase.NIGHT
                    ? !nightTracks.isEmpty()
                    : phase == CycleManager.Phase.COOLDOWN
                    ? !dayTracks.isEmpty() || !nightTracks.isEmpty()
                    : !dayTracks.isEmpty();
        } else {
            customMusicAvailable = false;
        }
        if (customMusicAvailable && "minecraft".equals(event.getSound().getLocation().getNamespace())) {
            event.setSound(null);
        }
    }

    private static void startMenuTrack(Minecraft minecraft) {
        if (menuSound != null || menuTracks.isEmpty()) return;
        ResourceLocation id = menuTracks.get(RandomSource.create().nextInt(menuTracks.size()));
        SoundEvent event = resolve(id);
        if (event == null) return;
        menuSound = new MenuMusicSound(event);
        minecraft.getSoundManager().play(menuSound);
        LOGGER.info("Started looping menu track {} across menu screens", id);
    }

    private static void startPhaseTrack(Minecraft minecraft, List<ResourceLocation> pool) {
        if (pool.isEmpty() || current != null) return;
        ResourceLocation id = pool.get(minecraft.level.random.nextInt(pool.size()));
        SoundEvent event = resolve(id);
        if (event == null) return;
        current = new PhaseMusicSound(event);
        currentInactiveTicks = 0;
        current.fadeIn(2.0f);
        playingPhase = phase;
        LOGGER.info("Starting custom music track {} for phase {}", id, phase);
        minecraft.getSoundManager().play(current);
    }

    private static SoundEvent resolve(ResourceLocation id) {
        SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(id);
        if (event == null && warnedMissingSounds.add(id)) {
            LOGGER.error("Music pool references unregistered sound event {}", id);
        }
        return event;
    }

    private static void stopCurrent(Minecraft minecraft) {
        if (current != null) minecraft.getSoundManager().stop(current);
        if (menuSound != null) minecraft.getSoundManager().stop(menuSound);
        current = null;
        menuSound = null;
        playingPhase = null;
    }

    private static void loadPlaylists(Minecraft minecraft) {
        if (loaded) return;
        loaded = true;
        try {
            var optional = minecraft.getResourceManager().getResource(PLAYLISTS);
            if (optional.isEmpty()) return;
            try (var reader = new InputStreamReader(optional.get().open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                menuTracks = readPool(root, "menu");
                dayTracks = readPool(root, "day");
                nightTracks = readPool(root, "night");
                if (root.has("cooldown_fade_seconds")) cooldownFadeSeconds = Math.max(0.1f, root.get("cooldown_fade_seconds").getAsFloat());
            }
            LOGGER.info("Loaded music pools: {} menu, {} day, {} night tracks", menuTracks.size(), dayTracks.size(), nightTracks.size());
        } catch (Exception exception) {
            LOGGER.error("Could not load {}", PLAYLISTS, exception);
        }
    }

    private static List<ResourceLocation> readPool(JsonObject root, String name) {
        List<ResourceLocation> tracks = new ArrayList<>();
        JsonArray entries = root.has(name) ? root.getAsJsonArray(name) : new JsonArray();
        for (JsonElement entry : entries) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getAsString());
            if (id != null) tracks.add(id);
            else LOGGER.warn("Ignoring invalid {} music ID: {}", name, entry);
        }
        return List.copyOf(tracks);
    }

    private static final class MenuMusicSound extends AbstractTickableSoundInstance {
        private MenuMusicSound(SoundEvent sound) {
            super(sound, SoundSource.MUSIC, RandomSource.create());
            this.looping = true;
            this.volume = 1.0f;
            this.relative = true;
        }
        @Override public void tick() {}
    }

    private static final class PhaseMusicSound extends AbstractTickableSoundInstance {
        private float volumePerTick;
        private boolean fadingOut;

        private PhaseMusicSound(SoundEvent sound) {
            super(sound, SoundSource.MUSIC, RandomSource.create());
            this.looping = false;
            // A zero-gain sound can be discarded before the tickable fade has a chance to run.
            this.volume = 0.05f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (fadingOut) {
                volume = Math.max(0.0f, volume - volumePerTick);
                if (volume == 0.0f) stop();
            } else if (volumePerTick > 0.0f) {
                volume = Math.min(1.0f, volume + volumePerTick);
                if (volume >= 1.0f) volumePerTick = 0.0f;
            }
        }

        private void fadeIn(float seconds) {
            fadingOut = false;
            volumePerTick = (1.0f - volume) / Math.max(1.0f, seconds * 20.0f);
        }

        private void fadeOut(float seconds) {
            if (fadingOut) return;
            fadingOut = true;
            volumePerTick = Math.max(0.01f, volume) / Math.max(1.0f, seconds * 20.0f);
        }
    }
}

