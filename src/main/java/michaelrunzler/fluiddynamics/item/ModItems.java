package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.block.MaterialEnum;
import michaelrunzler.fluiddynamics.block.OreEnum;
import michaelrunzler.fluiddynamics.generators.FDEnLangProvider;
import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

        //
        // Material listings
        //

        for(MaterialEnum type : MaterialEnum.values())
        {
            // Generate supplier and register items for ingot, nugget, and dust of this type
            Supplier<Item> itemSupplier = () -> new Item(new Item.Properties().tab(CreativeTabs.TAB_ITEMS).rarity(Rarity.COMMON).stacksTo(64));

            RegistryObject<Item> ingot = registerItem("ingot_" + type.name().toLowerCase(), itemSupplier);
            RegistryObject<Item> nugget = registerItem("nugget_" + type.name().toLowerCase(), itemSupplier);
            RegistryObject<Item> dust = registerItem("dust_" + type.name().toLowerCase(), itemSupplier);

            // Register language mappings
            FDEnLangProvider.addItemLangMapping(ingot, type.englishName + " Ingot");
            FDEnLangProvider.addItemLangMapping(nugget, type.englishName + " Nugget");
            FDEnLangProvider.addItemLangMapping(dust, "Powdered " + type.englishName);

            // These items don't have any special attributes, so we can ignore tag mappings
        }

        //
        // Crushed ore listings
        //

        for(OreEnum type : OreEnum.values())
        {
            // Same as for materials
            Supplier<Item> itemSupplier = () -> new Item(new Item.Properties().tab(CreativeTabs.TAB_ITEMS).rarity(Rarity.COMMON).stacksTo(64));

            RegistryObject<Item> dust = registerItem("crushed_" + type.name().toLowerCase(), itemSupplier);
            RegistryObject<Item> pureDust = registerItem("purified_" + type.name().toLowerCase(), itemSupplier);
            FDEnLangProvider.addItemLangMapping(dust, "Crushed " + type.englishName);
            FDEnLangProvider.addItemLangMapping(pureDust, "Purified Crushed " + type.englishName);
        }

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
