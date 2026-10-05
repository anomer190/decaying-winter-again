package ron.silly.decayinhwintah.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import ron.silly.decayinhwintah.ModBlocks;
import ron.silly.decayinhwintah.blocks.FaceAttachBlock;
import ron.silly.decayinhwintah.screen.ClientGuiHandler;

public class SprayCanItem extends Item {
    public SprayCanItem(Properties properties) {
        // Set maximum durability to 128
        super(properties.durability(128).setNoRepair());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                // Open the custom GUI Screen (Implemented in Step 4)
                ClientGuiHandler.openSprayCanScreen(hand);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos targetPos = clickedPos.relative(face);
        ItemStack stack = context.getItemInHand();

        // Fallback checks for air/replacable positions
        BlockState targetState = level.getBlockState(targetPos);
        boolean isAir = targetState.isAir() || targetState.canBeReplaced();
        BlockPos finalPos = isAir ? targetPos : clickedPos;

        // Grab custom data from NBT
        CompoundTag tag = stack.getOrCreateTag();
        int style = tag.getInt("SelectedStyle");
        int color = tag.contains("SelectedColor") ? tag.getInt("SelectedColor") : 0; // Default White

        BlockState markingState = ModBlocks.CUSTOM_MARKING.get().defaultBlockState()
                .setValue(FaceAttachBlock.STYLE, style)
                .setValue(FaceAttachBlock.COLOR, color)
                .setValue(FaceAttachBlock.getFaceProperty(face.getOpposite()), true);

        if (level.setBlock(finalPos, markingState, 3)) {
            level.playSound(null, finalPos, SoundEvents.SPIDER_DEATH, SoundSource.PLAYERS, 0.5F, 1.5F);
            if (!player.isCreative()) {
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}
