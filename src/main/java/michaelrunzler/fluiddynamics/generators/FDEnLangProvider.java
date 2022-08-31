package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.machines.MFMD.MFMDBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

/**
 * Provides English-language translations for block/item names.
 */
public class FDEnLangProvider extends LanguageProvider
{
    private static final HashMap<RegistryObject<Block>, String> blockMappings = new HashMap<>();
    private static final HashMap<RegistryObject<Item>, String> itemMappings = new HashMap<>();

    public FDEnLangProvider(DataGenerator gen) {
        super(gen, FluidDynamics.MODID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        // Add automappings from the generated blocks
        for(RegistryObject<Block> block : blockMappings.keySet())
            addBlock(block, blockMappings.get(block));

        for(RegistryObject<Item> item : itemMappings.keySet())
            addItem(item, itemMappings.get(item));

        // Add manual mappings
        add("itemGroup.tab_blocks", "Fluid Dynamics: Blocks");
        add("itemGroup.tab_resources", "Fluid Dynamics: Resources");
        add("itemGroup.tab_armor", "Fluid Dynamics: Armor");
        add("itemGroup.tab_tools", "Fluid Dynamics: Tools and Weapons");
        add("itemGroup.tab_components", "Fluid Dynamics: Components");

        add(MFMDBlock.SCREEN_TITLE, "Molecular Decompiler");
    }

    /**
     * Adds a new language mapping that will be added on the next call to addTranslations() through the resource generator system.
     */
    public static void addBlockLangMapping(RegistryObject<Block> block, String name) {
        blockMappings.put(block, name);
    }

    public static void addItemLangMapping(RegistryObject<Item> item, String name) {
        itemMappings.put(item, name);
    }
}
