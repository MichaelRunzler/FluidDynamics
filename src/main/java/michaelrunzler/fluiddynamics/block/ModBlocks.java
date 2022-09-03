package michaelrunzler.fluiddynamics.block;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.generators.FDBlockStateProvider;
import michaelrunzler.fluiddynamics.generators.FDBlockTagProvider;
import michaelrunzler.fluiddynamics.generators.FDEnLangProvider;
import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.machines.MFMD.MFMDBlock;
import michaelrunzler.fluiddynamics.machines.purifier.PurifierBlock;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import michaelrunzler.fluiddynamics.types.MaterialEnum;
import michaelrunzler.fluiddynamics.types.OreEnum;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
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

    public static void registerAllBlocks()
    {
        FluidDynamics.logModEvent(Level.DEBUG, "Starting block registration cycle...");

        //
        // Ores
        //

        for(OreEnum type : OreEnum.values())
        {
            FDOreHelper ore = new FDOreHelper(type);
            RegistryObject<Block> block = registerBlock(ore.name, () -> new FDOre(type));
            for(TagKey<Block> k : ore.tags) if(k != null) FDBlockTagProvider.addTagMapping(k, block.getKey());

            FDEnLangProvider.addBlockLangMapping(block, ore.englishName);
        }

        //
        // Resource Blocks
        //

        for(MaterialEnum type : MaterialEnum.values())
        {
            FDMaterialBlockHelper mat = new FDMaterialBlockHelper(type);
            RegistryObject<Block> block = registerBlock(mat.name, () -> new FDMaterialBlock(type));
            for(TagKey<Block> k : mat.tags) if(k != null) FDBlockTagProvider.addTagMapping(k, block.getKey());

            FDEnLangProvider.addBlockLangMapping(block, mat.englishName);
        }

        //
        // Machine Blocks
        //

        RegistryObject<Block> mfmd = registerBlock(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase(), MFMDBlock::new);
        FDEnLangProvider.addBlockLangMapping(mfmd, MachineEnum.MOLECULAR_DECOMPILER.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, mfmd.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_STONE_TOOL, mfmd.getKey());

        RegistryObject<Block> purifier = registerBlock(MachineEnum.PURIFIER.name().toLowerCase(), PurifierBlock::new);
        FDEnLangProvider.addBlockLangMapping(purifier, MachineEnum.PURIFIER.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.PURIFIER.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, purifier.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_STONE_TOOL, purifier.getKey());

        //
        // Special Non-Machine Blocks
        //

        RegistryObject<Block> machineFrame = specialRegisterBlock("machine_frame",
                () -> new Block(BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(3.0f).requiresCorrectToolForDrops()),
                new Item.Properties().tab(CreativeTabs.TAB_COMPONENTS).rarity(Rarity.COMMON).stacksTo(64).setNoRepair());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, machineFrame.getKey());
        FDEnLangProvider.addBlockLangMapping(machineFrame, "Universal Machine Frame");

        FluidDynamics.logModEvent(Level.DEBUG, "...done.");
    }

    public static final Item.Properties DEFAULT_ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.TAB_BLOCKS).stacksTo(64).rarity(Rarity.COMMON).setNoRepair();

    /**
     * Registers a block with the Block Registry with the specified ID, then adds the resulting registry entry to the registered block map.
     * Also adds a corresponding BlockItem to the Item Registry.
     */
    public static RegistryObject<Block> registerBlock(String id, Supplier<? extends Block> block) {
        return specialRegisterBlock(id, block, DEFAULT_ITEM_PROPERTIES);
    }

    /**
     * Registers a block with the Block Registry with the specified ID, then adds the resulting registry entry to the registered block map.
     * Also adds a corresponding BlockItem to the Item Registry with the provided Item Properties.
     */
    public static RegistryObject<Block> specialRegisterBlock(String id, Supplier<? extends Block> block, Item.Properties properties)
    {
        RegistryObject<Block> obj = blocks.register(id, block);
        registeredBlocks.put(id, obj);
        ModBlockItems.registerBItem(obj.getId().getPath(), () -> new BlockItem((obj.get()), properties)); // Register an item supplier from the block supplier

        return obj;
    }
}
