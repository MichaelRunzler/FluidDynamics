package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.block.MaterialEnum;
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
    public static void generateMaterialRecipes(Consumer<FinishedRecipe> c)
    {
        for(MaterialEnum type : MaterialEnum.values()) {
            blockRecipe(type, c);
            //nuggetRecipe(type, c); TODO add back in once nuggets and dusts are added
            //nuggetIngotRecipe(type, c);
            //dustSmeltingRecipe(type, c);
            blockIngotRecipe(type, c);
        }
    }

    private static void blockRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + mat.name().toLowerCase());
        ShapedRecipeBuilder.shaped(ModBlockItems.registeredBItems.get("block_" + mat.name().toLowerCase()).get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', itm.get())
                .group("fluid_dynamics_materials_block")
                .unlockedBy("block_" + mat.name().toLowerCase() + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c);
    }

    private static void nuggetIngotRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        RegistryObject<Item> itm = ModItems.registeredItems.get("nugget_" + mat.name().toLowerCase());
        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("ingot_" + mat.name().toLowerCase()).get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', itm.get())
                .group("fluid_dynamics_materials_ingot")
                .unlockedBy("n_ingot_" + mat.name().toLowerCase() + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c);
    }

    private static void blockIngotRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        RegistryObject<Item> itm = ModBlockItems.registeredBItems.get("block_" + mat.name().toLowerCase());
        RegistryObject<Item> out = ModItems.registeredItems.get("ingot_" + mat.name().toLowerCase());
        ShapelessRecipeBuilder.shapeless(out.get(), 9)
                .requires(itm.get())
                .group("fluid_dynamics_materials_ingot")
                .unlockedBy("b_ingot_" + mat.name().toLowerCase() + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(out.get()))
                .save(c);
    }

    private static void nuggetRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        RegistryObject<Item> itm = ModBlockItems.registeredBItems.get("ingot_" + mat.name().toLowerCase());
        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("nugget_" + mat.name().toLowerCase()).get(), 9)
                .requires(itm.get())
                .group("fluid_dynamics_materials_nugget")
                .unlockedBy("nugget_" + mat.name().toLowerCase() + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c);
    }

    private static void dustSmeltingRecipe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        RegistryObject<Item> itm = ModBlockItems.registeredBItems.get("dust_" + mat.name().toLowerCase());
        RegistryObject<Item> out = ModBlockItems.registeredBItems.get("ingot_" + mat.name().toLowerCase());
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(itm.get()), out.get(), mat.meltPoint / 500.0f, 100)
                .group("fluid_dynamics_materials_dust")
                .unlockedBy("dust_" + mat.name().toLowerCase() + "_trigget", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c);
    }
}
