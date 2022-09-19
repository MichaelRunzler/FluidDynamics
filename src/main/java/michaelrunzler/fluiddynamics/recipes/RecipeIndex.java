package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.types.*;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("SameParameterValue")
public class RecipeIndex
{
    // Machine recipe indexes
    public static Map<String, GenericMachineRecipe> MFMDRecipes = new HashMap<>();
    public static Map<String, GenericMachineRecipe> PurifierRecipes = new HashMap<>();
    public static Map<String, GenericMachineRecipe> CentrifugeRecipes = new HashMap<>();
    public static Map<String, XPGeneratingMachineRecipe> EFurnaceRecipes = new HashMap<>();
    public static Map<String, XPGeneratingMachineRecipe> HTFurnaceRecipes = new HashMap<>();
    private static RecipeGenerator gen = null;
    
    // Generator fuel indexes
    public static Map<Item, Integer> RSGenFuels = new HashMap<>();
    public static Map<Item, Integer> RsBeGenFuels = new HashMap<>();

    private static final float BASE_SMELT_MULTIPLIER = 0.2f;
    private static final float ACCELERATED_SMELT_MULTIPLIER = 0.1f;
    private static final float CRUSHING_MULTIPLIER = 15.0f;
    private static final float PURIFICATION_MULTIPLIER = 15.0f;
    private static final float SEPARATION_MULTIPLIER = 20.0f;
    
    public static final float ORE_SMELT_XP = 0.7f;
    public static final float INGOT_SMELT_XP = 0.1f;

    public static final float VANILLA_FURNACE_TEMP = 1800.0f;
    public static final float HT_FURNACE_TEMP = 4000.0f;

    public static final int RBE_GEN_FUEL_MULT = 3;

    /**
     * Generates ALL recipes that this index is capable of providing.
     */
    public static void generateAllRecipes(Consumer<FinishedRecipe> c)
    {
        if(gen == null) gen = new RecipeGenerator(c);

        generateOreRecipes(c);
        generateMaterialRecipes(c);
        addGeneratorFuels();

        // Copy generated E-furnace recipes over to the HT furnace
        for(String k : EFurnaceRecipes.keySet()) HTFurnaceRecipes.put(k, EFurnaceRecipes.get(k));
    }

