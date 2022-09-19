package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.types.IChargeableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * A portable version of the Molecular Decompiler, used to dissolve blocks and items into their constituents.
 * Uses Energy Cells for power, and can be recharged via crafting.
 */
public class PortableGrinder extends Item implements IChargeableItem
{
    public static final int DURABILITY = 4;

    public PortableGrinder() {
        super(new Properties().tab(CreativeTabs.TAB_TOOLS).rarity(Rarity.UNCOMMON).setNoRepair().stacksTo(1).defaultDurability(DURABILITY));
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return chargeDischarge(itemStack.copy(), 1, false);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true; // The PMD's "container" is in fact itself
    }

    @Override
    public ItemStack chargeDischarge(ItemStack stack, int amount, boolean checkEnergy)
    {
        if(checkEnergy)
        {
            if(amount > 0) throw new IllegalStateException("Cannot discharge from a PMD!");
            else if(amount < 0 && -amount > stack.getDamageValue())
                throw new IllegalStateException("PMD has insufficient energy capacity (has " + stack.getDamageValue() + ", needs " + -amount + ")");
        }

        super.setDamage(stack, stack.getDamageValue() + amount);

        // Convert this item to an Uncharged PMD if it is out of energy
        if(this.getDamage(stack) == stack.getMaxDamage())
            return new ItemStack(RecipeGenerator.registryToItem("uncharged_portable_grinder"), stack.getCount());

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
