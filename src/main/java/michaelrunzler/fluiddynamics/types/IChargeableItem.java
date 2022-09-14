package michaelrunzler.fluiddynamics.types;

import net.minecraft.world.item.ItemStack;

/**
 * Implemented by items which have an internal energy reserve that can be charged, discharged, or both.
 */
public interface IChargeableItem
{
    /**
     * Charges or discharges the item's internal storage. Positive values indicate discharging, negative values indicate
     * charging. If checkEnergy is true, the method should throw an exception if the requested power draw/input exceeds
     * the internal storage's capabilities.
     * Should return the given ItemStack after the charge/discharge is completed, even if the item is unchanged.
     */
    ItemStack chargeDischarge(ItemStack stack, int amount, boolean checkEnergy);

    boolean canCharge();
    boolean canDischarge();
}
