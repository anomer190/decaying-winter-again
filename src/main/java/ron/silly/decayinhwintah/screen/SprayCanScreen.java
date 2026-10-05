package ron.silly.decayinhwintah.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import ron.silly.decayinhwintah.network.ModMessages;
import ron.silly.decayinhwintah.network.SprayCanSyncPacket;

public class SprayCanScreen extends Screen {
    private final InteractionHand hand;
    private int selectedStyle = 0;
    private int selectedColor = 0;

    public SprayCanScreen(InteractionHand hand) {
        super(Component.literal("Spray Can Settings"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        // Example: Button to increment texture style cycle
        this.addRenderableWidget(Button.builder(Component.literal("Cycle Style"), (btn) -> {
            selectedStyle = (selectedStyle + 1) % 4;
            sendChangesToServer();
        }).bounds(this.width / 2 - 50, this.height / 2 - 30, 100, 20).build());

        // Example: Button to cycle color index
        this.addRenderableWidget(Button.builder(Component.literal("Cycle Color"), (btn) -> {
            selectedColor = (selectedColor + 1) % 16;
            sendChangesToServer();
        }).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    private void sendChangesToServer() {
        // Send packet to server to save changes into NBT
        ModMessages.sendToServer(new SprayCanSyncPacket(selectedStyle, selectedColor, hand));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, "Style: " + selectedStyle + " | Color ID: " + selectedColor, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
    }
}
