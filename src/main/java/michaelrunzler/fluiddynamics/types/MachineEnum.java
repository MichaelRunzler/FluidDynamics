package michaelrunzler.fluiddynamics.types;

import michaelrunzler.fluiddynamics.item.EnergyCell;

/**
 * Provides an easy way to get information about machine blocks and their associated tile entities.
 */
public enum MachineEnum
{
    MOLECULAR_DECOMPILER("Molecular Decompiler", 3.0f, 2 * EnergyCell.DURABILITY, 10, 0, 2, 3),
    PURIFIER("Electrogravitic Purifier", 3.0f, 2 * EnergyCell.DURABILITY, 10, 0, 2, 5),
    CENTRIFUGE("Fractionating Centrifuge", 5.0f, 4 * EnergyCell.DURABILITY, 15, 0, 4, 7),
    E_FURNACE("Resistive Casting Furnace", 3.0f, 2 * EnergyCell.DURABILITY, 10, 0, 1, 3),
    HT_FURNACE("High-Temp Blast Furnace", 8.0f, -1, -1, -1, -1, 3), // Power isn't used by this machine, since it's fueled
    POWER_CELL("Rs-Be Power Cell", 3.0f, 20000, 20, 20, -1, 2), // Power is never consumed by the cell, only stored
    RS_GENERATOR("Catalytic Redstone Generator", 4.0f, 5000, 0, 20, 10, 2); // In this case, consumption represents output, not input

    public final String englishName;
    public final float strength;
    public final int powerCapacity;
    public final int powerInputRate;
    public final int powerOutputRate;
    public final int powerConsumption;
    public final int numInvSlots;

    MachineEnum(String englishName, float strength, int powerCapacity, int powerInputRate, int powerOutputRate, int powerConsumption, int numInvSlots)
    {
        this.englishName = englishName;
        this.strength = strength;
        this.powerCapacity = powerCapacity;
        this.powerInputRate = powerInputRate;
        this.powerOutputRate = powerOutputRate;
        this.powerConsumption = powerConsumption;
        this.numInvSlots = numInvSlots;
    }
}