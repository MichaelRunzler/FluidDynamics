package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * A single-use Redstone-Beryllium energy cell item which becomes depleted after use and may be recharged through crafting.
 */
public class EnergyCell extends Item
{
    public static final int DURABILITY = 1000;

    public EnergyCell() {
        super(new Properties().tab(CreativeTabs.TAB_COMPONENTS).stacksTo(1).defaultDurability(DURABILITY).setNoRepair().rarity(Rarity.COMMON));
    }

    /**
     * Adds or removes energy from this Cell, and returns the new item stack with the changed durability.
     * Positive values indicate depletion (discharge), negative values indicate addition (charge).
     * Note that the returned itemstack might be a Depleted Cell instead of a standard Cell.
     * If checkEnergy is true, an exception will be thrown if the requested energy transfer amount exceeds the cell's
     * capacity. Otherwise, any overage (overcharge or over-discharge) will be lost without any errors.
     */
    public ItemStack chargeDischarge(ItemStack stack, int amount, boolean checkEnergy)
    {
        if(checkEnergy)
        {
            if(amount > 0 && amount > (stack.getMaxDamage() - stack.getDamageValue()))
                throw new IllegalStateException("Energy cell has insufficient energy remaining (has " + (stack.getMaxDamage() - stack.getDamageValue()) + ", needs " + amount + ")");
            else if(amount < 0 && -amount > stack.getDamageValue())
                throw new IllegalStateException("Energy cell has insufficient energy capacity (has " + stack.getDamageValue() + ", needs " + -amount + ")");
        }

        super.setDamage(stack, stack.getDamageValue() + amount);

        // Convert this item to a Depleted Cell if it is out of energy
        if(this.getDamage(stack) == stack.getMaxDamage())
            return new ItemStack(RecipeGenerator.registryToItem("depleted_cell"), stack.getCount());

        return stack;
    }
}
