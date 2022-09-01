package michaelrunzler.fluiddynamics.machines.MFMD;

import michaelrunzler.fluiddynamics.block.ModBlocks;
import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.types.FDEnergyStorage;
import michaelrunzler.fluiddynamics.types.FDItemHandler;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("SameParameterValue")
public class MFMDContainer extends AbstractContainerMenu
{
    private final BlockEntity be;
    private final IItemHandler playerInventory;

    public MFMDContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(ModContainers.CONTAINER_MFMD.get(), windowID);

        be = player.getCommandSenderWorld().getBlockEntity(pos);
        this.playerInventory = new InvWrapper(inventory);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_BATTERY, 56, 53));
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_OUTPUT, 116, 35));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);
        syncPower();
        syncProgress();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(player.getLevel(), be.getBlockPos()), player, ModBlocks.registeredBlocks.get(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase()).get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index)
    {
        // Set convenient numeric bounds for the various parts of the inventory
        Slot s = this.slots.get(index);
        final int INV_START = MFMDBE.NUM_INV_SLOTS;
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
                if(((MFMDBE)be).isItemValid(i, s.getItem())) {
                    if(this.moveItemStackTo(s.getItem(), i, i + 1, false)) return dst;
                    else return ItemStack.EMPTY;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Synchronizes power data between the server and client by splitting it into two data slots.
     */
    @SuppressWarnings("ConstantConditions")
    private void syncPower()
    {
        // Upper 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getEnergyStored(), true);
            }

            @Override
            public void set(int value) {
                be.getCapability(CapabilityEnergy.ENERGY).ifPresent(c -> ((FDEnergyStorage)c).setEnergy(merge16b(c.getEnergyStored(), value, true)));
            }
        });

        // Lower 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getEnergyStored(), false);
            }

            @Override
            public void set(int value) {
                be.getCapability(CapabilityEnergy.ENERGY).ifPresent(c -> ((FDEnergyStorage)c).setEnergy(merge16b(c.getEnergyStored(), value, false)));
            }
        });
    }

    /**
     * Synchronizes progress data between the server and client by splitting it into two data slots.
     */
    private void syncProgress()
    {
        MFMDBE mbe = (MFMDBE)be;

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

    public int getEnergyStored(){
        return be.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int getMaxEnergy(){
        return be.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(1);
    }

    public int getProgress(){
        return ((MFMDBE) be).progress.get();
    }

    public int getMaxProgress(){
        return ((MFMDBE)be).maxProgress.get();
    }

    //
    // Inventory layout utilities
    //

    private void layoutPlayerInventory(int col, int row)
    {
        addSlotBox(playerInventory, 9, col, row, 9, 18, 3, 18);
        row += 58;
        addSlotRange(playerInventory, 0, col, row, 9, 18);
    }

    /**
     * This and addSlotBox copied from McJty's 1.18 modding tutorial series.
     */
    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private void addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy)
    {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
    }

    //
    // Bitwise utilities
    //

    // Gets a 16-bit value from a 32-bit value in either the upper or lower range
    private int get16b(int value, boolean upper) {
        return (upper ? (value >> 16) : value) & 0x0000ffff;
    }

    // Merges a 16-bit value into a 32-bit value in either the upper or lower range, replacing any existing data
    private int merge16b(int value, int merge, boolean upper) {
        return (upper ? value & 0x0000ffff : value & 0xffff0000) | (upper ? (merge << 16) : merge);
    }
}
