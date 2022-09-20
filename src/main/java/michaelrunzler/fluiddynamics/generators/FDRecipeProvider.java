package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.recipes.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
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
        RecipeIndex.generateAllRecipes(consumer);
        ToolArmorRecipes.generateArmor(consumer);
        ToolArmorRecipes.generateTools(consumer);
        ComponentRecipes.generateComponentRecipes(consumer);
        AdvToolRecipes.generateAdvToolRecipes(consumer);
        MachineRecipes.generateMachineRecipes(consumer);
    }
}

