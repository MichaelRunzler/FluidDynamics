package michaelrunzler.fluiddynamics.types;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A specialized subclass of the ItemHandler which is designed to deal with battery slots.
 * It prevents extraction of non-full battery items from any designated slots.
 */
public class FDEnergyItemHandler extends FDItemHandler
{
    protected int[] batterySlots;

    public FDEnergyItemHandler(int slots, int... batterySlots)
    {
        super(slots);
        this.batterySlots = batterySlots;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        for(int i : batterySlots) if(i == slot && stack.getItem() instanceof IChargeableItem && stack.getDamageValue() > 0) return;
        super.setStackInSlot(slot, stack);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        for(int i : batterySlots) {
            if(i == slot) {
                ItemStack stack = this.getStackInSlot(slot);
                if(this.getStackInSlot(slot).getItem() instanceof IChargeableItem && stack.getDamageValue() > 0) return ItemStack.EMPTY;
            }
        }

        return super.extractItem(slot, amount, simulate);
    }
}
