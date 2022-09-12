package michaelrunzler.fluiddynamics.types;

import net.minecraft.world.item.ItemStack;

public interface IChargeableItem
{
    ItemStack chargeDischarge(ItemStack stack, int amount, boolean checkEnergy);

    boolean canCharge();
    boolean canDischarge();
}
