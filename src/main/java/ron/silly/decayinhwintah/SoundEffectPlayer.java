package ron.silly.decayinhwintah;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.registries.ForgeRegistries;

/** One-shot sound playback, independent of the music director's fade behavior. */
public final class SoundEffectPlayer {
    private SoundEffectPlayer() {}

    /**
     * Plays the registered sound for nearby players at its normal requested volume and pitch.
     * It ends naturally; no fade is applied.
     *
     * @return true if the sound event exists and was submitted for playback
     */
    public static boolean playAt(ServerLevel level, BlockPos position, ResourceLocation soundId,
                                 SoundSource source, float volume, float pitch) {
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
        if (sound == null) return false;
        level.playSound(null, position, sound, source, volume, pitch);
        return true;
    }
}

