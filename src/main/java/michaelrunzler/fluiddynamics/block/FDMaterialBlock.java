package michaelrunzler.fluiddynamics.block;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

/**
 * Represents a generic Material block which has its properties set through the {@link MaterialEnum} enum class.
 */
public class FDMaterialBlock extends Block
{
    public MaterialEnum type;

    public FDMaterialBlock(MaterialEnum type)
    {
        super(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(type.hardness)
                .requiresCorrectToolForDrops());

        this.type = type;
    }
}

/**
 * Used to get the properties of a completed Material Block object without instantiating a Registry call.
 */
class FDMaterialBlockHelper
{
    MaterialEnum type;
    TagKey<Block>[] tags;
    String name;
    String englishName;

    FDMaterialBlockHelper(MaterialEnum type)
    {
        this.type = type;

        tags = new TagKey[]{BlockTags.MINEABLE_WITH_PICKAXE};
        this.name = "block_" + type.name().toLowerCase();
        this.englishName = type.englishName + " Block";
    }
}
