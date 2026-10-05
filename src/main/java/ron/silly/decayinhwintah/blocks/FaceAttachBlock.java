package ron.silly.decayinhwintah.blocks;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FaceAttachBlock extends MultifaceBlock {
    // 0 to 3 allows for 4 different texture designs. Increase max value if you want more!
    public static final IntegerProperty STYLE = IntegerProperty.create("style", 0, 3);
    // 0 to 15 mapped to Minecraft's 16 Dye Colors
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);

    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public FaceAttachBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(STYLE, 0)
                .setValue(COLOR, DyeColor.WHITE.getId()));
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STYLE, COLOR);
    }
}