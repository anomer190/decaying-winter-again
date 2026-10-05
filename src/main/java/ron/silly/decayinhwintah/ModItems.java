package ron.silly.decayinhwintah;

import ron.silly.decayinhwintah.Decayinhwintah;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Decayinhwintah.MODID);

    public static final RegistryObject<Item> WOOD_SCRAP = ITEMS.register("wood_scrap",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STONE_SCRAP = ITEMS.register("stone_scrap",
            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}