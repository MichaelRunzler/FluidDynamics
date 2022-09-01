package michaelrunzler.fluiddynamics.block;

import michaelrunzler.fluiddynamics.types.OreEnum;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

/**
 * Represents a generic Ore block which has its properties set through the {@link OreEnum} enum class.
 */
public class FDOre extends Block
{
    public OreEnum type;

    public FDOre(OreEnum type)
    {
        super(Properties.of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(type.hardness)
                .requiresCorrectToolForDrops());

        this.type = type;
    }
}

/**
 * Used to get the properties of a completed Ore object without instantiating a Registry call.
 */
@SuppressWarnings("unchecked")
class FDOreHelper
{
    OreEnum type;
    TagKey<Block>[] tags;
    String name;
    String englishName;

    FDOreHelper(OreEnum type)
    {
        this.type = type;

        // Other properties are handled using tags, so add mappings as appropriate
        TagKey<Block> miningLevel = switch (type.miningLevel)
                {
                    case 2 -> BlockTags.NEEDS_STONE_TOOL;
                    case 3 -> BlockTags.NEEDS_IRON_TOOL;
                    case 4 -> BlockTags.NEEDS_DIAMOND_TOOL;
                    default -> null;
                };

        tags = new TagKey[]{type.miningLevel != 0 ? BlockTags.MINEABLE_WITH_PICKAXE : null, miningLevel};
        this.name = "ore_" + type.name().toLowerCase();
        this.englishName = type.englishName;
    }
}
