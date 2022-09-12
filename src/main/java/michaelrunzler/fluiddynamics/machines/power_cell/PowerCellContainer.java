package michaelrunzler.fluiddynamics.machines.power_cell;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.types.FDItemHandler;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("SameParameterValue")
public class PowerCellContainer extends MachineContainerBase
{
    public PowerCellContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.POWER_CELL, ModContainers.CONTAINER_POWERCELL.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, PowerCellBE.SLOT_BATTERY_IN, 65, 59));
                addSlot(new SlotItemHandler(c, PowerCellBE.SLOT_BATTERY_OUT, 95, 59));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);

        syncPower();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index)
    {
        // Set convenient numeric bounds for the various parts of the inventory
        Slot s = this.slots.get(index);
        final int INV_START = PowerCellBE.NUM_INV_SLOTS;
        final int INV_END = INV_START + 26;
        final int HOTBAR_START = INV_END + 1;
        final int HOTBAR_END = HOTBAR_START + 8;

        // Shortcut if the requested transfer is empty
        if(!s.hasItem()) return ItemStack.EMPTY;

        // If the slot has an item in it, see which direction it's going in
        ItemStack dst = s.getItem().copy();

        // If the item is moving from the machine to the inventory, place it in the first available slot
        if(index < INV_START) {
            if(this.moveItemStackTo(s.getItem(), INV_START, HOTBAR_END + 1, false)) {
                // Propagate the change back to the BE's item handler, since this doesn't trigger onContentsChanged by default
                be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> ((FDItemHandler)c).notifyExternalChange(index));
                return dst;
            } else return ItemStack.EMPTY;
        }else{
            // If the item is moving from the inventory to the machine, see which slot it should go in
            for(int i = 0; i < INV_START; i++)
            {
                // If we found a good slot, try to move the item
                if(((PowerCellBE)be).isItemValid(i, s.getItem())) {
                    if(this.moveItemStackTo(s.getItem(), i, i + 1, false)) return dst;
                    else return ItemStack.EMPTY;
                }
            }
        }

        return ItemStack.EMPTY;
    }
}
