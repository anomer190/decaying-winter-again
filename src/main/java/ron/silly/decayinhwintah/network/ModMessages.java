package ron.silly.decayinhwintah.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("yourmodid", "messages")) // Înlocuiește "yourmodid"
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Înregistrăm pachetul pe care l-am creat în etapa anterioară
        net.messageBuilder(SprayCanSyncPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SprayCanSyncPacket::decode)
                .encoder(SprayCanSyncPacket::encode)
                .consumerMainThread(SprayCanSyncPacket::handle)
                .add();
    }

    // Metodă utilitară pentru a trimite mesaje de la client la server
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}

