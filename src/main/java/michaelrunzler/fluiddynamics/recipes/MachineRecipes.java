package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * Serves the {@link michaelrunzler.fluiddynamics.generators.FDRecipeProvider} class with recipes for machines.
 */
public class MachineRecipes
{
    public static void generateMachineRecipes(Consumer<FinishedRecipe> c)
    {
        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase()))
                .pattern("gpg")
                .pattern("sud")
                .pattern("fef")
                .define('p', RecipeGenerator.registryToItem("power_converter"))
                .define('s', RecipeGenerator.registryToItem("actuator"))
                .define('u', RecipeGenerator.registryToItem("machine_frame"))
                .define('d', RecipeGenerator.registryToItem("actuator"))
                .define('e', RecipeGenerator.registryToItem("beam_emitter"))
                .define('g', Items.GLOWSTONE)
                .define('f', Items.IRON_INGOT)
                .group("fluid_dynamics_machine_mfmd")
                .unlockedBy("machine_mfmd_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(c, "machine_mfmd");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem(MachineEnum.PURIFIER.name().toLowerCase()))
                .pattern("bub")
                .pattern("sfs")
                .pattern("epe")
                .define('b', Items.IRON_BARS)
                .define('u', Items.BUCKET)
                .define('s', RecipeGenerator.registryToItem("actuator"))
                .define('f', RecipeGenerator.registryToItem("machine_frame"))
                .define('e', RecipeGenerator.registryToItem("electromagnet"))
                .define('p', RecipeGenerator.registryToItem("power_converter"))
                .group("fluid_dynamics_machine_purifier")
                .unlockedBy("machine_purifier_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_nickel")))
                .save(c, "machine_purifier");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem(MachineEnum.CENTRIFUGE.name().toLowerCase()))
                .pattern("ece")
                .pattern("sfs")
                .pattern("eoe")
                .define('e', RecipeGenerator.registryToItem("electromagnet"))
                .define('c', RecipeGenerator.registryToItem("power_converter"))
                .define('f', RecipeGenerator.registryToItem("machine_frame"))
                .define('s', RecipeGenerator.registryToItem("actuator"))
                .define('o', Items.OBSIDIAN)
                .group("fluid_dynamics_machine_centrifuge")
                .unlockedBy("machine_centrifuge_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.OBSIDIAN))
                .save(c, "machine_centrifuge");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem(MachineEnum.E_FURNACE.name().toLowerCase()))
                .pattern("ici")
                .pattern("ofo")
                .pattern("ihi")
                .define('i', Items.IRON_INGOT)
                .define('c', RecipeGenerator.registryToItem("power_converter"))
                .define('o', Items.OBSIDIAN)
                .define('f', RecipeGenerator.registryToItem("machine_frame"))
                .define('h', RecipeGenerator.registryToItem("heating_element"))
                .group("fluid_dynamics_machine_efurnace")
                .unlockedBy("machine_efurnace_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_invar")))
                .save(c, "machine_efurnace");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem(MachineEnum.HT_FURNACE.name().toLowerCase()))
                .pattern("nmn")
                .pattern("nfn")
                .pattern("oio")
                .define('n', Items.NETHER_BRICK)
                .define('m', Items.MAGMA_BLOCK)
                .define('f', Items.BLAST_FURNACE)
                .define('o', Items.OBSIDIAN)
                .define('i', RecipeGenerator.registryToItem("block_invar"))
                .group("fluid_dynamics_machine_htfurnace")
                .unlockedBy("machine_htfurnace_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHER_BRICK))
                .save(c, "machine_htfurnace");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem(MachineEnum.POWER_CELL.name().toLowerCase()))
                .pattern("ccc")
                .pattern("pfp")
                .pattern("ccc")
                .define('c', RecipeGenerator.registryToItem("energy_cell"))
                .define('p', RecipeGenerator.registryToItem("power_conduit"))
                .define('f', RecipeGenerator.registryToItem("machine_frame"))
                .group("fluid_dynamics_machine_powercell")
                .unlockedBy("machine_powercell_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("energy_cell")))
                .save(c, "machine_powercell");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem(MachineEnum.RS_GENERATOR.name().toLowerCase()))
                .pattern("bcb")
                .pattern("pdp")
                .pattern("ofo")
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('c', RecipeGenerator.registryToItem("energy_cell"))
                .define('p', RecipeGenerator.registryToItem("power_converter"))
                .define('d', RecipeGenerator.registryToItem("redstone_dynamo"))
                .define('o', RecipeGenerator.registryToItem("ingot_cobalt"))
                .define('f', RecipeGenerator.registryToItem("machine_frame"))
                .group("fluid_dynamics_machine_rs_generator")
                .unlockedBy("machine_rs_generator_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("energy_cell")))
                .save(c, "machine_rs_generator");
    }
}
