package michaelrunzler.fluiddynamics.machines.base;

import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.types.FDEnergyStorage;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a container instance which can be used as the basis for any {@link MachineEnum} type's container.
 */
@SuppressWarnings("SameParameterValue")
public abstract class MachineContainerBase extends AbstractContainerMenu
{
    public final MachineEnum type;
    protected final BlockEntity be;
    protected final IItemHandler playerInventory;

    protected MachineContainerBase(MachineEnum type, @Nullable MenuType<?> menuType, int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(menuType, windowID);
        this.type = type;

        be = player.getCommandSenderWorld().getBlockEntity(pos);
        this.playerInventory = new InvWrapper(inventory);

        syncPower();
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

    public int getEnergyStored(){
        return be.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int getMaxEnergy(){
        return be.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(1);
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
