package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.block.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Provides English-language translations for block/item names.
 */
public class FDEnLangProvider extends LanguageProvider
{
    public FDEnLangProvider(DataGenerator gen) {
        super(gen, FluidDynamics.MODID, "en-us");
    }

    @Override
    protected void addTranslations()
    {
        // We can't really automatically do this because the names for TEs don't follow a strict naming scheme,
        // so we have to do it manually

        //
        // Ores
        //

        addBlock(ModBlocks.registeredBlocks.get("ore_native_copper"), " Ore");
        addBlock(ModBlocks.registeredBlocks.get("ore_native_tin"), " Ore");
        addBlock(ModBlocks.registeredBlocks.get("ore_pentlandite"), " Ore");
        addBlock(ModBlocks.registeredBlocks.get("ore_spherocobaltite"), " Ore");
        addBlock(ModBlocks.registeredBlocks.get("ore_tetrataenite"), " Ore");
        addBlock(ModBlocks.registeredBlocks.get("ore_wolframite"), " Ore");
        addBlock(ModBlocks.registeredBlocks.get("ore_bauxite"), " Ore");
        addBlock(ModBlocks.registeredBlocks.get("ore_bertrandite"), " Ore");

        //
        //  Resource Blocks
        //

        addBlock(ModBlocks.registeredBlocks.get("block_aluminum"), "Aluminium Block");
        addBlock(ModBlocks.registeredBlocks.get("block_beryllium"), "Beryllium Block");
        addBlock(ModBlocks.registeredBlocks.get("block_bronze"), "Bronze Block");
        addBlock(ModBlocks.registeredBlocks.get("block_cobalt"), "Cobalt Block");
        addBlock(ModBlocks.registeredBlocks.get("block_copper"), "Copper Block");
        addBlock(ModBlocks.registeredBlocks.get("block_invar"), "Invar Block");
        addBlock(ModBlocks.registeredBlocks.get("block_iridium"), "Iridium Block");
        addBlock(ModBlocks.registeredBlocks.get("block_nickel"), "Nickel Block");
        addBlock(ModBlocks.registeredBlocks.get("block_osmium"), "Osmium Block");
        addBlock(ModBlocks.registeredBlocks.get("block_palladium"), "Palladium Block");
        addBlock(ModBlocks.registeredBlocks.get("block_sharicite"), "Inert Sharicite Block");
        addBlock(ModBlocks.registeredBlocks.get("block_infused_sharicite"), "Energetic Sharicite Block");
        addBlock(ModBlocks.registeredBlocks.get("block_silicon"), "Crystalline Silicon Block");
        addBlock(ModBlocks.registeredBlocks.get("block_steel"), "Steel Block");
        addBlock(ModBlocks.registeredBlocks.get("block_superconductor"), "Superconducting Alloy Block");
        addBlock(ModBlocks.registeredBlocks.get("block_tin"), "Tin Block");
        addBlock(ModBlocks.registeredBlocks.get("block_titanium"), "Titanium Block");
        addBlock(ModBlocks.registeredBlocks.get("block_tungsten"), "Tungsten Block");


        //
        // Processed Resources (gems/ingots)
        //

        //TODO
    }
}
