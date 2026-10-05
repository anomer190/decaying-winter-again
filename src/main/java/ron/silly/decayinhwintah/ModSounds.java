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

/** Registers sound events named by music_pools.json before Forge freezes the sound registry. */
public final class ModSounds {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Decayinhwintah.MODID);
    private static final Set<ResourceLocation> REGISTERED_IDS = new HashSet<>();

    static {
        registerPlaylistEvents();
    }

    private ModSounds() {}

    private static void registerPlaylistEvents() {
        String resourcePath = "/assets/" + Decayinhwintah.MODID + "/music_pools.json";
        try (InputStream stream = ModSounds.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                LOGGER.warn("Could not find {}; no custom music events registered", resourcePath);
                return;
            }
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
        } catch (Exception exception) {
            LOGGER.error("Could not register music events from {}", resourcePath, exception);
        }
        LOGGER.info("Registered {} custom music sound events", REGISTERED_IDS.size());
    }

    private static void registerArray(JsonArray entries) {
        if (entries == null) return;
        entries.forEach(entry -> {
            ResourceLocation id = ResourceLocation.tryParse(entry.getAsString());
            if (id == null || !Decayinhwintah.MODID.equals(id.getNamespace())) {
                LOGGER.warn("Ignoring invalid or foreign music event ID {}", entry);
                return;
            }
            if (REGISTERED_IDS.add(id)) {
                SOUND_EVENTS.register(id.getPath(), () -> SoundEvent.createVariableRangeEvent(id));
            }
        });
    }
}

