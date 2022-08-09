package michaelrunzler.fluiddynamics.block;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.generators.FDBlockTagProvider;
import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.item.ModItems;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Tracks all registered blocks for the mod.
 */
public class ModBlocks
{
    public static final DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, FluidDynamics.MODID);
    public static final HashMap<String, RegistryObject<Block>> registeredBlocks = new HashMap<>();

    /**
     * Registers all blocks in the mod with the Block Registry.
     */
    public static void registerAllBlocks()
    {
        FluidDynamics.logModEvent(Level.DEBUG, "Starting block registration cycle...");

        //
        // Ores
        //

        //registerBlock("ore_native_copper", () -> new FDOre(OreEnum.NATIVE_COPPER));

        for(OreEnum type : OreEnum.values())
        {
            FDOreHelper ore = new FDOreHelper(type);
            RegistryObject<Block> block = registerBlock(ore.name, () -> new FDOre(type));
            for(TagKey<Block> k : ore.tags)
                if(k != null)
                    FDBlockTagProvider.addTagMapping(k, block.getKey());
        }

        FluidDynamics.logModEvent(Level.DEBUG, "...done.");
    }

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.TAB_BLOCKS);

    /**
     * Registers a block with the Block Registry with the specified ID, then adds the resulting registry entry to the registered block map.
     * Also adds a corresponding BlockItem to the Item Registry.
     */
    public static RegistryObject<Block> registerBlock(String id, Supplier<? extends Block> block)
    {
        RegistryObject<Block> obj = blocks.register(id, block);
        registeredBlocks.put(id, obj);
        ModBlockItems.registerBItem(obj.getId().getPath(), () -> new BlockItem((obj.get()), ITEM_PROPERTIES)); // Register an item supplier from the block supplier

        return obj;
    }
}
