package michaelrunzler.fluiddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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
    NATIVE_COPPER(  3.0f, 2, 2, 16,0.9f, 16, 75, OreEnumConsts.OVERWORLD, OreEnumConsts.STANDARD, "Native Copper Ore"),
    NATIVE_TIN(     2.5f, 1, 4, 12,0.8f, 16, 64, OreEnumConsts.OVERWORLD, OreEnumConsts.STANDARD, "Native Tin Ore"),
    BERTRANDITE(    3.5f, 3, 1, 6, 1.0f, 0,  48, OreEnumConsts.OVERWORLD, OreEnumConsts.STANDARD, "Bertrandite"),
    SPHEROCOBALTITE(5.0f, 3, 1, 8, 0.5f, 0,  32, OreEnumConsts.OVERWORLD, OreEnumConsts.STANDARD, "Spherocobaltite"),
    TETRATAENITE(   50.0f,4, 1, 2, 0.2f, 56, 192,OreEnumConsts.OVERWORLD, new Block[]{Blocks.STONE, Blocks.DIRT}, "Tetrataenite"),
    BAUXITE(        2.8f, 3, 2, 8, 0.6f, 0,  32, OreEnumConsts.OVERWORLD, OreEnumConsts.STANDARD, "Bauxite"),
    WOLFRAMITE(     7.0f, 4, 1, 6, 0.3f, 0,  24, new int[]{0, -1}, OreEnumConsts.STANDARD, "Wolframite"),
    PENTLANDITE(    4.5f, 3, 3, 10,0.8f, 10, 48, OreEnumConsts.OVERWORLD, OreEnumConsts.STANDARD, "Pentlandite");

    public final float hardness;
    public final int miningLevel;
    public final int minSize;
    public final int maxSize;
    public final float rarity;
    public final int minY;
    public final int maxY;
    public final int[] genDimIDs;
    public final Block[] canReplace;
    public final String englishName;

    OreEnum(float hardness, int miningLevel, int minSize, int maxSize, float rarity, int minY, int maxY,
            int[] genDimIDs, Block[] canReplace, String englishName)
    {
        this.hardness = hardness;
        this.miningLevel = miningLevel;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.rarity = rarity;
        this.minY = minY;
        this.maxY = maxY;
        this.genDimIDs = genDimIDs;
        this.canReplace = canReplace;
        this.englishName = englishName;
    }
}

class OreEnumConsts{
    static final int[] OVERWORLD = {0};
    static final Block[] STANDARD = {Blocks.STONE};
}
