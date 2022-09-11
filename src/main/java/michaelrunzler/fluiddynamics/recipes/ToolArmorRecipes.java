package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.types.MaterialEnum;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * Serves the {@link michaelrunzler.fluiddynamics.generators.FDRecipeProvider} class with recipes for tools and armor.
 */
public class ToolArmorRecipes
{
    public static void generateArmor(Consumer<FinishedRecipe> c)
    {
        for(MaterialEnum type : MaterialEnum.values()) {
            armorRecipeHead(type, c);
            armorRecipeChest(type, c);
            armorRecipeLegs(type, c);
            armorRecipeFeet(type, c);
        }
    }

    public static void generateTools(Consumer<FinishedRecipe> c)
    {
        for(MaterialEnum type : MaterialEnum.values()) {
            toolRecipeAxe(type, c);
            toolRecipePick(type, c);
            toolRecipeSword(type, c);
            toolRecipeSpade(type, c);
            toolRecipeHoe(type, c);
        }
    }
    
    private static void armorRecipeHead(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("armor_head_" + matName))
                .pattern("xxx")
                .pattern("x x")
                .pattern("   ")
                .define('x', itm)
                .group("fluid_dynamics_armor_head")
                .unlockedBy("armor_head_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "armor_head_" + matName);
    }

    private static void armorRecipeChest(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("armor_chest_" + matName))
                .pattern("x x")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', itm)
                .group("fluid_dynamics_armor_chest")
                .unlockedBy("armor_chest_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "armor_chest_" + matName);
    }

    private static void armorRecipeLegs(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("armor_legs_" + matName))
                .pattern("xxx")
                .pattern("x x")
                .pattern("x x")
                .define('x', itm)
                .group("fluid_dynamics_armor_legs")
                .unlockedBy("armor_legs_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "armor_legs_" + matName);
    }

    private static void armorRecipeFeet(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("armor_feet_" + matName))
                .pattern("   ")
                .pattern("x x")
                .pattern("x x")
                .define('x', itm)
                .group("fluid_dynamics_armor_feet")
                .unlockedBy("armor_feet_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "armor_feet_" + matName);
    }
    
    private static void toolRecipeAxe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("axe_" + matName))
                .pattern("xx ")
                .pattern("xs ")
                .pattern(" s ")
                .define('x', itm)
                .define('s', Items.STICK)
                .group("fluid_dynamics_axe")
                .unlockedBy("axe_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "axe_" + matName);
    }

    private static void toolRecipePick(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("pickaxe_" + matName))
                .pattern("xxx")
                .pattern(" s ")
                .pattern(" s ")
                .define('x', itm)
                .define('s', Items.STICK)
                .group("fluid_dynamics_pickaxe")
                .unlockedBy("pickaxe_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "pickaxe_" + matName);
    }

    private static void toolRecipeSpade(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("spade_" + matName))
                .pattern(" x ")
                .pattern(" s ")
                .pattern(" s ")
                .define('x', itm)
                .define('s', Items.STICK)
                .group("fluid_dynamics_spade")
                .unlockedBy("spade_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "spade_" + matName);
    }

    private static void toolRecipeSword(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("sword_" + matName))
                .pattern(" x ")
                .pattern(" x ")
                .pattern(" s ")
                .define('x', itm)
                .define('s', Items.STICK)
                .group("fluid_dynamics_sword")
                .unlockedBy("sword_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "sword_" + matName);
    }

    private static void toolRecipeHoe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        Item itm = RecipeGenerator.registryToItem("ingot_" + matName);

        ShapedRecipeBuilder.shaped(RecipeGenerator.registryToItem("hoe_" + matName))
                .pattern("xx ")
                .pattern(" s ")
                .pattern(" s ")
                .define('x', itm)
                .define('s', Items.STICK)
                .group("fluid_dynamics_hoe")
                .unlockedBy("hoe_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm))
                .save(c, "hoe_" + matName);
    }
}
