package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.types.IChargeableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * A depleted version of the Portable Grinder (PMD) item which may be recharged in the Charging Table.
 */
public class DepletedPortableGrinder extends Item implements IChargeableItem
{
    public DepletedPortableGrinder() {
        super(new Properties().tab(CreativeTabs.TAB_TOOLS).stacksTo(1).defaultDurability(PortableGrinder.DURABILITY / 4).setNoRepair()
                .rarity(Rarity.UNCOMMON));
    }

    /**
     * Adds or removes energy from this PMD, and returns the new item stack with the changed durability.
     * Positive values indicate depletion (discharge), negative values indicate addition (charge).
     * Any charging will result in a PMD instead of a Depleted PMD.
     */
    public ItemStack chargeDischarge(ItemStack stack, int amount, boolean checkEnergy)
    {
        if(checkEnergy)
        {
            if(amount > 0) throw new IllegalStateException("Cannot discharge a Depleted PMD!");
            else if(amount < 0 && -amount > PortableGrinder.DURABILITY)
                throw new IllegalStateException("PMD has insufficient energy capacity (has " + PortableGrinder.DURABILITY + ", needs " + -amount + ")");
        }

        super.setDamage(stack, stack.getDamageValue() + amount);

        // If this item has been charged up to 1 use (1/4 of total energy), transform it into a standard PMD, ready for use
        if(this.getDamage(stack) <= 0)
        {
            ItemStack tmp = new ItemStack(RecipeGenerator.registryToItem("portable_grinder"), stack.getCount());
            tmp.getItem().setDamage(tmp, tmp.getMaxDamage());
            ((IChargeableItem)tmp.getItem()).chargeDischarge(tmp, -this.getMaxDamage(stack), false);
            return tmp;
        }

        // Otherwise, just charge this depleted item
        return stack;
    }

    @Override
    public boolean canCharge() {
        return true;
    }

    @Override
    public boolean canDischarge() {
        return false;
    }
}
