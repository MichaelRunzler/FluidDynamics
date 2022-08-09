package michaelrunzler.fluiddynamics.block;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Used by the Block Registry to register item forms of all blocks.
 */
public class ModBlockItems
{
    public static final DeferredRegister<Item> blockitems = DeferredRegister.create(ForgeRegistries.ITEMS, FluidDynamics.MODID);
    public static final HashMap<String, RegistryObject<Item>> registeredBItems = new HashMap<>();

    public static RegistryObject<Item> registerBItem(String id, Supplier<? extends Item> item)
    {
        RegistryObject<Item> obj = blockitems.register(id, item);
        registeredBItems.put(id, obj);
        return obj;
    }
}
