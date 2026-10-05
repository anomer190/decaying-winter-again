package ron.silly.decayinhwintah;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ron.silly.decayinhwintah.network.ModNetwork;

@Mod.EventBusSubscriber(modid = Decayinhwintah.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CycleManager {
    private static final int TICKS_PER_SECOND = 20;
    private static final int MIN_PREPARATION_TICKS = 90 * TICKS_PER_SECOND;
    private static final int MAX_PREPARATION_TICKS = 150 * TICKS_PER_SECOND;
    private static final int PHASE_DAYLIGHT_TICKS = 12_000;
    private static final int NIGHT_TICKS = 10 * TICKS_PER_SECOND;
    private static final int COOLDOWN_TICKS = 7 * TICKS_PER_SECOND;

    private static final RandomSource RANDOM = RandomSource.create();
    private static MinecraftServer activeServer;
    private static Phase phase = Phase.PREPARATION;
    private static int ticksRemaining;
    private static int lastAnnouncedPreparationSecond = -1;
    private static double dayTime;
    private static double targetDayTime;

    private CycleManager() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        if (server != activeServer) {
            activeServer = server;
            dayTime = 0.0;
            server.overworld().getGameRules()
                    .getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, server);
            beginPreparation();
            ModNetwork.sendPhaseToAll(phase);
        }

        if (phase == Phase.PREPARATION || phase == Phase.NIGHT) {
            dayTime += (targetDayTime - dayTime) / ticksRemaining;
        }

        if (--ticksRemaining <= 0) {
            advancePhase(server);
        } else if (phase == Phase.PREPARATION) {
            int secondsRemaining = preparationSecondsRemaining();
            if (secondsRemaining != lastAnnouncedPreparationSecond) {
                lastAnnouncedPreparationSecond = secondsRemaining;
                announce(server, "Night begins in " + secondsRemaining + " seconds.");
            }
        }

        long displayedDayTime = (long) dayTime;
        for (ServerLevel level : server.getAllLevels()) {
            level.setDayTime(displayedDayTime);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Send phase even if the first server tick has not initialized activeServer yet.
            ModNetwork.sendPhase(player, phase);
            if (phase == Phase.PREPARATION && ticksRemaining > 0) {
                player.sendSystemMessage(Component.literal(
                        "Night begins in " + preparationSecondsRemaining() + " seconds."));
            }
        }
    }
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (event.getEntity() instanceof Player && "starve".equals(source.getMsgId())) {
            event.setCanceled(true);
        }
    }

    /**
     * Adds time to the current preparation phase. Call this on the logical server thread.
     * Returns false if the cycle is not in preparation or the requested extension is invalid.
     */
    public static boolean extendDaytime(int seconds) {
        if (phase != Phase.PREPARATION || seconds <= 0
                || seconds > (Integer.MAX_VALUE - ticksRemaining) / TICKS_PER_SECOND) {
            return false;
        }

        ticksRemaining += seconds * TICKS_PER_SECOND;
        lastAnnouncedPreparationSecond = -1;
        return true;
    }

    private static int preparationSecondsRemaining() {
        return (ticksRemaining - 1) / TICKS_PER_SECOND + 1;
    }

    private static void beginPreparation() {
        phase = Phase.PREPARATION;
        ticksRemaining = RANDOM.nextInt(MIN_PREPARATION_TICKS, MAX_PREPARATION_TICKS + 1);
        targetDayTime = dayTime + PHASE_DAYLIGHT_TICKS;
        lastAnnouncedPreparationSecond = -1;
    }

    private static void advancePhase(MinecraftServer server) {
        switch (phase) {
            case PREPARATION -> {
                phase = Phase.NIGHT;
                ticksRemaining = NIGHT_TICKS;
                targetDayTime = dayTime + PHASE_DAYLIGHT_TICKS;
                announce(server, "Nightfall! Combat phase begins.");
            }
            case NIGHT -> {
                phase = Phase.COOLDOWN;
                ticksRemaining = COOLDOWN_TICKS;
                announce(server, "Night ends. Cooldown begins.");
            }
            case COOLDOWN -> {
                beginPreparation();
                announce(server, "A new day begins.");
            }
        }
        ModNetwork.sendPhaseToAll(phase);
    }

    private static void announce(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    public enum Phase {
        PREPARATION,
        NIGHT,
        COOLDOWN
    }
}