    /**
     * Generates all ore-related recipes, including those from Vanilla Minecraft.
     */
    public static void generateOreRecipes(Consumer<FinishedRecipe> c)
    {
        if(gen == null) gen = new RecipeGenerator(c);

        // Generate recipes for modded ores
        for(OreEnum type : OreEnum.values())
        {
            // Get all item types for this ore
            String name = type.name().toLowerCase();
            Item ore = RecipeGenerator.registryToItem("ore_" + name);
            Item crushed = RecipeGenerator.registryToItem("crushed_" + name);
            Item purified = RecipeGenerator.registryToItem("purified_" + name);
            MaterialEnum product = OreProductIndex.PrimaryProducts.get(type);
            Item ingot = RecipeGenerator.registryToItem("ingot_" + product.name().toLowerCase());
            
            // Generate vanilla recipes (crafting/smelting)
            gen.ingotToDustPortable(ore, crushed);
            if(product.meltPoint < VANILLA_FURNACE_TEMP){
                gen.oreToIngotSmelting(ore, ingot, product.meltPoint * BASE_SMELT_MULTIPLIER, ORE_SMELT_XP);
                gen.dustToIngotSmelting(crushed, ingot, product.meltPoint * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP);
                gen.dustToIngotSmelting(purified, ingot, product.meltPoint * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP);
            }

            // Generate modded recipes (machines)
            MFMDRecipes.put(RecipeGenerator.getName(ore), gen.ingotToDustMachine(ore, crushed, type.hardness * CRUSHING_MULTIPLIER));
            PurifierRecipes.put(RecipeGenerator.getName(crushed), gen.crushedToPurified(crushed, purified, OreEnum.NATIVE_COPPER.hardness * PURIFICATION_MULTIPLIER));
            CentrifugeRecipes.put(RecipeGenerator.getName(purified), gen.purifiedToDust(purified, type.hardness * SEPARATION_MULTIPLIER, OreProductIndex.CentrifugeProducts.get(ore)));

            if(product.meltPoint < VANILLA_FURNACE_TEMP)
            {
                EFurnaceRecipes.put(RecipeGenerator.getName(ore), gen.dustToIngotESmelting(ore, ingot, product.meltPoint * BASE_SMELT_MULTIPLIER, ORE_SMELT_XP));
                EFurnaceRecipes.put(RecipeGenerator.getName(crushed), gen.dustToIngotESmelting(crushed, ingot, product.meltPoint * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP));
                EFurnaceRecipes.put(RecipeGenerator.getName(purified), gen.dustToIngotESmelting(purified, ingot, product.meltPoint * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP));
            }else if(product.meltPoint < HT_FURNACE_TEMP){
                HTFurnaceRecipes.put(RecipeGenerator.getName(ore), gen.dustToIngotESmelting(ore, ingot, product.meltPoint * BASE_SMELT_MULTIPLIER, ORE_SMELT_XP));
                HTFurnaceRecipes.put(RecipeGenerator.getName(crushed), gen.dustToIngotESmelting(crushed, ingot, product.meltPoint * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP));
                HTFurnaceRecipes.put(RecipeGenerator.getName(purified), gen.dustToIngotESmelting(purified, ingot, product.meltPoint * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP));
            }

            // Generate deepslate ore recipes
            if(type.hasDeepslateVariant)
            {
                Item deepOre = RecipeGenerator.registryToItem("deepslate_ore_" + name);
                gen.ingotToDustPortable(deepOre, crushed);
                MFMDRecipes.put(RecipeGenerator.getName(deepOre), gen.ingotToDustMachine(deepOre, crushed, type.hardness * CRUSHING_MULTIPLIER));

                if(product.meltPoint < VANILLA_FURNACE_TEMP) {
                    gen.oreToIngotSmelting(deepOre, ingot, product.meltPoint * BASE_SMELT_MULTIPLIER, ORE_SMELT_XP);
                    EFurnaceRecipes.put(RecipeGenerator.getName(deepOre), gen.dustToIngotESmelting(deepOre, ingot, product.meltPoint * BASE_SMELT_MULTIPLIER, ORE_SMELT_XP));
                }else if(product.meltPoint < HT_FURNACE_TEMP){
                    HTFurnaceRecipes.put(RecipeGenerator.getName(deepOre), gen.dustToIngotESmelting(deepOre, ingot, product.meltPoint * BASE_SMELT_MULTIPLIER, ORE_SMELT_XP));
                }
            }
        }
        
        // Generate recipes for vanilla ores
        generateVanillaOreRecipe("iron_ore", Items.RAW_IRON, Items.IRON_INGOT, 1798.0f, 3.0f);
        generateVanillaOreRecipe("gold_ore", Items.RAW_GOLD, Items.GOLD_INGOT, 1337.0f, 4.0f);

        // Endstone is a special case, since it only has a crushed version and a separation recipe
        Item crushed = RecipeGenerator.registryToItem("crushed_endstone");
        MFMDRecipes.put("end_stone", gen.ingotToDustMachine(Items.END_STONE, crushed, 5.0f * CRUSHING_MULTIPLIER));
        CentrifugeRecipes.put("crushed_endstone", gen.purifiedToDust(crushed, 5.0f * SEPARATION_MULTIPLIER, OreProductIndex.CentrifugeProducts.get(Items.END_STONE)));
    }

