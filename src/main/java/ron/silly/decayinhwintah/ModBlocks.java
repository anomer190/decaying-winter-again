package ron.silly.decayinhwintah;

import ron.silly.decayinhwintah.Decayinhwintah;
import ron.silly.decayinhwintah.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ron.silly.decayinhwintah.blocks.FaceAttachBlock;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Decayinhwintah.MODID);

    public static final RegistryObject<Block> ROUGH_CONCRETE = registerBlock("rough_concrete",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.LIGHT_GRAY_CONCRETE).sound(SoundType.STONE)));
    public static final RegistryObject<Block> GREEN_ROUGH_CONCRETE = registerBlock("green_rough_concrete",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GREEN_CONCRETE).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CUSTOM_MARKING = BLOCKS.register("custom_marking",
            () -> new FaceAttachBlock(BlockBehaviour.Properties.of()
                    .noCollission()                       // Allows entities to walk right through the markings
                    .instabreak()                         // Breaks instantly like a drawing or overlay
                    .sound(SoundType.STONE)         // Or choose wool/stone depending on your marking type
                    .lightLevel(state -> 0)               // Change to a higher number if you want them to glow
            ));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}