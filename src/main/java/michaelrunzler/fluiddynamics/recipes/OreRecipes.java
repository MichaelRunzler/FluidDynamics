package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.block.MaterialEnum;
import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.item.ModItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Serves the {@link michaelrunzler.fluiddynamics.generators.FDRecipeProvider} class with recipes that use ores.
 */
public class OreRecipes 
{
    public static void generateOreSmeltingRecipes(Consumer<FinishedRecipe> c)
    {
        rawOreSmeltingRecipe(c, "native_tin", "tin", (int)(MaterialEnum.TIN.meltPoint / 5));
        rawOreSmeltingRecipe(c, "native_copper", "copper", (int)(MaterialEnum.COPPER.meltPoint / 5));
        rawOreSmeltingRecipe(c, "bertrandite", "beryllium", (int)(MaterialEnum.BERYLLIUM.meltPoint / 5));
        rawOreSmeltingRecipe(c, "spherocobaltite", "cobalt", (int)(MaterialEnum.COBALT.meltPoint / 5));
        rawOreSmeltingRecipe(c, "tetrataenite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint / 5));
        rawOreSmeltingRecipe(c, "bauxite", "aluminium", (int)(MaterialEnum.ALUMINIUM.meltPoint / 5));
        rawOreSmeltingRecipe(c, "pentlandite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint / 5));

        crushedOreSmeltingRecipe(c, "native_tin", "tin", (int)(MaterialEnum.TIN.meltPoint / 10));
        crushedOreSmeltingRecipe(c, "native_copper", "copper", (int)(MaterialEnum.COPPER.meltPoint / 10));
        crushedOreSmeltingRecipe(c, "bertrandite", "beryllium", (int)(MaterialEnum.BERYLLIUM.meltPoint / 10));
        crushedOreSmeltingRecipe(c, "spherocobaltite", "cobalt", (int)(MaterialEnum.COBALT.meltPoint / 10));
        crushedOreSmeltingRecipe(c, "tetrataenite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint / 10));
        crushedOreSmeltingRecipe(c, "bauxite", "aluminium", (int)(MaterialEnum.ALUMINIUM.meltPoint / 10));
        crushedOreSmeltingRecipe(c, "pentlandite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint / 10));

        purifiedOreSmeltingRecipe(c, "native_tin", "tin", (int)(MaterialEnum.TIN.meltPoint / 10));
        purifiedOreSmeltingRecipe(c, "native_copper", "copper", (int)(MaterialEnum.COPPER.meltPoint / 10));
        purifiedOreSmeltingRecipe(c, "bertrandite", "beryllium", (int)(MaterialEnum.BERYLLIUM.meltPoint / 10));
        purifiedOreSmeltingRecipe(c, "spherocobaltite", "cobalt", (int)(MaterialEnum.COBALT.meltPoint / 10));
        purifiedOreSmeltingRecipe(c, "tetrataenite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint / 10));
        purifiedOreSmeltingRecipe(c, "bauxite", "aluminium", (int)(MaterialEnum.ALUMINIUM.meltPoint / 10));
        purifiedOreSmeltingRecipe(c, "pentlandite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint / 10));
    }

    public static void rawOreSmeltingRecipe(Consumer<FinishedRecipe> c, String oreName, String matName, int time) {
        genericSmeltingRecipe(c, ModBlockItems.registeredBItems, "ore_" + oreName, "raw_ore", matName, time);
    }

    public static void crushedOreSmeltingRecipe(Consumer<FinishedRecipe> c, String oreName, String matName, int time) {
        genericSmeltingRecipe(c, ModItems.registeredItems, "crushed_" + oreName, "crushed_ore", matName, time);
    }

    public static void purifiedOreSmeltingRecipe(Consumer<FinishedRecipe> c, String oreName, String matName, int time) {
        genericSmeltingRecipe(c, ModItems.registeredItems, "purified_" + oreName, "purified_ore", matName, time);
    }

    public static void genericSmeltingRecipe(Consumer<FinishedRecipe> c, HashMap<String, RegistryObject<Item>> registry, String name, String type, String matName, int time)
    {
        RegistryObject<Item> tmp = registry.get(name);
        if(tmp == null) throw new NullPointerException("Unable to find Registry object with name: " + name);
        if(!ModItems.registeredItems.containsKey("ingot_" + matName)) throw new NullPointerException("Unable to find material ingot with name: " + matName);

        Item input = tmp.get();
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), ModItems.registeredItems.get("ingot_" + matName).get(), 0.5f, time)
                .group("fluid_dynamics_" + type + "_smelting")
                .unlockedBy(name + "_smelting", InventoryChangeTrigger.TriggerInstance.hasItems(input))
                .save(c, name + "_smelting");
    }
}
