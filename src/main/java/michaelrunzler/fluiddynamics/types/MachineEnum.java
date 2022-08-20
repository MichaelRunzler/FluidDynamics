package michaelrunzler.fluiddynamics.types;

/**
 * Provides an easy way to get information about machine blocks and their associated tile entities.
 */
public enum MachineEnum
{
    MOLECULAR_DECOMPILER("Multi-Function Molecular Decompiler", 1.0f, "mfmd_iface", 100.0f, 5.0f);

    public final String englishName;
    public final float strength;
    public final String guiID;
    public final float powerCapacity;
    public final float powerConsumption;

    MachineEnum(String englishName, float strength, String guiID, float powerCapacity, float powerConsumption)
    {
        this.englishName = englishName;
        this.strength = strength;
        this.guiID = guiID;
        this.powerCapacity = powerCapacity;
        this.powerConsumption = powerConsumption;
    }
}