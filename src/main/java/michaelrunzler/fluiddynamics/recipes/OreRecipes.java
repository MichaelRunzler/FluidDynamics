package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.types.MaterialEnum;
import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.item.ModItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Serves the {@link michaelrunzler.fluiddynamics.generators.FDRecipeProvider} class with recipes that use ores.
 */
public class OreRecipes 
{
    private static final float BASE_SMELT_MULTIPLIER = 0.2f;
    private static final float ACCELERATED_SMELT_MULTIPLIER = 0.1f;
    
    public static void generateOreSmeltingRecipes(Consumer<FinishedRecipe> c)
    {
        // Raw ore can be smelted, and yields 1 at the standard speed
        rawOreSmeltingRecipe(c, "native_tin", "tin", (int)(MaterialEnum.TIN.meltPoint * BASE_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "native_copper", "copper", (int)(MaterialEnum.COPPER.meltPoint * BASE_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "bertrandite", "beryllium", (int)(MaterialEnum.BERYLLIUM.meltPoint * BASE_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "spherocobaltite", "cobalt", (int)(MaterialEnum.COBALT.meltPoint * BASE_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "tetrataenite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * BASE_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "bauxite", "aluminium", (int)(MaterialEnum.ALUMINIUM.meltPoint * BASE_SMELT_MULTIPLIER));
        rawOreSmeltingRecipe(c, "pentlandite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * BASE_SMELT_MULTIPLIER));

        // Crushed ore yields 1 at double speed
        crushedOreSmeltingRecipe(c, "native_tin", "tin", (int)(MaterialEnum.TIN.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "native_copper", "copper", (int)(MaterialEnum.COPPER.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "bertrandite", "beryllium", (int)(MaterialEnum.BERYLLIUM.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "spherocobaltite", "cobalt", (int)(MaterialEnum.COBALT.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "tetrataenite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "bauxite", "aluminium", (int)(MaterialEnum.ALUMINIUM.meltPoint * ACCELERATED_SMELT_MULTIPLIER));
        crushedOreSmeltingRecipe(c, "pentlandite", "nickel", (int)(MaterialEnum.NICKEL.meltPoint * ACCELERATED_SMELT_MULTIPLIER));

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

        // Add recipes for vanilla gold/iron ores, coal, and endstone
        portableGrinderRecipe(c, Items.GOLD_ORE, ModItems.registeredItems.get("crushed_gold_ore").get());
        portableGrinderRecipe(c, Items.IRON_ORE, ModItems.registeredItems.get("crushed_iron_ore").get());
        portableGrinderRecipe(c, Items.CHARCOAL, ModItems.registeredItems.get("dust_coal").get());
        portableGrinderRecipe(c, Items.END_STONE, ModItems.registeredItems.get("crushed_endstone").get());
        portableGrinderRecipe(c, Items.GOLD_INGOT, ModItems.registeredItems.get("dust_gold").get());
        portableGrinderRecipe(c, Items.IRON_INGOT, ModItems.registeredItems.get("dust_iron").get());
        
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.registeredItems.get("crushed_gold_ore").get()),
                        Items.GOLD_INGOT, 0.5f, (int)(1337.0f * ACCELERATED_SMELT_MULTIPLIER))
                .group("fluid_dynamics_gold_ore_smelting")
                .unlockedBy("crushed_gold_ore_smelting_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("crushed_gold_ore").get()))
                .save(c, "crushed_gold_ore_smelting");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.registeredItems.get("purified_gold_ore").get()),
                        Items.GOLD_INGOT, 0.5f, (int)(1337.0f * ACCELERATED_SMELT_MULTIPLIER))
                .group("fluid_dynamics_gold_ore_smelting")
                .unlockedBy("purified_gold_ore_smelting_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("purified_gold_ore").get()))
                .save(c, "purified_gold_ore_smelting");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.registeredItems.get("crushed_iron_ore").get()),
                        Items.IRON_INGOT, 0.5f, (int)(1798.0f * ACCELERATED_SMELT_MULTIPLIER))
                .group("fluid_dynamics_iron_ore_smelting")
                .unlockedBy("crushed_iron_ore_smelting_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("crushed_iron_ore").get()))
                .save(c, "crushed_iron_ore_smelting");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.registeredItems.get("purified_iron_ore").get()),
                        Items.IRON_INGOT, 0.5f, (int)(1798.0f * ACCELERATED_SMELT_MULTIPLIER))
                .group("fluid_dynamics_iron_ore_smelting")
                .unlockedBy("purified_iron_ore_smelting_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("purified_iron_ore").get()))
                .save(c, "purified_iron_ore_smelting");
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
                .unlockedBy(name + "_smelting_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(input))
                .save(c, name + "_smelting");
    }

    private static void portableGrinderOreRecipe(Consumer<FinishedRecipe> c, String oreName)
    {
        RegistryObject<Item> in = ModBlockItems.registeredBItems.get("ore_" + oreName);
        RegistryObject<Item> out = ModItems.registeredItems.get("crushed_" + oreName);
        if(out == null) throw new NullPointerException("Unable to find ore with name: " + oreName);

        portableGrinderRecipe(c, in.get(), out.get());
    }

    private static void portableGrinderRecipe(Consumer<FinishedRecipe> c, Item in, Item out)
    {
        ShapelessRecipeBuilder.shapeless(out)
                .requires(in)
                .requires(ModItems.registeredItems.get("portable_grinder").get())
                .group("fluid_dynamics_grinding_portable_ore")
                .unlockedBy("grinding_portable_" + Objects.requireNonNull(in.getRegistryName()).getPath() +
                        "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(in))
                .save(c, "grinding_portable_" + in.getRegistryName().getPath());
    }
}
