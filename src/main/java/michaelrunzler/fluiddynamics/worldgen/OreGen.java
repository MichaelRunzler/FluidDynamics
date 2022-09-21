package michaelrunzler.fluiddynamics.worldgen;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.types.OreEnum;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import org.slf4j.event.Level;

import java.util.ArrayList;

/**
 * Manages oregen configurations.
 */
public class OreGen
{
    private static final ArrayList<Holder<PlacedFeature>> features = new ArrayList<>();
    private static final float BASE_PLACEMENT_COUNT = 20.0f;
    private static final float BASE_VEIN_SIZE = 4.0f;

    public static void registerGeneratedFeatures()
    {
        FluidDynamics.logModEvent(Level.DEBUG, "Started generating oregen config...");

        for(OreEnum type : OreEnum.values()) {
            if(type.maxY > 0) features.add(generateOreConfig(type));
            if(type.minY < 0) features.add(generateDeepOreConfig(type, type.hasDeepslateVariant));
        }

        FluidDynamics.logModEvent(Level.DEBUG, "...done.");
    }

    private static Holder<PlacedFeature> generateOreConfig(OreEnum type)
    {
        String oreName = "ore_" + type.name().toLowerCase();

        // Limit placement of this ore to the "top half" of the underground area (not deepslate)
        OreConfiguration overworldConfig = new OreConfiguration(new TagMatchTest(type.canReplace), RecipeGenerator.registryToBlock(oreName).defaultBlockState(), (int)(BASE_VEIN_SIZE * type.sizeModifier));
        return registerPlacedFeature("oregen_" + oreName, new ConfiguredFeature<>(Feature.ORE, overworldConfig),
                CountPlacement.of((int)(BASE_PLACEMENT_COUNT * type.rarity)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(Math.max(0, type.minY)), VerticalAnchor.absolute(Math.max(0, type.maxY))));
    }

    private static Holder<PlacedFeature> generateDeepOreConfig(OreEnum type, boolean hasDeepVariant)
    {
        String oreName = (hasDeepVariant ? "deepslate_ore_" : "ore_") + type.name().toLowerCase();

        // Limit placement of this ore to the "bottom half" of the underground area (deepslate)
        OreConfiguration deepslateConfig = new OreConfiguration(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                RecipeGenerator.registryToBlock(oreName).defaultBlockState(), (int)(BASE_VEIN_SIZE * type.sizeModifier));
        return registerPlacedFeature("oregen_" + (hasDeepVariant ? oreName : ("deepslate_" + oreName)),
                new ConfiguredFeature<>(Feature.ORE, deepslateConfig),
                CountPlacement.of((int)(BASE_PLACEMENT_COUNT * type.rarity / 2)), // Deepslate has far more exposed stone than the upper layers, so we reduce the spawn rate to compensate
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(Math.min(0, type.minY)), VerticalAnchor.absolute(Math.min(0, type.maxY))));
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> Holder<PlacedFeature> registerPlacedFeature(String registryName, ConfiguredFeature<C, F> feature, PlacementModifier... placementModifiers) {
        return PlacementUtils.register(registryName, Holder.direct(feature), placementModifiers);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static void onBiomeLoadingEvent(BiomeLoadingEvent event)
    {
        if (event.getCategory() == Biome.BiomeCategory.NETHER) {
            // Add Nether gen here
        } else if (event.getCategory() == Biome.BiomeCategory.THEEND) {
            // Add End gen here
        } else {
            // Add Overworld gen here
            for(Holder<PlacedFeature> f : features)
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, f);
        }
    }
}
