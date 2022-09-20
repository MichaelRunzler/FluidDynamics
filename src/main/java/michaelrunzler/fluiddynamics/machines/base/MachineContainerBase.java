package michaelrunzler.fluiddynamics.machines.base;

import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a container instance which can be used as the basis for any {@link MachineEnum} type's container.
 * Auto-detects if the underlying BE uses progress data and syncs said data if applicable.
 */
@SuppressWarnings("SameParameterValue")
public abstract class MachineContainerBase extends AbstractContainerMenu
{
    public final MachineEnum type;
    protected final BlockEntity be;
    protected final IItemHandler playerInventory;
    
    protected final AtomicInteger defaultProgress = new AtomicInteger(0);
    protected final AtomicInteger defaultMaxProgress = new AtomicInteger(1);

    protected MachineContainerBase(MachineEnum type, @Nullable MenuType<?> menuType, int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(menuType, windowID);
        this.type = type;

        be = player.getCommandSenderWorld().getBlockEntity(pos);
        this.playerInventory = new InvWrapper(inventory);

        syncPower();
        syncProgress();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(player.getLevel(), be.getBlockPos()), player, RecipeGenerator.registryToBlock(type.name().toLowerCase()));
    }

    /**
     * Synchronizes power data between the server and client by splitting it into two data slots.
     */
    @SuppressWarnings("ConstantConditions")
    protected void syncPower()
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
     * Synchronizes progress data between the server and client. Syncs both progress and maximum progress.
     */
    protected void syncProgress()
    {
        // Upper 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getProgress().get(), true);
            }

            @Override
            public void set(int value) {
                getProgress().set(merge16b(getProgress().get(), value, true));
            }
        });

        // Lower 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getProgress().get(), false);
            }

            @Override
            public void set(int value) {
                getProgress().set(merge16b(getProgress().get(), value, false));
            }
        });

        // Sync MaxProgress as well
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxProgress().get(), true);
            }

            @Override
            public void set(int value) {
                getMaxProgress().set(merge16b(getMaxProgress().get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxProgress().get(), false);
            }

            @Override
            public void set(int value) {
                getMaxProgress().set(merge16b(getMaxProgress().get(), value, false));
            }
        });
    }

    public int getEnergyStored(){
        return be.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int getMaxEnergy(){
        return be.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(1);
    }

    public AtomicInteger getProgress(){
        return be instanceof IProcessingBE pe ? pe.progress() : defaultProgress;
    }

    public AtomicInteger getMaxProgress(){
        return be instanceof IProcessingBE pe ? pe.maxProgress() : defaultMaxProgress;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index)
    {
        // Set convenient numeric bounds for the various parts of the inventory
        Slot s = this.slots.get(index);
        final int INV_START = be instanceof IInventoriedBE ibe ? ibe.getNumSlots() : 0;
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
                IInventoriedBE ibe = (IInventoriedBE)be;
                if(ibe.isItemValid(i, s.getItem())) {
                    if(this.moveItemStackTo(s.getItem(), i, i + 1, false)) return dst;
                    else return ItemStack.EMPTY;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    //
    // Inventory layout utilities
    //

    protected void layoutPlayerInventory(int col, int row)
    {
        addSlotBox(playerInventory, 9, col, row, 9, 18, 3, 18);
        row += 58;
        addSlotRange(playerInventory, 0, col, row, 9, 18);
    }

    /**
     * This and addSlotBox copied from McJty's 1.18 modding tutorial series.
     */
    protected int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    protected void addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy)
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
    protected int get16b(int value, boolean upper) {
        return (upper ? (value >> 16) : value) & 0x0000ffff;
    }

    // Merges a 16-bit value into a 32-bit value in either the upper or lower range, replacing any existing data
    protected int merge16b(int value, int merge, boolean upper) {
        return (upper ? value & 0x0000ffff : value & 0xffff0000) | (upper ? (merge << 16) : merge);
    }
}
