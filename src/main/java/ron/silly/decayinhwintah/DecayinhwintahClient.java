package ron.silly.decayinhwintah;

import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ron.silly.decayinhwintah.blocks.FaceAttachBlock;

// Înlocuiește "yourmodid" con ID-ul modului tău
@Mod.EventBusSubscriber(modid = "decayinhwintah", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DecayinhwintahClient {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Înregistrează colorarea pentru blocul tău
        event.register((state, level, pos, tintIndex) -> {
            if (state.hasProperty(FaceAttachBlock.COLOR)) {
                int colorId = state.getValue(FaceAttachBlock.COLOR);
                // .getTextureDiffuseColor() returnează valoarea hex perfectă pentru culoarea lânii/colorantului din vanilla
                return DyeColor.byId(colorId).getTextColor();
            }
            return -1; // -1 înseamnă că nu aplică nicio tentă de culoare (alb implicit)
        }, ModBlocks.CUSTOM_MARKING.get()); // Înlocuiește cu registrul tău pentru bloc
    }
}
