package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.item.ModItems;
import michaelrunzler.fluiddynamics.recipes.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;

/**
 * Provides crafting recipes for added items and blocks.
 */
public class FDRecipeProvider extends RecipeProvider
{
    public FDRecipeProvider(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildCraftingRecipes(@NonNull Consumer<FinishedRecipe> consumer)
    {
        MaterialRecipes.generateMaterialRecipes(consumer);
        OreRecipes.generateOreSmeltingRecipes(consumer);
        ToolArmorRecipes.generateArmor(consumer);
        ToolArmorRecipes.generateTools(consumer);
        ComponentRecipes.generateComponentRecipes(consumer);
        AdvToolRecipes.generateAdvToolRecipes(consumer);
        MachineRecipes.generateMachineRecipes(consumer);

        //
        // Special smelting recipes
        //

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.REDSTONE), ModItems.registeredItems.get("nugget_silicon").get(), 0.1f, 100)
                .group("fluid_dynamics_silicon_wafer_smelting")
                .unlockedBy("silicon_wafer_smelting", InventoryChangeTrigger.TriggerInstance.hasItems(Items.REDSTONE))
                .save(consumer, "silicon_wafer_smelting");

    }
}

