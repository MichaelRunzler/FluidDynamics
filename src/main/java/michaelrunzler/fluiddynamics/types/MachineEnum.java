package michaelrunzler.fluiddynamics.types;

import michaelrunzler.fluiddynamics.item.EnergyCell;

/**
 * Provides an easy way to get information about machine blocks and their associated tile entities.
 */
public enum MachineEnum
{
    MOLECULAR_DECOMPILER("Multi-Function Molecular Decompiler", 1.0f, "mfmd_iface", 2 * EnergyCell.DURABILITY, 2);

    public final String englishName;
    public final float strength;
    public final String guiID;
    public final int powerCapacity;
    public final int powerConsumption;

    MachineEnum(String englishName, float strength, String guiID, int powerCapacity, int powerConsumption)
    {
        this.englishName = englishName;
        this.strength = strength;
        this.guiID = guiID;
        this.powerCapacity = powerCapacity;
        this.powerConsumption = powerConsumption;
    }
}