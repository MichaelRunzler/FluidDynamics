package michaelrunzler.fluiddynamics.interfaces;

import michaelrunzler.fluiddynamics.block.ModBlocks;
import michaelrunzler.fluiddynamics.blockentity.MFMDBE;
import michaelrunzler.fluiddynamics.types.FDEnergyReceiver;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

public class MFMDContainer extends AbstractContainerMenu
{
    private final BlockEntity be;
    private final Player player;
    private final IItemHandler playerInventory;

    public MFMDContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(ModContainers.CONTAINER_MFMD.get(), windowID);

        be = player.getCommandSenderWorld().getBlockEntity(pos);
        this.player = player;
        this.playerInventory = new InvWrapper(inventory);

        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_BATTERY, 56, 53));
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_OUTPUT, 116, 35));
            });

        layoutPlayerInventory(8, 84);
        syncPower();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(player.getLevel(), be.getBlockPos()), player, ModBlocks.registeredBlocks.get(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase()).get());
    }

    public int getEnergyStored(){
        return be.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int getMaxEnergy(){
        return be.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(1);
    }

    public int getProgress(){
        return ((MFMDBE) be).progress;
    }

    public int getMaxProgress(){
        MFMDBE tmp = (MFMDBE)be;
        return tmp.currentRecipe == null ? 1 : tmp.currentRecipe.time;
    }

    /**
     * Synchronizes power data between the server and client by splitting it into two data slots.
     */
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
                be.getCapability(CapabilityEnergy.ENERGY).ifPresent(c -> ((FDEnergyReceiver)c).setEnergy(merge16b(c.getEnergyStored(), value, true)));
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
                be.getCapability(CapabilityEnergy.ENERGY).ifPresent(c -> ((FDEnergyReceiver)c).setEnergy(merge16b(c.getEnergyStored(), value, true)));
            }
        });
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

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventory(int col, int row)
    {
        addSlotBox(playerInventory, 9, col, row, 9, 18, 3, 18);
        row += 58;
        addSlotRange(playerInventory, 0, col, row, 9, 18);
    }

    // Gets a 16-bit value from a 32-bit value in either the upper or lower range
    private int get16b(int value, boolean upper) {
        return (upper ? (value >> 16) : value) & 0xffff;
    }

    // Merges a 16-bit value into a 32-bit value in either the upper or lower range, replacing any existing data
    private int merge16b(int value, int merge, boolean upper) {
        return (upper ? value & 0xffff0000 : value & 0x0000ffff) | (upper ? (merge << 16) : merge);
    }
}