    /**
     * Generates all material-related recipes, including those from Vanilla Minecraft.
     */
    public static void generateMaterialRecipes(Consumer<FinishedRecipe> c)
    {
        if(gen == null) gen = new RecipeGenerator(c);

        for(MaterialEnum type : MaterialEnum.values())
        {
            String name = type.name().toLowerCase();
            Item ingot = RecipeGenerator.registryToItem("ingot_" + name);
            Item nugget = RecipeGenerator.registryToItem("nugget_" + name);
            Item block = RecipeGenerator.registryToItem("block_" + name);
            Item dust = RecipeGenerator.registryToItem("dust_" + name);
            Item sDust = RecipeGenerator.registryToItem("dust_small_" + name);

            // Generate vanilla recipes (crafting/smelting)
            gen.blockToIngot(block, ingot);
            gen.ingotToBlock(ingot, block);
            gen.ingotToNugget(ingot, nugget);
            gen.nuggetToIngot(nugget, ingot);
            gen.ingotToDustPortable(ingot, dust);
            gen.largeToSmallDust(dust, sDust);
            gen.smallToLargeDust(sDust, dust);

            // Generate modded recipes (machines)
            MFMDRecipes.put(RecipeGenerator.getName(ingot), gen.ingotToDustMachine(ingot, dust, type.hardness * CRUSHING_MULTIPLIER * 2.0f));

            if(type.meltPoint < VANILLA_FURNACE_TEMP) {
                gen.dustToIngotSmelting(dust, ingot, type.meltPoint * ACCELERATED_SMELT_MULTIPLIER, INGOT_SMELT_XP);
                EFurnaceRecipes.put(RecipeGenerator.getName(dust), gen.dustToIngotESmelting(dust, ingot, type.meltPoint * ACCELERATED_SMELT_MULTIPLIER, INGOT_SMELT_XP));
            }else if(type.meltPoint < HT_FURNACE_TEMP){
                HTFurnaceRecipes.put(RecipeGenerator.getName(dust), gen.dustToIngotESmelting(dust, ingot, type.meltPoint * ACCELERATED_SMELT_MULTIPLIER, INGOT_SMELT_XP));
            }
        }

        // Generate recipes for vanilla-derived materials
        generateVanillaMatRecipe("iron", Items.IRON_INGOT, 1798.0f, 1.5f, true);
        generateVanillaMatRecipe("gold", Items.GOLD_INGOT, 1337.0f, 2.0f, true);
        generateVanillaMatRecipe("coal", Items.CHARCOAL, -1.0f, 1.0f, false);

        // Generate alloying recipes
        generateAlloyRecipes("bronze", 2, MaterialEnum.BRONZE.hardness, "copper", "copper", "tin");
        generateAlloyRecipes("invar", 2, MaterialEnum.INVAR.hardness, "iron", "iron", "nickel");
    }
    
    public static void addGeneratorFuels()
    {
        // RS Generator
        RSGenFuels.put(Items.REDSTONE, 10);
        RSGenFuels.put(Items.REDSTONE_BLOCK, 90);
        RSGenFuels.put(Items.REDSTONE_ORE, 50);
        RSGenFuels.put(Items.DEEPSLATE_REDSTONE_ORE, 50);

        RsBeGenFuels.put(RecipeGenerator.registryToItem("dust_beryllium"), 40);
        RsBeGenFuels.put(RecipeGenerator.registryToItem("dust_small_beryllium"), 10);
    }

    /**
     * Gets a default Consumer which can be passed to any of the generation methods to avoid actually interfacing with the
     * Forge recipe generation system. If this is done, only the machine recipes will be generated, and all standard Forge recipes
     * will be skipped.
     */
    public static Consumer<FinishedRecipe> getDefaultConsumer()
    {
        return finishedRecipe -> {
            // This lambda intentionally left blank
        };
    }

    /**
     * Generates crushing, purification, separation, and smelting recipes for the normal and (optionally) deepslate variants
     * of a given Vanilla ore.
     */
    private static void generateVanillaOreRecipe(String name, Item ore, Item ingot, float temp, float hardness)
    {
        Item crushed = RecipeGenerator.registryToItem("crushed_" + name);
        Item purified = RecipeGenerator.registryToItem("purified_" + name);

        gen.ingotToDustPortable(ore, crushed);
        gen.dustToIngotSmelting(crushed, ingot, temp * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP);
        gen.dustToIngotSmelting(purified, ingot, temp * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP);

        MFMDRecipes.put(name, gen.ingotToDustMachine(ore, crushed, hardness * CRUSHING_MULTIPLIER));
        PurifierRecipes.put(RecipeGenerator.getName(crushed), gen.crushedToPurified(crushed, purified, OreEnum.NATIVE_COPPER.hardness * PURIFICATION_MULTIPLIER));
        CentrifugeRecipes.put(RecipeGenerator.getName(purified), gen.purifiedToDust(purified, hardness * SEPARATION_MULTIPLIER, OreProductIndex.CentrifugeProducts.get(ore)));
        EFurnaceRecipes.put(RecipeGenerator.getName(crushed), gen.dustToIngotESmelting(crushed, ingot, temp * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP));
        EFurnaceRecipes.put(RecipeGenerator.getName(purified), gen.dustToIngotESmelting(purified, ingot, temp * ACCELERATED_SMELT_MULTIPLIER, ORE_SMELT_XP));
    }

