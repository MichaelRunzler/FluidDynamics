package michaelrunzler.fluiddynamics.block;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.generators.FDBlockStateProvider;
import michaelrunzler.fluiddynamics.generators.FDBlockTagProvider;
import michaelrunzler.fluiddynamics.generators.FDEnLangProvider;
import michaelrunzler.fluiddynamics.generators.FDLootTableProvider;
import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.machines.MFMD.MFMDBlock;
import michaelrunzler.fluiddynamics.machines.centrifuge.CentrifugeBlock;
import michaelrunzler.fluiddynamics.machines.e_furnace.EFurnaceBlock;
import michaelrunzler.fluiddynamics.machines.ht_furnace.HTFurnaceBlock;
import michaelrunzler.fluiddynamics.machines.power_cell.PowerCellBlock;
import michaelrunzler.fluiddynamics.machines.purifier.PurifierBlock;
import michaelrunzler.fluiddynamics.machines.rbe_generator.RsBeGenBlock;
import michaelrunzler.fluiddynamics.machines.redstone_generator.RsGenBlock;
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
            FDEnLangProvider.addBlockLangMapping(block, ore.englishName);

            RegistryObject<Block> deepBlock = null;
            if(type.hasDeepslateVariant) {
                deepBlock = registerBlock("deepslate_" + ore.name, () -> new FDOre(type));
                FDEnLangProvider.addBlockLangMapping(deepBlock, "Deepslate " + ore.englishName);
            }

            for(TagKey<Block> k : ore.tags)
            {
                if(k != null) {
                    FDBlockTagProvider.addTagMapping(k, block.getKey());
                    if(type.hasDeepslateVariant) FDBlockTagProvider.addTagMapping(k, deepBlock.getKey());
                }
            }
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

        RegistryObject<Block> mfmd = specialRegisterBlock(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase(), MFMDBlock::new,
                new Item.Properties().tab(CreativeTabs.TAB_MACHINES).rarity(Rarity.UNCOMMON).stacksTo(64).setNoRepair());
        FDEnLangProvider.addBlockLangMapping(mfmd, MachineEnum.MOLECULAR_DECOMPILER.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, mfmd.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_STONE_TOOL, mfmd.getKey());

        RegistryObject<Block> purifier = specialRegisterBlock(MachineEnum.PURIFIER.name().toLowerCase(), PurifierBlock::new,
                new Item.Properties().tab(CreativeTabs.TAB_MACHINES).rarity(Rarity.UNCOMMON).stacksTo(64).setNoRepair());
        FDEnLangProvider.addBlockLangMapping(purifier, MachineEnum.PURIFIER.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.PURIFIER.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, purifier.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_STONE_TOOL, purifier.getKey());

        RegistryObject<Block> centrifuge = specialRegisterBlock(MachineEnum.CENTRIFUGE.name().toLowerCase(), CentrifugeBlock::new,
                new Item.Properties().tab(CreativeTabs.TAB_MACHINES).rarity(Rarity.UNCOMMON).stacksTo(64).setNoRepair());
        FDEnLangProvider.addBlockLangMapping(centrifuge, MachineEnum.CENTRIFUGE.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.CENTRIFUGE.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, centrifuge.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_IRON_TOOL, centrifuge.getKey());

        RegistryObject<Block> eFurnace = specialRegisterBlock(MachineEnum.E_FURNACE.name().toLowerCase(), EFurnaceBlock::new,
                new Item.Properties().tab(CreativeTabs.TAB_MACHINES).rarity(Rarity.UNCOMMON).stacksTo(64).setNoRepair());
        FDEnLangProvider.addBlockLangMapping(eFurnace, MachineEnum.E_FURNACE.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.E_FURNACE.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, eFurnace.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_STONE_TOOL, eFurnace.getKey());

        RegistryObject<Block> htFurnace = specialRegisterBlock(MachineEnum.HT_FURNACE.name().toLowerCase(), HTFurnaceBlock::new,
                new Item.Properties().tab(CreativeTabs.TAB_MACHINES).rarity(Rarity.UNCOMMON).stacksTo(64).setNoRepair());
        FDEnLangProvider.addBlockLangMapping(htFurnace, MachineEnum.HT_FURNACE.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.HT_FURNACE.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, htFurnace.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_DIAMOND_TOOL, htFurnace.getKey());

        RegistryObject<Block> powerCell = specialRegisterBlock(MachineEnum.POWER_CELL.name().toLowerCase(), PowerCellBlock::new,
                new Item.Properties().tab(CreativeTabs.TAB_MACHINES).rarity(Rarity.UNCOMMON).stacksTo(64).setNoRepair());
        FDEnLangProvider.addBlockLangMapping(powerCell, MachineEnum.POWER_CELL.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.POWER_CELL.name().toLowerCase());
        FDLootTableProvider.nonDefaultLootTableBlocks.add(MachineEnum.POWER_CELL.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, powerCell.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_STONE_TOOL, powerCell.getKey());

        RegistryObject<Block> rsGen = specialRegisterBlock(MachineEnum.RS_GENERATOR.name().toLowerCase(), RsGenBlock::new,
                new Item.Properties().tab(CreativeTabs.TAB_MACHINES).rarity(Rarity.UNCOMMON).stacksTo(64).setNoRepair());
        FDEnLangProvider.addBlockLangMapping(rsGen, MachineEnum.RS_GENERATOR.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.RS_GENERATOR.name().toLowerCase());
        FDLootTableProvider.nonDefaultLootTableBlocks.add(MachineEnum.RS_GENERATOR.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, rsGen.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_STONE_TOOL, rsGen.getKey());

        RegistryObject<Block> rbeGen = specialRegisterBlock(MachineEnum.RBE_GENERATOR.name().toLowerCase(), RsBeGenBlock::new,
                new Item.Properties().tab(CreativeTabs.TAB_MACHINES).rarity(Rarity.UNCOMMON).stacksTo(64).setNoRepair());
        FDEnLangProvider.addBlockLangMapping(rbeGen, MachineEnum.RBE_GENERATOR.englishName);
        FDBlockStateProvider.nonDefaultModelBlocks.add(MachineEnum.RBE_GENERATOR.name().toLowerCase());
        FDLootTableProvider.nonDefaultLootTableBlocks.add(MachineEnum.RBE_GENERATOR.name().toLowerCase());
        FDBlockTagProvider.addTagMapping(BlockTags.MINEABLE_WITH_PICKAXE, rbeGen.getKey());
        FDBlockTagProvider.addTagMapping(BlockTags.NEEDS_IRON_TOOL, rbeGen.getKey());

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
