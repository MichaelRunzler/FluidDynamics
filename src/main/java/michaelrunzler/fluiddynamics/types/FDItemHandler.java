package michaelrunzler.fluiddynamics.types;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * A variant of the {@link ItemStackHandler} which supports external notification of item update, for example, from
 * code which uses {@link net.minecraft.world.inventory.AbstractContainerMenu}'s moveItemStackTo(), which doesn't obey
 * the API spec for ItemHandler access and thus doesn't call {@link ItemStackHandler#onContentsChanged(int)} when
 * stacks are changed.
 */
@SuppressWarnings("unused")
public class FDItemHandler extends ItemStackHandler
{
    public FDItemHandler(){
        super();
    }

    public FDItemHandler(int slots){
        super(slots);
    }

    public FDItemHandler(NonNullList<ItemStack> stacks){
        super(stacks);
    }

    /**
     * Notifies the handler that an inventory slot's contents may have changed outside the usual {@link ItemStackHandler#onContentsChanged(int)}
     * structure. The behavior of this method defaults to delegating to the standard mechanism as above. Note that there
     * is no guarantee that any action will be taken as a result of this method call, nor is there a guarantee that any
     * items will have actually changed.
     * @param slot the slot to notify changes to.
     */
    public void notifyExternalChange(int slot){
        this.onContentsChanged(slot);
    }
}
