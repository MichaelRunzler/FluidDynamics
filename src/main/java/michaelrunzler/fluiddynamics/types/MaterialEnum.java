package michaelrunzler.fluiddynamics.types;

/**
 * Provides an enum value for each processed material (elemental or alloy) for use in ingots, nuggets, blocks, armor, etc.
 */
public enum MaterialEnum
{
    COPPER(     1.5f, 12, 0.7f, 0.7f, 0.7f, 2, 20, 0.7f, 1357.8f, 401.0f, "Copper"), // Soft, not bad for armor or tools, common and easy to work with
    TIN(        0.8f, 8,  0.4f, 0.4f, 0.4f, 2, 13, 0.4f, 505.1f,  66.8f, "Tin"),  // Very bad for tools, only useful for alloys
    BERYLLIUM(  3.5f, 17, 0.8f, 1.2f, 0.7f, 3, 25, 1.1f, 1560.0f, 200.0f, "Beryllium"), // Hard, but brittle
    SILICON(    2.7f, 10, 0.2f, 0.8f, 0.2f, 1, 15, 0.2f, 1687.0f, 149.0f, "Crystalline Silicon"), // Terrible. What do you expect from crystalline silicon?
    COBALT(     4.5f, 16, 1.5f, 1.3f, 1.5f, 3, 10, 1.3f, 1768.0f, 100.0f, "Cobalt"), // A good improvement over iron, hard and durable but not that much better
    OSMIUM(     7.5f, 20, 2.0f, 2.2f, 2.0f, 4, 5,  5.0f, 3306.0f, 87.6f, "Sintered Osmium"),  // Very good; as or more effective as diamond but not as enchantable and harder to get
    ALUMINIUM(  1.3f, 10, 0.7f, 1.0f, 0.7f, 3, 20, 0.6f, 933.5f,  237.0f, "Aluminium"), // Not great due to its softness, but enchantable, like gold
    NICKEL(     3.0f, 16, 0.9f, 1.1f, 0.9f, 3, 8,  1.0f, 1728.0f, 90.9f, "Nickel"),  // Almost identical to iron but a bit more hard and brittle
    TITANIUM(   7.0f, 20, 4.0f, 2.5f, 3.0f, 4, 15, 4.0f, 1941.0f, 21.9f, "Titanium"),  // Super hard and durable, but rare and a pain to work with
    TUNGSTEN(   6.0f, 18, 0.8f, 1.5f, 0.8f, 3, 12, 2.0f, 3695.0f, 173.0f, "Sintered Tungsten"), // Hard but not that durable; also a pain because of its high melt point
    PALLADIUM(  2.0f, 12, 1.0f, 0.9f, 1.0f, 3, 35, 0.7f, 1828.1f, 71.8f, "Palladium"),  // Not as good as iron, but extremely enchantable, moreso than gold
    IRIDIUM(    8.5f, 20, 2.2f, 2.3f, 2.2f, 4, 8,  4.0f, 2719.0f, 147.0f, "Sintered Iridium"), // Like osmium, but a bit more enchantable and rare
    BRONZE(     4.5f, 17, 1.3f, 1.4f, 1.3f, 3, 16, 1.2f, 1328.0f, 80.0f, "Bronze Alloy"),  // Similar to cobalt, but a bit better at the cost of durability
    STEEL(      5.2f, 18, 1.5f, 1.6f, 1.5f, 4, 8,  2.2f, 1810.0f, 45.0f, "Carbon Steel"),  // The superior option to both bronze and cobalt, but hard to make
    INVAR(      4.0f, 17, 1.6f, 1.3f, 1.6f, 3, 2,  1.5f, 1700.0f, 13.5f, "Invar Alloy"),  // More durable than steel, but not as good and basically unenchantable
 SUPERCONDUCTOR(7.5f, 18, 1.6f, 1.5f, 1.6f, 4, 50, 1.9f, 4125.0f, 1345.0f, "Superconducting Alloy"),// A bit worse than osmium or iridium, but insanely enchantable
    SHARICITE(  30.0f,24, 5.0f, 4.0f, 4.0f, 5, 0,  100f, 8600.0f, 11.0f, "Energized Sharicite"),  // Incredibly effective and better than Osmiridium for tools, but hard to obtain and completely unenchantable
    OSMIRIDIUM( 30.0f,26, 10.0f,1.5f, 10.0f,5, 10, 1000f,4352.0f, 123.2f, "Osmiridium Superalloy"); // Even more durable than Sharicite, and makes excellent armor, but not very good as tool material

    public final float hardness;
    public final int armorVal;
    public final float armorDur;
    public final float toolMult;
    public final float toolDur;
    public final int toolTier;
    public final int enchantability;
    public final float blastResist;
    public final float meltPoint;
    public final float conductivity;
    public final String englishName;

    MaterialEnum(float hardness, int armorVal, float armorDur, float toolMult, float toolDur, int toolTier,
                 int enchantability, float blastResist, float meltPoint, float conductivity, String englishName)
    {
        this.hardness = hardness; // the absolute mining hardness of blocks made of this material; iron is 3.0
        this.armorVal = armorVal; // the absolute armor value of a full set made of this material; iron is 15 (out of 20)
        this.armorDur = armorDur; // the relative durability of armor made from this material; iron is 1.0
        this.toolMult = toolMult; // the relative effectiveness of tools made from this material; iron is 1.0
        this.toolDur = toolDur; // the relative durability of tools made from this material; iron is 1.0
        this.toolTier = toolTier; // the mining tier of the tool; iron is 3
        this.enchantability = enchantability; // the absolute enchantability factor of this material; iron is 9
        this.blastResist = blastResist; // the relative blast resistance of blocks of this material; iron is 1.0
        this.meltPoint = meltPoint; // the melting point of this material in Kelvin; iron is 1,811K
        this.conductivity = conductivity; // the conductivity of this material in Watts per Meter-Kelvin; iron is 80.4W/m*k
        this.englishName = englishName;
    }
}
