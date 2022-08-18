package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.item.ModItems;
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
        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("energy_cell").get())
                .pattern(" c ")
                .pattern("tbt")
                .pattern("trt")
                .define('b', ModItems.registeredItems.get("ingot_beryllium").get())
                .define('c', ModItems.registeredItems.get("ingot_copper").get())
                .define('r', Items.REDSTONE)
                .define('t', ModItems.registeredItems.get("ingot_tin").get())
                .unlockedBy("energy_cell_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("ingot_beryllium").get()))
                .save(c, "energy_cell");

        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("energy_cell").get(), 1)
                .requires(ModItems.registeredItems.get("ingot_beryllium").get(), 1)
                .requires(ModItems.registeredItems.get("depleted_cell").get(), 1)
                .requires(Items.REDSTONE, 1)
                .unlockedBy("depleted_cell_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("depleted_cell").get()))
                .save(c, "depleted_cell_recharge");

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("portable_grinder").get())
                .pattern("aba")
                .pattern("apa")
                .pattern("cgc")
                .define('a', ModItems.registeredItems.get("ingot_aluminium").get())
                .define('b', ModItems.registeredItems.get("ingot_beryllium").get())
                .define('c', ModItems.registeredItems.get("ingot_cobalt").get())
                .define('p', ModItems.registeredItems.get("energy_cell").get())
                .define('g', Items.GLOWSTONE)
                .unlockedBy("portable_grinder_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("energy_cell").get()))
                .save(c, "portable_grinder");

        // This recipe trips the container definition for the uncharged grinder, adding a fully-charged grinder to the
        // player's inventory alongside the depleted cell output by the crafting recipe
        ShapelessRecipeBuilder.shapeless(ModItems.registeredItems.get("depleted_cell").get(), 1)
                .requires(ModItems.registeredItems.get("energy_cell").get(), 1)
                .requires(ModItems.registeredItems.get("uncharged_portable_grinder").get(), 1)
                .unlockedBy("grinder_recharge_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.registeredItems.get("uncharged_portable_grinder").get()))
                .save(c, "grinder_recharge");
    }
}
