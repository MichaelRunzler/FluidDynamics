package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.item.ModItems;
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
        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("power_conduit").get())
                .pattern("ggg")
                .pattern("cbc")
                .pattern("ggg")
                .define('b', ModItems.registeredItems.get("ingot_beryllium").get())
                .define('c', ModItems.registeredItems.get("ingot_copper").get())
                .define('g', Items.GLASS_PANE)
                .unlockedBy("power_conduit_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_copper").get()))
                .save(c, "power_conduit");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("power_converter").get())
                .pattern("bcb")
                .pattern("rgr")
                .pattern("bcb")
                .define('b', ModItems.registeredItems.get("ingot_beryllium").get())
                .define('c', ModItems.registeredItems.get("ingot_copper").get())
                .define('r', Items.REDSTONE)
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("power_converter_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_beryllium").get()))
                .save(c, "power_converter");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("redstone_dynamo").get())
                .pattern("bgb")
                .pattern("rdr")
                .pattern("bgb")
                .define('b', ModItems.registeredItems.get("ingot_beryllium").get())
                .define('d', Items.DIAMOND)
                .define('r', Items.REDSTONE)
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("redstone_dynamo_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(c, "redstone_dynamo");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("actuator").get())
                .pattern("fnf")
                .pattern("zbz")
                .pattern("crc")
                .define('b', ModItems.registeredItems.get("ingot_beryllium").get())
                .define('c', ModItems.registeredItems.get("ingot_copper").get())
                .define('z', ModItems.registeredItems.get("ingot_bronze").get())
                .define('n', ModItems.registeredItems.get("ingot_nickel").get())
                .define('r', Items.REDSTONE)
                .define('f', Items.IRON_INGOT)
                .unlockedBy("actuator_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_bronze").get()))
                .save(c, "actuator");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("heating_element").get())
                .pattern("cii")
                .pattern("r i")
                .pattern("cii")
                .define('i', ModItems.registeredItems.get("ingot_invar").get())
                .define('c', ModItems.registeredItems.get("ingot_copper").get())
                .define('r', Items.REDSTONE)
                .unlockedBy("heating_element_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_invar").get()))
                .save(c, "heating_element");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("high_temp_heating_element").get())
                .pattern("cww")
                .pattern("r w")
                .pattern("cww")
                .define('w', ModItems.registeredItems.get("ingot_tungsten").get())
                .define('c', ModItems.registeredItems.get("ingot_copper").get())
                .define('r', Items.REDSTONE)
                .unlockedBy("high_temp_heating_element_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_tungsten").get()))
                .save(c, "high_temp_heating_element");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("pressure_vessel").get())
                .pattern("sgs")
                .pattern("ggg")
                .pattern("tst")
                .define('s', ModItems.registeredItems.get("ingot_steel").get())
                .define('t', ModItems.registeredItems.get("ingot_titanium").get())
                .define('g', Items.GLASS_PANE)
                .unlockedBy("pressure_vessel_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_titanium").get()))
                .save(c, "pressure_vessel");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("electromagnet").get())
                .pattern("ccc")
                .pattern("nfn")
                .pattern("ccc")
                .define('n', ModItems.registeredItems.get("ingot_nickel").get())
                .define('c', ModItems.registeredItems.get("ingot_copper").get())
                .define('f', Items.IRON_INGOT)
                .unlockedBy("electromagnet_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_nickel").get()))
                .save(c, "electromagnet");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("superconductor").get())
                .pattern("sss")
                .pattern("ioi")
                .pattern("sss")
                .define('i', ModItems.registeredItems.get("ingot_iridium").get())
                .define('o', ModItems.registeredItems.get("ingot_osmium").get())
                .define('s', ModItems.registeredItems.get("ingot_superconductor").get())
                .unlockedBy("superconductor_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_superconductor").get()))
                .save(c, "superconductor");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("super_conduit").get())
                .pattern("ooo")
                .pattern("ses")
                .pattern("ooo")
                .define('s', ModItems.registeredItems.get("ingot_superconductor").get())
                .define('e', Items.EMERALD)
                .define('o', Items.OBSIDIAN)
                .unlockedBy("super_conduit_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_superconductor").get()))
                .save(c, "super_conduit");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("heat_exchanger").get())
                .pattern("ana")
                .pattern("cgc")
                .pattern("ana")
                .define('a', ModItems.registeredItems.get("ingot_aluminium").get())
                .define('n', ModItems.registeredItems.get("ingot_nickel").get())
                .define('c', ModItems.registeredItems.get("ingot_copper").get())
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("heat_exchanger_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_aluminium").get()))
                .save(c, "heat_exchanger");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("processor").get())
                .pattern("bgr")
                .pattern("gsg")
                .pattern("rgb")
                .define('b', ModItems.registeredItems.get("ingot_beryllium").get())
                .define('s', ModItems.registeredItems.get("ingot_silicon").get())
                .define('r', Items.REDSTONE)
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("processor_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_silicon").get()))
                .save(c, "processor");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("chemical_reactor").get())
                .pattern("ctc")
                .pattern("gpg")
                .pattern("ctc")
                .define('c', ModItems.registeredItems.get("ingot_cobalt").get())
                .define('p', ModItems.registeredItems.get("ingot_palladium").get())
                .define('t', ModItems.registeredItems.get("ingot_titanium").get())
                .define('g', Items.GOLD_INGOT)
                .unlockedBy("chemical_reactor_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_palladium").get()))
                .save(c, "chemical_reactor");
    }
}
