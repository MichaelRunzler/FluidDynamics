package michaelrunzler.fluiddynamics.machines.centrifuge;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.machines.purifier.PurifierBE;
import michaelrunzler.fluiddynamics.types.FDFluidStorage;
import michaelrunzler.fluiddynamics.types.FDItemHandler;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CentrifugeContainer extends MachineContainerBase
{
    public CentrifugeContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.PURIFIER, ModContainers.CONTAINER_PURIFIER.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_BATTERY, 56, 53));
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_OUTPUT, 116, 35));
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_BUCKET, 29, 17));
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_EMPTY_BUCKET, 29, 53));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);

        syncFluids();
        syncProgress();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index)
    {
        // Set convenient numeric bounds for the various parts of the inventory
        Slot s = this.slots.get(index);
        final int INV_START = PurifierBE.NUM_INV_SLOTS;
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
                if(((PurifierBE)be).isItemValid(i, s.getItem())) {
                    if(this.moveItemStackTo(s.getItem(), i, i + 1, false)) return dst;
                    else return ItemStack.EMPTY;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Synchronizes fluid tank data between the server and client by splitting it into two data slots.
     */
    @SuppressWarnings("ConstantConditions")
    private void syncFluids()
    {
        // Upper 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getFluidLevel(), true);
            }

            @Override
            public void set(int value) {
                be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(c -> ((FDFluidStorage) c).setFluidInTank(
                        0, new FluidStack(Fluids.WATER, merge16b(c.getFluidInTank(0).getAmount(), value, true))));
            }
        });

        // Lower 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getFluidLevel(), false);
            }

            @Override
            public void set(int value) {
                be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(c -> ((FDFluidStorage) c).setFluidInTank(
                        0, new FluidStack(Fluids.WATER, merge16b(c.getFluidInTank(0).getAmount(), value, false))));
            }
        });
    }

    /**
     * Synchronizes progress data between the server and client by splitting it into two data slots.
     */
    private void syncProgress()
    {
        PurifierBE mbe = (PurifierBE)be;

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
    }

    //
    // Accessors for the BE's properties
    //

    public int getProgress(){
        return ((PurifierBE)be).progress.get();
    }

    public int getMaxProgress(){
        return ((PurifierBE)be).maxProgress.get();
    }

    public int getFluidLevel(){
        return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(iFluidHandler -> iFluidHandler.getFluidInTank(0).getAmount()).orElse(0);
    }

    public int getMaxFluidLevel(){
        return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(iFluidHandler -> iFluidHandler.getTankCapacity(0)).orElse(1);
    }
}
