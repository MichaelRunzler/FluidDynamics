package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.recipes.MaterialRecipes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

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
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
    {
        MaterialRecipes.generateMaterialRecipes(consumer);
    }
}

