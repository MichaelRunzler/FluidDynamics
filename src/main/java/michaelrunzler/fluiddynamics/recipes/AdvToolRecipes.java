package michaelrunzler.fluiddynamics.recipes;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class AdvToolRecipes
{
    public static void generateAdvToolRecipes(Consumer<FinishedRecipe> c)
    {
        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("energy_cell"))
                .pattern(" c ")
                .pattern("tbt")
                .pattern("trt")
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('c', RecipeGenerator.registryToItem("ingot_copper"))
                .define('r', Items.REDSTONE)
                .define('t', RecipeGenerator.registryToItem("ingot_tin"))
                .unlockedBy("energy_cell_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("ingot_beryllium")))
                .save(c, "energy_cell");

        ShapelessRecipeBuilder.shapeless(RecipeGenerator.registryToItem("energy_cell"), 1)
                .requires(RecipeGenerator.registryToItem("ingot_beryllium"), 1)
                .requires(RecipeGenerator.registryToItem("depleted_cell"), 1)
                .requires(Items.REDSTONE, 1)
                .unlockedBy("depleted_cell_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(RecipeGenerator.registryToItem("depleted_cell")))
                .save(c, "depleted_cell_recharge");

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("portable_grinder"))
                .pattern("bub")
                .pattern("apa")
                .pattern("cdc")
                .define('a', RecipeGenerator.registryToItem("ingot_aluminium"))
                .define('b', RecipeGenerator.registryToItem("ingot_beryllium"))
                .define('c', RecipeGenerator.registryToItem("ingot_cobalt"))
                .define('u', RecipeGenerator.registryToItem("ingot_copper"))
                .define('p', RecipeGenerator.registryToItem("energy_cell"))
                .define('d', Items.DIAMOND)
                .unlockedBy("portable_grinder_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(c, "portable_grinder");
    }
}
