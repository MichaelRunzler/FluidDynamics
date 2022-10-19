package michaelrunzler.fluiddynamics.types;

import michaelrunzler.fluiddynamics.item.EnergyCell;

/**
 * Provides an easy way to get information about machine blocks and their associated tile entities.
 */
public enum MachineEnum
{
    // Note: power I/O rates are per-side; consumption is per-tick
    MOLECULAR_DECOMPILER("Molecular Decompiler", 3.0f, 2 * EnergyCell.DURABILITY, 10, 0, 2, 3),
    PURIFIER("Electrogravitic Purifier", 3.0f, 2 * EnergyCell.DURABILITY, 10, 0, 2, 5),
    CENTRIFUGE("Fractionating Centrifuge", 5.0f, 4 * EnergyCell.DURABILITY, 15, 0, 4, 7),
    E_FURNACE("Resistive Casting Furnace", 3.0f, 2 * EnergyCell.DURABILITY, 10, 0, 1, 3),
    HT_FURNACE("High-Temp Blast Furnace", 8.0f, -1, -1, -1, -1, 3), // Power isn't used by this machine, since it's fueled
    POWER_CELL("Rs-Be Power Cell", 3.0f, 20000, 50, 50, -1, 2), // Power is never consumed by the cell, only stored
    RS_GENERATOR("Catalytic Redstone Generator", 4.0f, 5000, 0, 10, 10, 2), // In this case, consumption represents output, not input
    RBE_GENERATOR("Rs-Be Reaction Generator", 6.0f, 9000, 0, 25, 25, 3), // Same as above
    CHARGING_TABLE("Charging Table", 2.0f, 0, -1, -1, 10, 2), // Consumption is transfer in this case
    POWER_CONDUIT_BASIC("Rs-Be Power Conduit", 3.0f, 500, 25, 25, -1, 0), // Doesn't consume any power, nor does it have any inventory slots
    POWER_CONDUIT_ENHANCED("Enhanced Rs-Be Power Conduit", 3.0f, 800, 40, 40, -1, 0), // Same as above
    POWER_CONDUIT_SUPERCONDUCTING("Superconducting Rs-Be Power Conduit", 5.0f, 2000, 100, 100, -1, 0); // Capacity etc. change with cooling

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