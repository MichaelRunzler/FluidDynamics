package michaelrunzler.fluiddynamics.types;

/**
 * Determines what action should be taken when attempting to extract an item from a given slot via an I-sided accessor.
 * NOTHING means that extraction will always succeed.
 * CHARGE means that extraction will fail if the item is chargeable and not at max charge.
 * DISCHARGE means that extraction will fail if the item is chargeable and not fully discharged.
 */
public enum BatterySlotAction
{
    NOTHING, CHARGE, DISCHARGE
}
