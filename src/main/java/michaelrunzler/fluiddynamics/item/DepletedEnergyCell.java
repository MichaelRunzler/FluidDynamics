package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.types.IChargeableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

/**
 * A depleted single-use Redstone-Beryllium energy cell item which may be recharged through crafting.
 */
public class DepletedEnergyCell extends Item implements IChargeableItem
{
    public DepletedEnergyCell() {
        super(new Properties().tab(CreativeTabs.TAB_COMPONENTS).stacksTo(1).defaultDurability(0).setNoRepair().rarity(Rarity.COMMON));
    }

    /**
     * Adds or removes energy from this Cell, and returns the new item stack with the changed durability.
     * Positive values indicate depletion (discharge), negative values indicate addition (charge).
     * Any charging will result in an Energy Cell instead of a Depleted Cell.
     */
    public ItemStack chargeDischarge(ItemStack stack, int amount, boolean checkEnergy)
    {
        if(checkEnergy)
        {
            if(amount > 0) throw new IllegalStateException("Cannot discharge a Depleted Cell!");
            else if(amount < 0 && -amount > EnergyCell.DURABILITY)
                throw new IllegalStateException("Energy cell has insufficient energy capacity (has " + EnergyCell.DURABILITY + ", needs " + -amount + ")");
        }

        if(amount >= 0) return stack;

        // Get a new stack which has been charged by first discharging the cell all the way, then charging it back up to the required amount
        ItemStack tmp = new ItemStack(RecipeGenerator.registryToItem("energy_cell"), stack.getCount());
        tmp.getItem().setDamage(tmp, tmp.getMaxDamage());
        ((IChargeableItem)tmp.getItem()).chargeDischarge(tmp, amount, false);

        return tmp;
    }

    @Override
    public int getDamage(ItemStack stack) {
        return EnergyCell.DURABILITY; // Since this is an "empty" cell, we need to treat it as though it is at max damage value constantly
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false; // We never want to show the durability bar on this item, since it can't be damaged or discharged
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
