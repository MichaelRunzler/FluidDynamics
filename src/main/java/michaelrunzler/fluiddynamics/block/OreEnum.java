package michaelrunzler.fluiddynamics.block;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Provides an enum value for each ore block that generates in the world.
 */
public enum OreEnum
{
    // Tiers are as follows:
    // 0: Bare Hand
    // 1: Wood
    // 2: Stone
    // 3: Iron/Gold
    // 4: Diamond/Netherite
    // Iron Ore has the following properties:
    // (3.0, 2, 2, 16, 1.0, 0, 64, OVERWORLD, STANDARD)
    NATIVE_COPPER(  3.0f, 2, 1.0f, 0.8f, 0, 64, BlockTags.STONE_ORE_REPLACEABLES, "Native Copper Ore"),
    NATIVE_TIN(     2.5f, 1, 1.0f, 0.7f, 0, 80, BlockTags.STONE_ORE_REPLACEABLES, "Native Tin Ore"),
    BERTRANDITE(    3.5f, 2, 1.0f, 1.0f, 0, 48, BlockTags.STONE_ORE_REPLACEABLES, "Bertrandite"),
    SPHEROCOBALTITE(5.0f, 3, 1.0f, 0.4f, 0, 32, BlockTags.STONE_ORE_REPLACEABLES, "Spherocobaltite"),
    TETRATAENITE(   50.0f,4, 1.0f, 0.2f, -128,192, BlockTags.DIRT, "Tetrataenite"),
    BAUXITE(        2.8f, 3, 2.0f, 0.4f, 0, 32, BlockTags.STONE_ORE_REPLACEABLES, "Bauxite"),
    WOLFRAMITE(     7.0f, 4, 1.0f, 0.3f, 0, 24, BlockTags.STONE_ORE_REPLACEABLES, "Wolframite"),
    PENTLANDITE(    4.5f, 2, 1.0f, 0.5f, 0, 48, BlockTags.STONE_ORE_REPLACEABLES, "Pentlandite");

    public final float hardness;
    public final int miningLevel;
    public final float sizeModifier;
    public final float rarity;
    public final int minY;
    public final int maxY;
    public final TagKey<Block> canReplace;
    public final String englishName;

    OreEnum(float hardness, int miningLevel, float sizeModifier, float rarity, int minY, int maxY,
            TagKey<Block> canReplace, String englishName)
    {
        this.hardness = hardness;
        this.miningLevel = miningLevel;
        this.sizeModifier = sizeModifier;
        this.rarity = rarity;
        this.minY = minY;
        this.maxY = maxY;
        this.canReplace = canReplace;
        this.englishName = englishName;
    }
}
