package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.item.ModItems;
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
        ShapedRecipeBuilder.shaped(ModBlockItems.registeredBItems.get(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase()).get())
                .pattern("gpg")
                .pattern("sud")
                .pattern("fef")
                .define('p', ModItems.registeredItems.get("power_converter").get())
                .define('s', ModItems.registeredItems.get("actuator").get())
                .define('u', ModBlockItems.registeredBItems.get("machine_frame").get())
                .define('d', ModItems.registeredItems.get("actuator").get())
                .define('e', ModItems.registeredItems.get("beam_emitter").get())
                .define('g', Items.GLOWSTONE)
                .define('f', Items.IRON_INGOT)
                .group("fluid_dynamics_machine_mfmd")
                .unlockedBy("machine_mfmd_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(c, "machine_mfmd");
    }
}
