package michaelrunzler.fluiddynamics.machines.power_cell;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PowerCellBE extends PoweredMachineBE
{
    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    // Sided energy output handlers for restricted I/O
    private final FDEnergyStorage inputHandler = createDirectionalEHandler(true, false);
    private final FDEnergyStorage outputHandler = createDirectionalEHandler(false, true);
    private final LazyOptional<IEnergyStorage> EInputOpt = LazyOptional.of(() -> inputHandler);
    private final LazyOptional<IEnergyStorage> EOutputOpt = LazyOptional.of(() -> outputHandler);

    public static final int SLOT_BATTERY_IN = 0;
    public static final int SLOT_BATTERY_OUT = 1;

    public RelativeFacing relativeFacing;
    protected int nextExportDir;

    @SuppressWarnings("unchecked")
    public PowerCellBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.POWER_CELL, true, true, PowerInteraction.STORAGE);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        lastPowerState = false;

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[type.numInvSlots];
        rawHandlers = new IItemHandler[type.numInvSlots];
        for(int i = 0; i < type.numInvSlots; i++)
        {
            final int k = i;
            if(k == SLOT_BATTERY_IN) rawHandlers[k] = createStackSpecificIHandler(itemHandler, BatterySlotAction.DISCHARGE, k);
            else if(k == SLOT_BATTERY_OUT) rawHandlers[k] = createStackSpecificIHandler(itemHandler, BatterySlotAction.CHARGE, k);
            else rawHandlers[k] = createStackSpecificIHandler(itemHandler, BatterySlotAction.NOTHING, k);

            slotHandlers[k] = LazyOptional.of(() -> rawHandlers[k]);
            optionals.add(slotHandlers[k]);
        }

        nextExportDir = 0;
    }

    @Override
    public void saveToItem(@NotNull ItemStack stack) {
        // The cell should retain its energy when picked up, so ensure that we write metadata when breaking it
        BlockItem.setBlockEntityData(stack, this.getType(), this.saveWithFullMetadata());
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(side == relativeFacing.TOP || side == relativeFacing.BOTTOM) return (LazyOptional<T>)slotHandlers[SLOT_BATTERY_IN];
            else if(side == relativeFacing.LEFT || side == relativeFacing.RIGHT || side == relativeFacing.FRONT || side == relativeFacing.BACK) return (LazyOptional<T>)slotHandlers[SLOT_BATTERY_OUT];
            else return (LazyOptional<T>) itemOpt;
        }

        // In the Power Cell, energy I/O is also sided, since non-sided I/O results in energy loops
        if(cap == CapabilityEnergy.ENERGY) {
            if(side == relativeFacing.TOP || side == relativeFacing.LEFT || side == relativeFacing.BACK) return (LazyOptional<T>)EInputOpt;
            else if(side == relativeFacing.BOTTOM || side == relativeFacing.RIGHT || side == relativeFacing.FRONT) return (LazyOptional<T>)EOutputOpt;
            else return (LazyOptional<T>)energyOpt;
        }
        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        ItemStack bStack = chargeFromBattery(SLOT_BATTERY_IN, itemHandler);
        if(bStack != null) itemHandler.setStackInSlot(SLOT_BATTERY_IN, bStack);

        bStack = chargeBatteryItem(SLOT_BATTERY_OUT, itemHandler);
        if(bStack != null) itemHandler.setStackInSlot(SLOT_BATTERY_OUT, bStack);

        // Export only on directions which can export via getCapability() using the same RR algorithm used in the superclass,
        // but only trying directions which can export via sided handlers
        int startDir = nextExportDir;
        do {
            if(energyHandler.getEnergyStored() == 0) break;
            Direction d = Direction.values()[nextExportDir];
            IEnergyStorage es = this.getCapability(CapabilityEnergy.ENERGY, d).resolve().orElse(null);
            nextExportDir++;
            if(nextExportDir < 0 || nextExportDir >= Direction.values().length) nextExportDir = 0;
            if(es != null && es.canExtract() && exportToNeighbor(d)) break;
        }while(startDir != nextExportDir);

        updatePowerState(energyHandler.getEnergyStored() > 0);
    }

    /**
     * Checks if a given ItemStack is valid for a given slot.
     */
    public boolean isItemValid(int slot, @NotNull ItemStack stack)
    {
        if(slot == SLOT_BATTERY_IN) return stack.getItem() instanceof IChargeableItem chargeable && chargeable.canDischarge();
        else if(slot == SLOT_BATTERY_OUT) return stack.getItem() instanceof IChargeableItem chargeable && chargeable.canCharge();
        else return false;
    }
}
