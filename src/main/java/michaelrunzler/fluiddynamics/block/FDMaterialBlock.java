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

    private static final float BASE_DESTROY_TIME = 3.0f;
    private static final float BASE_STRENGTH = 3.0f;
    private static final float BASE_RESISTANCE = 3.0f;

    public FDMaterialBlock(MaterialEnum type)
    {
        super(Properties.of(Material.METAL));

        this.type = type;

        super.properties.sound(SoundType.METAL);
        super.properties.strength(BASE_STRENGTH * type.hardness);
        super.properties.explosionResistance(BASE_RESISTANCE * type.hardness);
        super.properties.destroyTime(BASE_DESTROY_TIME * type.hardness);
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
