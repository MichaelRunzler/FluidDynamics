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

    public static void generateMaterialRecipes(Consumer<FinishedRecipe> c)
    {
        for(MaterialEnum type : MaterialEnum.values()) {
            blockRecipe(type, c);
            nuggetRecipe(type, c);
            nuggetIngotRecipe(type, c);
            dustSmeltingRecipe(type, c);
            blockIngotRecipe(type, c);
        }
    }

    private static void blockRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

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
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

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
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

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
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

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
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);
        RegistryObject<Item> out = ModItems.registeredItems.get("ingot_" + matName);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(itm.get()), out.get(), 0.5f, (int)(mat.meltPoint / 5))
                .group("fluid_dynamics_materials_dust")
                .unlockedBy("dust_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "dust_smelting_standard_" + matName);
    }
}
