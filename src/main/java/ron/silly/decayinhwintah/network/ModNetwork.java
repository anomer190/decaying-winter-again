package ron.silly.decayinhwintah.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import ron.silly.decayinhwintah.CycleManager;
import ron.silly.decayinhwintah.Decayinhwintah;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Decayinhwintah.MODID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int nextMessageId;

    private ModNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(PhaseSyncPacket.class, nextMessageId++)
                .encoder(PhaseSyncPacket::encode)
                .decoder(PhaseSyncPacket::decode)
                .consumerMainThread(PhaseSyncPacket::handle)
                .add();
    }

    public static void sendPhaseToAll(CycleManager.Phase phase) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new PhaseSyncPacket(phase));
    }

    public static void sendPhase(ServerPlayer player, CycleManager.Phase phase) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PhaseSyncPacket(phase));
    }

    private record PhaseSyncPacket(CycleManager.Phase phase) {
        private void encode(FriendlyByteBuf buffer) { buffer.writeEnum(phase); }
        private static PhaseSyncPacket decode(FriendlyByteBuf buffer) {
            return new PhaseSyncPacket(buffer.readEnum(CycleManager.Phase.class));
        }
        private static void handle(PhaseSyncPacket packet, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> contextSupplier) {
            net.minecraftforge.network.NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> ron.silly.decayinhwintah.client.ClientPhaseHandler.setPhase(packet.phase())));
            context.setPacketHandled(true);
        }
    }
}

