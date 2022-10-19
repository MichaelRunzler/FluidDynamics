package michaelrunzler.fluiddynamics.recipes;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * Serves the {@link michaelrunzler.fluiddynamics.generators.FDRecipeProvider} class with recipes for component items
 * (used in crafting other items or blocks).
 */
public class ComponentRecipes
{
    public static void generateComponentRecipes(Consumer<FinishedRecipe> c)
    {
        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("machine_frame"))
                .pattern("zbc")
                .pattern("aoa")
                .pattern("cbz")
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('a', RecipeGenerator.registryToItem("ingot_aluminium"))
                .define('o', RecipeGenerator.registryToItem("ingot_cobalt"))
                .define('z', RecipeGenerator.registryToItem("ingot_bronze"))
                .unlockedBy("machine_frame_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_bronze")))
                .save(c, "machine_frame");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("conduit_basic"))
                .pattern("ggg")
                .pattern("cbc")
                .pattern("ggg")
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('g', Items.GLASS_PANE)
                .unlockedBy("conduit_basic_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_beryllium")))
                .save(c, "conduit_basic");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("conduit_enh"))
                .pattern("ggg")
                .pattern("aba")
                .pattern("ggg")
                .define('g', Items.GLASS_PANE)
                .define('a', Items.GOLD_INGOT)
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .unlockedBy("conduit_enh_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_beryllium")))
                .save(c, "conduit_enh");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("conduit_super"))
                .pattern("ooo")
                .pattern("ses")
                .pattern("ooo")
                .define('s', RecipeGenerator.registryToItem("ingot_superconductor"))
                .define('e', Items.EMERALD)
                .define('o', Items.OBSIDIAN)
                .unlockedBy("conduit_super_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_superconductor")))
                .save(c, "conduit_super_trigger");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("power_converter"))
                .pattern("bcb")
                .pattern("rgr")
                .pattern("bcb")
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('r', Items.REDSTONE)
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("power_converter_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLD_INGOT))
                .save(c, "power_converter");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("redstone_dynamo"))
                .pattern("bgb")
                .pattern("rdr")
                .pattern("bgb")
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('d', Items.DIAMOND)
                .define('r', Items.REDSTONE)
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("redstone_dynamo_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(c, "redstone_dynamo");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("actuator"))
                .pattern("fnf")
                .pattern("zbz")
                .pattern("crc")
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('z', RecipeGenerator.registryToItem("ingot_bronze"))
                .define('n', RecipeGenerator.registryToItem("ingot_nickel"))
                .define('r', Items.REDSTONE)
                .define('f', Items.IRON_INGOT)
                .unlockedBy("actuator_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_bronze")))
                .save(c, "actuator");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("heating_element"))
                .pattern("cii")
                .pattern("r i")
                .pattern("cii")
                .define('i', RecipeGenerator.registryToItem("ingot_invar"))
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('r', Items.REDSTONE)
                .unlockedBy("heating_element_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_invar")))
                .save(c, "heating_element");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("high_temp_heating_element"))
                .pattern("cww")
                .pattern("r w")
                .pattern("cww")
                .define('w', RecipeGenerator.registryToItem("ingot_tungsten"))
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('r', Items.REDSTONE)
                .unlockedBy("high_temp_heating_element_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_tungsten")))
                .save(c, "high_temp_heating_element");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("pressure_vessel"))
                .pattern("sgs")
                .pattern("ggg")
                .pattern("tst")
                .define('s', RecipeGenerator.registryToItem("ingot_steel"))
                .define('t', RecipeGenerator.registryToItem("ingot_titanium"))
                .define('g', Items.GLASS_PANE)
                .unlockedBy("pressure_vessel_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_titanium")))
                .save(c, "pressure_vessel");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("electromagnet"))
                .pattern("ccc")
                .pattern("nfn")
                .pattern("ccc")
                .define('n', RecipeGenerator.registryToItem("ingot_nickel"))
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('f', Items.IRON_INGOT)
                .unlockedBy("electromagnet_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_nickel")))
                .save(c, "electromagnet");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("superconductor"))
                .pattern("sss")
                .pattern("ioi")
                .pattern("sss")
                .define('i', RecipeGenerator.registryToItem("ingot_iridium"))
                .define('o', RecipeGenerator.registryToItem("ingot_osmium"))
                .define('s', RecipeGenerator.registryToItem("ingot_superconductor"))
                .unlockedBy("superconductor_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_superconductor")))
                .save(c, "superconductor");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("heat_exchanger"))
                .pattern("ana")
                .pattern("cgc")
                .pattern("ana")
                .define('a', RecipeGenerator.registryToItem("ingot_aluminium"))
                .define('n', RecipeGenerator.registryToItem("ingot_nickel"))
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("heat_exchanger_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLD_INGOT))
                .save(c, "heat_exchanger");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("processor"))
                .pattern("bgr")
                .pattern("gsg")
                .pattern("rgb")
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('s', RecipeGenerator.registryToItem("ingot_silicon"))
                .define('r', Items.REDSTONE)
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("processor_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_silicon")))
                .save(c, "processor");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("chemical_reactor"))
                .pattern("ctc")
                .pattern("gpg")
                .pattern("ctc")
                .define('c', RecipeGenerator.registryToItem("ingot_cobalt"))
                .define('p', RecipeGenerator.registryToItem("ingot_palladium"))
                .define('t', RecipeGenerator.registryToItem("ingot_titanium"))
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("chemical_reactor_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_palladium")))
                .save(c, "chemical_reactor");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("beam_emitter"))
                .pattern("brb")
                .pattern("ndn")
                .pattern(" g ")
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('n', RecipeGenerator.registryToItem("ingot_nickel"))
                .define('r', Items.REDSTONE_BLOCK)
                .define('d', Items.DIAMOND)
                .define('g', Items.GLOWSTONE)
                .unlockedBy("beam_emitter_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLOWSTONE))
                .save(c, "beam_emitter");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("filter_screen"))
                .pattern("coc")
                .pattern("qqq")
                .pattern("coc")
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('o', Items.OBSIDIAN)
                .define('q', Items.QUARTZ)
                .unlockedBy("filter_screen_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.QUARTZ))
                .save(c, "filter_screen");
    }
}
