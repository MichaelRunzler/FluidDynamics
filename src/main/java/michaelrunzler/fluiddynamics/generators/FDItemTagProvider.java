package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Works just like the {@link FDBlockTagProvider}, but for items.
 */
public class FDItemTagProvider extends ItemTagsProvider
{
    private static final HashMap<TagKey<Item>, ArrayList<ResourceKey<Item>>> mappings = new HashMap<>();

    public FDItemTagProvider(DataGenerator gen, BlockTagsProvider blocks, ExistingFileHelper helper) {
        super(gen, blocks, FluidDynamics.MODID, helper);
    }

    @Override
    protected void addTags()
    {
        // Offload the mapping cache to the tag map and then clear the cache
        for(TagKey<Item> k : mappings.keySet())
        {
            ArrayList<ResourceKey<Item>> items = mappings.get(k);
            for(ResourceKey<Item> i : items)
                tag(k).add(i);
            items.clear();
        }

        mappings.clear();

        //
        // Manual tags
        //

        tag(ItemTags.COPPER_ORES).add(RecipeGenerator.registryToItem("ore_native_copper"));
        tag(ItemTags.PIGLIN_LOVED).add(RecipeGenerator.registryToItem("ingot_palladium"));
    }

    /**
     * Adds a new tag map that will be added on the next call to addTags() through the resource generator system.
     */
    public static void addTagMapping(TagKey<Item> tag, ResourceKey<Item> item) {
        if(!mappings.containsKey(tag)) mappings.put(tag, new ArrayList<>());
        mappings.get(tag).add(item);
    }
}
