package michaelrunzler.fluiddynamics.types;

import michaelrunzler.fluiddynamics.item.EnergyCell;

/**
 * Provides an easy way to get information about machine blocks and their associated tile entities.
 */
public enum MachineEnum
{
    MOLECULAR_DECOMPILER("Molecular Decompiler", 3.0f, 2 * EnergyCell.DURABILITY, 2),
    PURIFIER("Electrogravitic Purifier", 3.0f, 2 * EnergyCell.DURABILITY, 2),
    CENTRIFUGE("Fractionating Centrifuge", 5.0f, 4 * EnergyCell.DURABILITY, 4),
    E_FURNACE("Resistive Casting Furnace", 3.0f, 2 * EnergyCell.DURABILITY, 1),
    HT_FURNACE("High-Temp Blast Furnace", 8.0f, -1, -1); // Power isn't used by this machine, since it's fueled

    public final String englishName;
    public final float strength;
    public final int powerCapacity;
    public final int powerConsumption;

    MachineEnum(String englishName, float strength, int powerCapacity, int powerConsumption)
    {
        this.englishName = englishName;
        this.strength = strength;
        this.powerCapacity = powerCapacity;
        this.powerConsumption = powerConsumption;
    }
}