package michaelrunzler.fluiddynamics.machines.ht_furnace;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.types.FDItemHandler;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("SameParameterValue")
public class HTFurnaceContainer extends MachineContainerBase
{
    public HTFurnaceContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.HT_FURNACE, ModContainers.CONTAINER_HTFURNACE.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, HTFurnaceBE.SLOT_FUEL, 56, 53));
                addSlot(new SlotItemHandler(c, HTFurnaceBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, HTFurnaceBE.SLOT_OUTPUT, 116, 35));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);

        syncPower();
        syncProgress();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index)
    {
        // Set convenient numeric bounds for the various parts of the inventory
        Slot s = this.slots.get(index);
        final int INV_START = HTFurnaceBE.NUM_INV_SLOTS;
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
                if(((HTFurnaceBE)be).isItemValid(i, s.getItem())) {
                    if(this.moveItemStackTo(s.getItem(), i, i + 1, false)) return dst;
                    else return ItemStack.EMPTY;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Synchronizes progress data between the server and client by splitting it into two data slots.
     */
    private void syncProgress()
    {
        HTFurnaceBE mbe = (HTFurnaceBE) be;

        // Upper 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getProgress(), true);
            }

            @Override
            public void set(int value) {
                mbe.progress.set(merge16b(mbe.progress.get(), value, true));
            }
        });

        // Lower 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getProgress(), false);
            }

            @Override
            public void set(int value) {
                mbe.progress.set(merge16b(mbe.progress.get(), value, false));
            }
        });

        // Sync MaxProgress as well
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxProgress(), true);
            }

            @Override
            public void set(int value) {
                mbe.maxProgress.set(merge16b(mbe.maxProgress.get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxProgress(), false);
            }

            @Override
            public void set(int value) {
                mbe.maxProgress.set(merge16b(mbe.maxProgress.get(), value, false));
            }
        });

        // And fuel/max fuel
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getFuel(), true);
            }

            @Override
            public void set(int value) {
                mbe.fuel.set(merge16b(mbe.fuel.get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getFuel(), false);
            }

            @Override
            public void set(int value) {
                mbe.fuel.set(merge16b(mbe.fuel.get(), value, false));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxFuel(), true);
            }

            @Override
            public void set(int value) {
                mbe.maxFuel.set(merge16b(mbe.maxFuel.get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxFuel(), false);
            }

            @Override
            public void set(int value) {
                mbe.maxFuel.set(merge16b(mbe.maxFuel.get(), value, false));
            }
        });
    }

    //
    // Accessors for the BE's properties
    //

    public int getProgress(){
        return ((HTFurnaceBE) be).progress.get();
    }

    public int getMaxProgress(){
        return ((HTFurnaceBE)be).maxProgress.get();
    }

    public int getFuel(){
        return ((HTFurnaceBE)be).fuel.get();
    }

    public int getMaxFuel(){
        return ((HTFurnaceBE)be).maxFuel.get();
    }
}
