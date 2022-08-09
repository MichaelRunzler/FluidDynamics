package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Tracks all registered items for the mod.
 */
public class ModItems
{
    public static final DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, FluidDynamics.MODID);
    public static final HashMap<String, RegistryObject<Item>> registeredItems = new HashMap<>();

    /**
     * Registers all items in the mod with the Item Registry.
     */
    public static void registerAllItems()
    {
        FluidDynamics.logModEvent(Level.DEBUG, "Starting item registration cycle...");

        registerItem("ingot_copper", () -> new Item(new Item.Properties().tab(CreativeTabs.TAB_ITEMS)));
        //TODO add more items

        FluidDynamics.logModEvent(Level.DEBUG, "...done.");
    }

    /**
     * Registers an item with the Item Registry with the specified ID, then adds the resulting registry entry to the registered item map.
     */
    public static RegistryObject<Item> registerItem(String id, Supplier<? extends Item> item)
    {
        RegistryObject<Item> obj = items.register(id, item);
        registeredItems.put(id, obj);
        return obj;
    }
}
