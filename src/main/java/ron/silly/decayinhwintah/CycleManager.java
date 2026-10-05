package ron.silly.decayinhwintah;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Decayinhwintah.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CycleManager {
    private static final int TICKS_PER_SECOND = 20;
    private static final int MIN_PREPARATION_TICKS = 90 * TICKS_PER_SECOND;
    private static final int MAX_PREPARATION_TICKS = 150 * TICKS_PER_SECOND;
    private static final int NIGHT_TICKS = 10 * TICKS_PER_SECOND;
    private static final int COOLDOWN_TICKS = 7 * TICKS_PER_SECOND;

    private static final RandomSource RANDOM = RandomSource.create();
    private static MinecraftServer activeServer;
    private static Phase phase = Phase.PREPARATION;
    private static int ticksRemaining;

    private CycleManager() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        if (server != activeServer) {
            activeServer = server;
            beginPreparation();
        }

        long fixedTime = phase == Phase.NIGHT ? 13_000L : 1_000L;
        for (ServerLevel level : server.getAllLevels()) {
            level.setDayTime(fixedTime);
        }

        if (--ticksRemaining <= 0) {
            advancePhase(server);
        }
    }

    private static void beginPreparation() {
        phase = Phase.PREPARATION;
        ticksRemaining = RANDOM.nextInt(MIN_PREPARATION_TICKS, MAX_PREPARATION_TICKS + 1);
    }

    private static void advancePhase(MinecraftServer server) {
        switch (phase) {
            case PREPARATION -> {
                phase = Phase.NIGHT;
                ticksRemaining = NIGHT_TICKS;
                announce(server, "Nightfall! Combat phase begins.");
            }
            case NIGHT -> {
                phase = Phase.COOLDOWN;
                ticksRemaining = COOLDOWN_TICKS;
                announce(server, "Night ends. Cooldown begins.");
            }
            case COOLDOWN -> {
                beginPreparation();
                announce(server, "A new day begins. Prepare for the next wave.");
            }
        }
    }

    private static void announce(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    private enum Phase {
        PREPARATION,
        NIGHT,
        COOLDOWN
    }
}