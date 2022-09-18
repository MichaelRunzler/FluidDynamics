package michaelrunzler.fluiddynamics.types;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Provides an interface for other classes to access BEs with one or more inventory slots.
 */
public interface IInventoriedBE
{
    boolean isItemValid(int slot, @NotNull ItemStack stack);

    int getNumSlots();
}
