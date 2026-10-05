package ron.silly.decayinhwintah.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;
import ron.silly.decayinhwintah.items.SprayCanItem;

import java.util.function.Supplier;

public class SprayCanSyncPacket {
    private final int style;
    private final int color;
    private final InteractionHand hand;

    public SprayCanSyncPacket(int style, int color, InteractionHand hand) {
        this.style = style;
        this.color = color;
        this.hand = hand;
    }

    public static void encode(SprayCanSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.style);
        buf.writeInt(msg.color);
        buf.writeEnum(msg.hand);
    }

    public static SprayCanSyncPacket decode(FriendlyByteBuf buf) {
        return new SprayCanSyncPacket(buf.readInt(), buf.readInt(), buf.readEnum(InteractionHand.class));
    }

    public static void handle(SprayCanSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getItemInHand(msg.hand);
                if (stack.getItem() instanceof SprayCanItem) {
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("SelectedStyle", msg.style);
                    tag.putInt("SelectedColor", msg.color);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}