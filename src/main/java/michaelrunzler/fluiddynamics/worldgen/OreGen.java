package michaelrunzler.fluiddynamics.worldgen;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.block.ModBlocks;
import michaelrunzler.fluiddynamics.types.OreEnum;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
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

        for(OreEnum type : OreEnum.values())
            features.add(generateOreConfig(type));

        FluidDynamics.logModEvent(Level.DEBUG, "...done.");
    }

    private static Holder<PlacedFeature> generateOreConfig(OreEnum type)
    {
        String oreName = "ore_" + type.name().toLowerCase();

        OreConfiguration overworldConfig = new OreConfiguration(new TagMatchTest(type.canReplace), ModBlocks.registeredBlocks.get(oreName).get().defaultBlockState(), (int)(BASE_VEIN_SIZE * type.sizeModifier));
        return registerPlacedFeature("oregen_" + oreName, new ConfiguredFeature<>(Feature.ORE, overworldConfig),
                CountPlacement.of((int)(BASE_PLACEMENT_COUNT * type.rarity)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(type.minY), VerticalAnchor.absolute(type.maxY)));
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> Holder<PlacedFeature> registerPlacedFeature(String registryName, ConfiguredFeature<C, F> feature, PlacementModifier... placementModifiers) {
        return PlacementUtils.register(registryName, Holder.direct(feature), placementModifiers);
    }

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
