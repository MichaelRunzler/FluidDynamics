package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.types.MaterialEnum;
import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.item.ModItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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
    private static final float BASE_SMELT_MULTIPLIER = 0.1f;
    private static final float ACCELERATED_SMELT_MULTIPLIER = 0.2f;
    
    public static void generateOreSmeltingRecipes(Consumer<FinishedRecipe> c)
    {
        // Raw ore can be smelted, and yields 1 at the standard speed
        rawOreSmeltingRecipe(c, "native_tin", "tin", (int)(MaterialEnum.TIN.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "native_copper", "copper", (int)(MaterialEnum.COPPER.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "bertrandite", "beryllium", (int)(MaterialEnum.BERYLLIUM.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "spherocobaltite", "cobalt", (int)(MaterialEnum.COBALT.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "tetrataenite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "bauxite", "aluminium", (int)(MaterialEnum.ALUMINIUM.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "pentlandite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * ACCELERATED_SMELT_MULTIPLIER));

        // Crushed ore yields 1 at double speed
        crushedOreSmeltingRecipe(c, "native_tin", "tin", (int)(MaterialEnum.TIN.meltPoint * BASE_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "native_copper", "copper", (int)(MaterialEnum.COPPER.meltPoint * BASE_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "bertrandite", "beryllium", (int)(MaterialEnum.BERYLLIUM.meltPoint * BASE_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "spherocobaltite", "cobalt", (int)(MaterialEnum.COBALT.meltPoint * BASE_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "tetrataenite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * BASE_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "bauxite", "aluminium", (int)(MaterialEnum.ALUMINIUM.meltPoint * BASE_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "pentlandite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * BASE_SMELT_MULTIPLIER));

        // Purified ore yields 1 at double speed, but you get 2 for every crushed ore
        purifiedOreSmeltingRecipe(c, "native_tin", "tin", (int)(MaterialEnum.TIN.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        purifiedOreSmeltingRecipe(c, "native_copper", "copper", (int)(MaterialEnum.COPPER.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        purifiedOreSmeltingRecipe(c, "bertrandite", "beryllium", (int)(MaterialEnum.BERYLLIUM.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        purifiedOreSmeltingRecipe(c, "spherocobaltite", "cobalt", (int)(MaterialEnum.COBALT.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        purifiedOreSmeltingRecipe(c, "tetrataenite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        purifiedOreSmeltingRecipe(c, "bauxite", "aluminium", (int)(MaterialEnum.ALUMINIUM.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        purifiedOreSmeltingRecipe(c, "pentlandite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * ACCELERATED_SMELT_MULTIPLIER));

        // Grinding raw ore yields 1 crushed ore of the proper type 
        portableGrinderOreRecipe(c, "native_tin");
        portableGrinderOreRecipe(c, "native_copper");
        portableGrinderOreRecipe(c, "bertrandite");
        portableGrinderOreRecipe(c, "spherocobaltite");
        portableGrinderOreRecipe(c, "tetrataenite");
        portableGrinderOreRecipe(c, "bauxite");
        portableGrinderOreRecipe(c, "pentlandite");
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

    private static void portableGrinderOreRecipe(Consumer<FinishedRecipe> c, String oreName)
    {
        RegistryObject<Item> in = ModBlockItems.registeredBItems.get("ore_" + oreName);
        RegistryObject<Item> out = ModItems.registeredItems.get("crushed_" + oreName);
        if(out == null) throw new NullPointerException("Unable to find ore with name: " + oreName);

        ShapelessRecipeBuilder.shapeless(out.get())
                .requires(in.get())
                .requires(ModItems.registeredItems.get("portable_grinder").get())
                .group("fluid_dynamics_grinding_portable_ore")
                .unlockedBy("ore_grinding_" + oreName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(in.get()))
                .save(c, "ore_grinding_portable_" + oreName);
    }
}
