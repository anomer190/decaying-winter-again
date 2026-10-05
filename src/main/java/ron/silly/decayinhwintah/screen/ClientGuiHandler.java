package ron.silly.decayinhwintah.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public class ClientGuiHandler {
    public static void openSprayCanScreen(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new SprayCanScreen(hand));
    }
}
