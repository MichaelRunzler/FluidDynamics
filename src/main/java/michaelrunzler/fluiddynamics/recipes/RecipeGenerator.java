package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.block.ModBlocks;
import michaelrunzler.fluiddynamics.item.ModItems;
import michaelrunzler.fluiddynamics.types.GenericMachineRecipe;
import michaelrunzler.fluiddynamics.types.RecipeIngredient;
import michaelrunzler.fluiddynamics.types.XPGeneratingMachineRecipe;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/**
 * Provides utility methods for generating recipes for various types of crafting, including smelting and machine crafting.
 */
@SuppressWarnings({"ConstantConditions", "unused"})
public class RecipeGenerator
{
    private final Consumer<FinishedRecipe> c;

    public RecipeGenerator(Consumer<FinishedRecipe> c) {
        this.c = c;
    }

    //
    // Recipe generators
    //

    public void blockToIngot(Item block, Item ingot)
    {
        ShapelessRecipeBuilder.shapeless(ingot, 9)
                .requires(block, 1)
                .group("fluid_dynamics_materials_bti")
                .unlockedBy("bti_" + getName(block) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(block))
                .save(c, "bti_" + getName(block));
    }

    public void ingotToBlock(Item ingot, Item block)
    {
        ShapedRecipeBuilder.shaped(block)
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ingot)
                .group("fluid_dynamics_materials_itb")
                .unlockedBy("itb_" + getName(ingot) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ingot))
                .save(c, "itb_" + getName(ingot));
    }

    public void ingotToNugget(Item ingot, Item nugget)
    {
        ShapelessRecipeBuilder.shapeless(nugget, 9)
                .requires(ingot, 1)
                .group("fluid_dynamics_materials_itn")
                .unlockedBy("itn_" + getName(ingot) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ingot))
                .save(c, "itn_" + getName(ingot));
    }

    public void nuggetToIngot(Item nugget, Item ingot)
    {
        ShapedRecipeBuilder.shaped(ingot)
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', nugget)
                .group("fluid_dynamics_materials_nti")
                .unlockedBy("nti_" + getName(nugget) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(nugget))
                .save(c, "nti_" + getName(nugget));
    }

    public void ingotToDustPortable(Item ingot, Item dust)
    {
        Item portableGrinder = registryToItem("portable_grinder");
        ShapelessRecipeBuilder.shapeless(dust, 1)
                .requires(ingot, 1)
                .requires(portableGrinder, 1)
                .group("fluid_dynamics_grinding_portable")
                .unlockedBy("pgrinder_" + getName(ingot) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(portableGrinder))
                .save(c, "pgrinder_" + getName(ingot));
    }

    public GenericMachineRecipe ingotToDustMachine(Item ingot, Item dust, float time) {
        return new GenericMachineRecipe((int)time, ingot, new RecipeIngredient(dust, 1));
    }

    public void dustToIngotSmelting(Item dust, Item ingot, float time, float xp)
    {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(dust), ingot, xp, (int)time)
                .group("fluid_dynamics_smelting_dti")
                .unlockedBy("dust_smelting_" + getName(dust) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(dust))
                .save(c, "dust_smelting_" + getName(dust));
    }

    public void oreToIngotSmelting(Item ore, Item ingot, float time, float xp)
    {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ore), ingot, xp, (int)time)
                .group("fluid_dynamics_smelting_ore")
                .unlockedBy("ore_smelting_" + getName(ore) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(ore))
                .save(c, "ore_smelting_" + getName(ore));
    }

    public XPGeneratingMachineRecipe dustToIngotESmelting(Item dust, Item ingot, float time, float xp){
        return new XPGeneratingMachineRecipe((int)time, xp, dust, new RecipeIngredient(ingot, 1));
    }

    public void largeToSmallDust(Item lDust, Item sDust)
    {
        ShapelessRecipeBuilder.shapeless(sDust, 4)
                .requires(lDust, 1)
                .group("fluid_dynamics_materials_dust_lts")
                .unlockedBy("dust_lts_" + getName(lDust) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(lDust))
                .save(c, "dust_lts_" + getName(lDust));
    }

    public void smallToLargeDust(Item sDust, Item lDust)
    {
        ShapelessRecipeBuilder.shapeless(lDust, 1)
                .requires(sDust, 4)
                .group("fluid_dynamics_materials_dust_stl")
                .unlockedBy("dust_stl_" + getName(sDust) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(sDust))
                .save(c, "dust_stl_" + getName(sDust));
    }

    public void alloyRecipe(Item result, int numResult, RecipeIngredient... inputs)
    {
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(result, numResult)
                .group("fluid_dynamics_materials_alloy_" + getName(result))
                .unlockedBy("alloy_" + getName(result) + "_trigger", InventoryChangeTrigger.TriggerInstance.hasItems(inputs[0].ingredient()));

        for(RecipeIngredient r : inputs) builder = builder.requires(r.ingredient(), r.count());
        builder.save(c, "alloy_" + getName(result));
    }

    public GenericMachineRecipe alloySeparation(Item dust, float time, RecipeIngredient... outputs){
        return new GenericMachineRecipe((int)time, dust, outputs);
    }

    public GenericMachineRecipe crushedToPurified(Item crushed, Item purified, float time){
        return new GenericMachineRecipe((int)time, crushed, new RecipeIngredient(purified, 2));
    }

    public GenericMachineRecipe purifiedToDust(Item purified, float time, RecipeIngredient... results){
        return new GenericMachineRecipe((int)time, purified, results);
    }

    //
    // Utility methods
    //

    /**
     * Gets an item or BlockItem from the central item registries.
     * Tries to find an Item first, and then tries to find a BlockItem.
     * If both searches fail, an exception will be thrown.
     */
    public static Item registryToItem(String name)
    {
        RegistryObject<Item> i = null;
        if(name != null && !name.isEmpty()) {
            i = ModItems.registeredItems.get(name);
            if(i == null) i = ModBlockItems.registeredBItems.get(name);
        }

        if(i == null) throw new IllegalArgumentException("Could not find item with ID: " + name + " in any registry.");
        return i.get();
    }

    /**
     * Gets a Block from the central Block registry.
     * If the search fails, an exception will be thrown.
     */
    public static Block registryToBlock(String name)
    {
        RegistryObject<Block> b = null;
        if(name != null && !name.isEmpty()) b = ModBlocks.registeredBlocks.get(name);

        if(b == null) throw new IllegalArgumentException("Could not find block with ID: " + name + " in Block registry.");
        return b.get();
    }

    public static String getName(Item i){
        return i.getRegistryName().getPath().toLowerCase();
    }
}