    /**
     * Generates crushing, large-to-small dust (and v/v), and re-smelting (if smeltable) recipes for a given type of Vanilla
     * ingot (or other resource). Note that the {@code temp} parameter is ignored if {@code isSmeltable = false}.
     */
    private static void generateVanillaMatRecipe(String name, Item ingot, float temp, float hardness, boolean isSmeltable)
    {
        Item dust = RecipeGenerator.registryToItem("dust_" + name);
        Item sDust = RecipeGenerator.registryToItem("dust_small_" + name);
        gen.ingotToDustPortable(ingot, dust);
        gen.largeToSmallDust(dust, sDust);
        gen.smallToLargeDust(sDust, dust);
        MFMDRecipes.put(RecipeGenerator.getName(ingot), gen.ingotToDustMachine(ingot, dust, hardness * CRUSHING_MULTIPLIER * 2.0f));

        if(isSmeltable) {
            gen.dustToIngotSmelting(dust, ingot, temp * ACCELERATED_SMELT_MULTIPLIER, INGOT_SMELT_XP);
            EFurnaceRecipes.put(RecipeGenerator.getName(dust), gen.dustToIngotESmelting(dust, ingot, temp * ACCELERATED_SMELT_MULTIPLIER, INGOT_SMELT_XP));
        }
    }

    /**
     * Generates crafting and separation recipes for the given alloy type. componentNames is a list of material names which
     * are used in crafting and yielded by separation. The first in the list is the "primary" ingredient, which will be
     * yielded at full amount by separation. All other ingredients will be divided by the number of resultant units produced
     * by crafting, and further divided by 2. If the result is fractional, it will be rounded to the nearest quarter and
     * returned as small dusts. If the result is whole, it will be returned as whole dusts.
     */
    private static void generateAlloyRecipes(String name, int count, float hardness, String... componentNames)
    {
        // Generate recipe ingredients from the names
        HashMap<String, RecipeIngredient> ingredients = new HashMap<>();
        for (String c : componentNames)
        {
            String cName = "dust_" + c;
            if(ingredients.containsKey(c))
                ingredients.put(c, new RecipeIngredient(RecipeGenerator.registryToItem(cName), ingredients.get(c).count() + 1));
            else
                ingredients.put(c, new RecipeIngredient(RecipeGenerator.registryToItem(cName), 1));
        }

        Item dust = RecipeGenerator.registryToItem("dust_" + name);
        gen.alloyRecipe(dust, count, ingredients.values().toArray(new RecipeIngredient[0]));

        // Transform the ingredient list by halving the outputs of all items except the first (primary) one.
        // Ingredients which are required in even numbers are simply halved, while odd numbered ingredients are turned
        // into twice the number of small dusts (so 3 standard ingredients would become 6 small dust, or 1.5 units).
        for(String k : ingredients.keySet())
        {
            int divisor = (k.equals(componentNames[0]) ? 1 : 2);
            int c = ingredients.get(k).count();

            if(c % (count * divisor) != 0) ingredients.put(k, new RecipeIngredient(RecipeGenerator.registryToItem("dust_small_" + k), c * 4 / (count * divisor)));
            else ingredients.put(k, new RecipeIngredient(RecipeGenerator.registryToItem("dust_" + k), c / (count * divisor)));
        }

        CentrifugeRecipes.put(RecipeGenerator.getName(dust), gen.alloySeparation(dust, hardness * SEPARATION_MULTIPLIER, ingredients.values().toArray(new RecipeIngredient[0])));
    }
}
