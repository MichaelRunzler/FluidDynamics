package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.item.ModItems;
import michaelrunzler.fluiddynamics.types.MaterialEnum;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

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
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("armor_head_" + matName).get())
                .pattern("xxx")
                .pattern("x x")
                .pattern("   ")
                .define('x', itm.get())
                .group("fluid_dynamics_armor_head")
                .unlockedBy("armor_head_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "armor_head_" + matName);
    }

    private static void armorRecipeChest(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("armor_chest_" + matName).get())
                .pattern("x x")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', itm.get())
                .group("fluid_dynamics_armor_chest")
                .unlockedBy("armor_chest_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "armor_chest_" + matName);
    }

    private static void armorRecipeLegs(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("armor_legs_" + matName).get())
                .pattern("xxx")
                .pattern("x x")
                .pattern("x x")
                .define('x', itm.get())
                .group("fluid_dynamics_armor_legs")
                .unlockedBy("armor_legs_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "armor_legs_" + matName);
    }

    private static void armorRecipeFeet(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("armor_feet_" + matName).get())
                .pattern("   ")
                .pattern("x x")
                .pattern("x x")
                .define('x', itm.get())
                .group("fluid_dynamics_armor_feet")
                .unlockedBy("armor_feet_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "armor_feet_" + matName);
    }
    
    private static void toolRecipeAxe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);
        
        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("axe_" + matName).get())
                .pattern("xx ")
                .pattern("xs ")
                .pattern("  s")
                .define('x', itm.get())
                .define('s', Items.STICK)
                .group("fluid_dynamics_axe")
                .unlockedBy("axe_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "axe_" + matName);
    }

    private static void toolRecipePick(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("pickaxe_" + matName).get())
                .pattern("xxx")
                .pattern(" s ")
                .pattern(" s ")
                .define('x', itm.get())
                .define('s', Items.STICK)
                .group("fluid_dynamics_pickaxe")
                .unlockedBy("pickaxe_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "pickaxe_" + matName);
    }

    private static void toolRecipeSpade(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("spade_" + matName).get())
                .pattern(" x ")
                .pattern(" s ")
                .pattern(" s ")
                .define('x', itm.get())
                .define('s', Items.STICK)
                .group("fluid_dynamics_spade")
                .unlockedBy("spade_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "spade_" + matName);
    }

    private static void toolRecipeSword(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("sword_" + matName).get())
                .pattern(" x ")
                .pattern(" x ")
                .pattern(" s ")
                .define('x', itm.get())
                .define('s', Items.STICK)
                .group("fluid_dynamics_sword")
                .unlockedBy("sword_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "sword_" + matName);
    }

    private static void toolRecipeHoe(MaterialEnum mat, Consumer<FinishedRecipe> c)
    {
        String matName = mat.name().toLowerCase();
        RegistryObject<Item> itm = ModItems.registeredItems.get("ingot_" + matName);
        if(itm == null) throw new NullPointerException("Unable to find material with type: " + matName);

        ShapedRecipeBuilder.shaped(ModItems.registeredItems.get("hoe_" + matName).get())
                .pattern("xx ")
                .pattern(" s ")
                .pattern(" s ")
                .define('x', itm.get())
                .define('s', Items.STICK)
                .group("fluid_dynamics_hoe")
                .unlockedBy("hoe_" + matName + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(itm.get()))
                .save(c, "hoe_" + matName);
    }
}
