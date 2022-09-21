package michaelrunzler.fluiddynamics.recipes;

import michaelrunzler.fluiddynamics.types.MaterialEnum;
import michaelrunzler.fluiddynamics.types.OreEnum;
import michaelrunzler.fluiddynamics.types.RecipeIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a reference lookup to get the primary and secondary products of processing modded and Vanilla ores via
 * smelting and separation.
 */
public class OreProductIndex 
{
    public static final Map<Item, RecipeIngredient[]> CentrifugeProducts = generateCentrifugeProducts();
    public static final Map<OreEnum, MaterialEnum> PrimaryProducts = generatePrimaryProducts();
    
    private static Map<Item, RecipeIngredient[]> generateCentrifugeProducts()
    {
        Map<Item, RecipeIngredient[]> rv = new HashMap<>();

        rv.put(RecipeGenerator.registryToItem("ore_native_copper"), new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_copper"), 1)});

        rv.put(RecipeGenerator.registryToItem("ore_native_tin"), new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_tin"), 1)});

        rv.put(RecipeGenerator.registryToItem("ore_bertrandite"), new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_beryllium"), 1),
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_small_silicon"), 1)});

        rv.put(RecipeGenerator.registryToItem("ore_spherocobaltite"), new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_cobalt"), 1)});

        rv.put(RecipeGenerator.registryToItem("ore_tetrataenite"), new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_nickel"), 1),
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_small_iridium"), 1),
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_small_osmium"), 1)});

        rv.put(RecipeGenerator.registryToItem("ore_bauxite"), new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_aluminium"), 1),
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_small_titanium"), 1)});

        rv.put(RecipeGenerator.registryToItem("ore_wolframite"), new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_tungsten"), 1),
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_iron"), 1)});

        rv.put(RecipeGenerator.registryToItem("ore_pentlandite"), new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_nickel"), 1),
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_iron"), 1)});

        rv.put(Items.RAW_GOLD, new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_gold"), 1),
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_small_palladium"), 1)});

        rv.put(Items.RAW_IRON, new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_iron"), 1),
                new RecipeIngredient(RecipeGenerator.registryToItem("dust_small_nickel"), 1)});

        rv.put(Items.END_STONE, new RecipeIngredient[] {
                new RecipeIngredient(RecipeGenerator.registryToItem("nugget_rare_earth"), 1)});

        return rv;
    }

    private static Map<OreEnum, MaterialEnum> generatePrimaryProducts()
    {
        Map<OreEnum, MaterialEnum> rv = new HashMap<>();

        rv.put(OreEnum.NATIVE_COPPER, MaterialEnum.COPPER);
        rv.put(OreEnum.NATIVE_TIN, MaterialEnum.TIN);
        rv.put(OreEnum.BERTRANDITE, MaterialEnum.BERYLLIUM);
        rv.put(OreEnum.SPHEROCOBALTITE, MaterialEnum.COBALT);
        rv.put(OreEnum.TETRATAENITE, MaterialEnum.NICKEL);
        rv.put(OreEnum.BAUXITE, MaterialEnum.ALUMINIUM);
        rv.put(OreEnum.WOLFRAMITE, MaterialEnum.TUNGSTEN);
        rv.put(OreEnum.PENTLANDITE, MaterialEnum.NICKEL);

        return rv;
    }
}
