package ron.silly.decayinhwintah;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/** Registers sound events listed by the mod's music and one-shot sound configuration. */
public final class ModSounds {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Decayinhwintah.MODID);
    private static final Set<ResourceLocation> REGISTERED_IDS = new HashSet<>();

    static {
        registerConfiguredEvents();
    }

    private ModSounds() {}

    private static void registerConfiguredEvents() {
        String musicPath = "/assets/" + Decayinhwintah.MODID + "/music_pools.json";
        String effectsPath = "/assets/" + Decayinhwintah.MODID + "/sound_effects.json";
        try {
            try (InputStream stream = ModSounds.class.getResourceAsStream(musicPath)) {
                if (stream != null) {
                    try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                        registerArray(root.getAsJsonArray("menu"));
                        registerArray(root.getAsJsonArray("day"));
                        registerArray(root.getAsJsonArray("night"));
                        if (root.has("scenarios") && root.get("scenarios").isJsonObject()) {
                            for (var entry : root.getAsJsonObject("scenarios").entrySet()) {
                                if (entry.getValue().isJsonArray()) registerArray(entry.getValue().getAsJsonArray());
                            }
                        }
                    }
                } else {
                    LOGGER.warn("Could not find {}; no custom music events registered", musicPath);
                }
            }
            try (InputStream stream = ModSounds.class.getResourceAsStream(effectsPath)) {
                if (stream != null) {
                    try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                        registerArray(root.getAsJsonArray("effects"));
                    }
                } else {
                    LOGGER.warn("Could not find {}; no custom one-shot sound events registered", effectsPath);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Could not register sound events from configuration", exception);
        }
        LOGGER.info("Registered {} custom sound events", REGISTERED_IDS.size());
    }

    private static void registerArray(JsonArray entries) {
        if (entries == null) return;
        entries.forEach(entry -> {
            ResourceLocation id = ResourceLocation.tryParse(entry.getAsString());
            if (id == null || !Decayinhwintah.MODID.equals(id.getNamespace())) {
                LOGGER.warn("Ignoring invalid or foreign sound event ID {}", entry);
                return;
            }
            if (REGISTERED_IDS.add(id)) {
                SOUND_EVENTS.register(id.getPath(), () -> SoundEvent.createVariableRangeEvent(id));
            }
        });
    }
}
