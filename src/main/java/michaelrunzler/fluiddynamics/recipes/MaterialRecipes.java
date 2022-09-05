package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.types.MaterialEnum;
import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.item.ModItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/**
 * Serves the {@link michaelrunzler.fluiddynamics.generators.FDRecipeProvider} class with recipes for material items
 * (blocks, ingots, nuggets, and dusts).
 */
public class MaterialRecipes
{
    public static final float VANILLA_FURNACE_MELT_POINT = 1800.0f;
    private static final float BASE_SMELT_MULTIPLIER = 0.2f;
    private static final float ACCELERATED_SMELT_MULTIPLIER = 0.1f;

    public static void generateMaterialRecipes(Consumer<FinishedRecipe> c)
    {
        for(MaterialEnum type : MaterialEnum.values())
        {
            blockRecipe(type, c);
            nuggetRecipe(type, c);
            nuggetIngotRecipe(type, c);
            dustSmeltingRecipe(type, c);
            blockIngotRecipe(type, c);
            portableGrinderIngot(type, c);
            smallToLargeDustRecipe(type, c);
            largeToSmallDustRecipe(type, c);
        }
        
        // Add alloy crafting recipes
        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("dust_bronze").get(), 2)
                .requires(ModItems.registeredItems.get("dust_copper").get(), 2)
                .requires(ModItems.registeredItems.get("dust_tin").get(), 1)
                .group("fluid_dynamics_bronze_alloy")
                .unlockedBy("bronze_alloy_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_tin").get()))
                .save(c, "bronze_alloy");

        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("dust_invar").get(), 2)
                .requires(ModItems.registeredItems.get("dust_iron").get(), 2)
                .requires(ModItems.registeredItems.get("dust_nickel").get(), 1)
                .group("fluid_dynamics_invar_alloy")
                .unlockedBy("invar_alloy_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_nickel").get()))
                .save(c, "invar_alloy");
        
        // Add vanilla small-to-large and v/v dust crafting recipes
        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("dust_gold").get(), 1)
                .requires(ModItems.registeredItems.get("dust_small_gold").get(), 4)
                .group("fluid_dynamics_materials_small_dust")
                .unlockedBy("stl_dust_gold_ore_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("dust_small_gold").get()))
                .save(c, "stl_dust_gold_ore");

        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("dust_iron").get(), 1)
                .requires(ModItems.registeredItems.get("dust_small_iron").get(), 4)
                .group("fluid_dynamics_materials_small_dust")
                .unlockedBy("stl_dust_iron_ore_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("dust_small_iron").get()))
                .save(c, "stl_dust_iron_ore");

        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("dust_small_gold").get(), 4)
                .requires(ModItems.registeredItems.get("dust_gold").get(), 1)
                .group("fluid_dynamics_materials_dust")
                .unlockedBy("lts_dust_gold_ore_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("dust_gold").get()))
                .save(c, "lts_dust_gold_ore");

        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("dust_small_iron").get(), 4)
                .requires(ModItems.registeredItems.get("dust_iron").get(), 1)
                .group("fluid_dynamics_materials_dust")
                .unlockedBy("lts_dust_iron_ore_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("dust_iron").get()))
                .save(c, "lts_dust_iron_ore");
        
        // Add vanilla dust smelting recipes
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.registeredItems.get("dust_gold").get()), Items.GOLD_INGOT, 0.1f, (int)(1337.0f * ACCELERATED_SMELT_MULTIPLIER))
                .group("fluid_dynamics_materials_dust")
                .unlockedBy("dust_gold_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("dust_gold").get()))
                .save(c, "dust_smelting_standard_gold");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.registeredItems.get("dust_iron").get()), Items.GOLD_INGOT, 0.1f, (int)(1798.0f * ACCELERATED_SMELT_MULTIPLIER))
                .group("fluid_dynamics_materials_dust")
                .unlockedBy("dust_iron_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("dust_iron").get()))
                .save(c, "dust_smelting_standard_iron");
    }

    private static void blockRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material ingot with type: " + matName);

        ShapedRecipeBuilder.shaped(ModBlockItems.registeredBItems.get("block_" + matName).get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', itm.get())
                .group("fluid_dynamics_materials_block")
                .unlockedBy("block_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "block_" + matName);
    }

    private static void nuggetIngotRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("nugget_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material nugget with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("ingot_" + matName).get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', itm.get())
                .group("fluid_dynamics_materials_ingot_n")
                .unlockedBy("n_ingot_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "nugget_ingot_" + matName);
    }

    private static void blockIngotRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModBlockItems.registeredBItems.get("block_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material block with type: " + matName);

        RegistryObject<Item> out = ModItems.registeredItems.get("ingot_" + matName);
        ShapelessRecipeBuilder.shapeless(out.get(), 9)
                .requires(itm.get())
                .group("fluid_dynamics_materials_ingot_b")
                .unlockedBy("b_ingot_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(out.get()))
                .save(c, "block_ingot_" + matName);
    }

    private static void nuggetRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material ingot with type: " + matName);

        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("nugget_" + matName).get(), 9)
                .requires(itm.get())
                .group("fluid_dynamics_materials_nugget")
                .unlockedBy("nugget_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "nugget_" + matName);
    }

    private static void dustSmeltingRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        // Only add recipes for materials whose melt point is below the temp of a vanilla furnace (1,800K)
        if(mat.meltPoint >= VANILLA_FURNACE_MELT_POINT) return;

        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("dust_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material dust with type: " + matName);
        RegistryObject<Item> out = ModItems.registeredItems.get("ingot_" + matName);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(itm.get()), out.get(), 0.1f, (int)(mat.meltPoint * BASE_SMELT_MULTIPLIER))
                .group("fluid_dynamics_materials_dust")
                .unlockedBy("dust_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "dust_smelting_standard_" + matName);
    }

    private static void portableGrinderIngot(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> out = ModItems.registeredItems.get("dust_" + matName);
        RegistryObject<Item> in = ModItems.registeredItems.get("ingot_" + matName);
        if(in == null) throw new NullPointerException("Unable to find material ingot with type: " + matName);
        if(out == null) throw new NullPointerException("Unable to find material dust with type: " + matName);

        ShapelessRecipeBuilder.shapeless(out.get())
                .requires(in.get())
                .requires(ModItems.registeredItems.get("portable_grinder").get())
                .group("fluid_dynamics_grinding_portable_ingot")
                .unlockedBy("ingot_grinding_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(in.get()))
                .save(c, "ingot_grinding_portable_" + matName);
    }

    private static void largeToSmallDustRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("dust_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material dust with type: " + matName);

        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("dust_small_" + matName).get(), 4)
                .requires(itm.get())
                .group("fluid_dynamics_materials_small_dust")
                .unlockedBy("lts_dust_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "lts_dust_" + matName);
    }

    private static void smallToLargeDustRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("dust_small_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material dust with type: " + matName);

        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("dust_" + matName).get(), 1)
                .requires(itm.get(), 4)
                .group("fluid_dynamics_materials_small_dust")
                .unlockedBy("stl_dust_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "stl_dust_" + matName);
    }
}
