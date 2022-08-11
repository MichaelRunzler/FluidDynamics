package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.block.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides tag listings for all blocks. Takes tag listings from external providers.
 */
public class FDBlockTagProvider extends BlockTagsProvider
{
    private static final HashMap<TagKey<Block>, ArrayList<ResourceKey<Block>>> mappings = new HashMap<>();

    public FDBlockTagProvider(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, FluidDynamics.MODID, helper);
    }

    @Override
    protected void addTags()
    {
        // Offload the mapping cache to the tag map and then clear the cache
        for(TagKey<Block> k : mappings.keySet())
        {
            ArrayList<ResourceKey<Block>> blocks = mappings.get(k);
            for(ResourceKey<Block> b : blocks)
                tag(k).add(b);
            blocks.clear();
        }

        mappings.clear();

        //
        // Manual tags
        //

        tag(BlockTags.COPPER_ORES).add(ModBlocks.registeredBlocks.get("ore_native_copper").get());
    }

    /**
     * Adds a new tag map that will be added on the next call to addTags() through the resource generator system.
     */
    public static void addTagMapping(TagKey<Block> tag, ResourceKey<Block> block) {
        if(!mappings.containsKey(tag)) mappings.put(tag, new ArrayList<>());
        mappings.get(tag).add(block);
    }
}
