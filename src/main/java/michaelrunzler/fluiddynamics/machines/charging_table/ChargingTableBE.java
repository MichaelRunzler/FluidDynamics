package michaelrunzler.fluiddynamics.machines.charging_table;

import michaelrunzler.fluiddynamics.machines.base.MachineBlockEntityBase;
import michaelrunzler.fluiddynamics.types.IChargeableItem;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import michaelrunzler.fluiddynamics.types.RelativeFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChargingTableBE extends MachineBlockEntityBase
{
    public static final int SLOT_BATTERY_IN = 0;
    public static final int SLOT_BATTERY_OUT = 1;

    public RelativeFacing relativeFacing;

    public ChargingTableBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.CHARGING_TABLE);
        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        lastPowerState = false;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side == null) return (LazyOptional<T>) itemOpt;
        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        // Transfer power from the input to the output if possible
        ItemStack input = itemHandler.getStackInSlot(SLOT_BATTERY_IN);
        ItemStack output = itemHandler.getStackInSlot(SLOT_BATTERY_OUT);
        boolean didTransfer = false;
        if(!input.isEmpty() && !output.isEmpty() && input.getItem() instanceof IChargeableItem ich && output.getItem() instanceof IChargeableItem och)
        {
            if(ich.canDischarge() && och.canCharge() && input.getDamageValue() < input.getMaxDamage() && output.getDamageValue() > 0)
            {
                // Get the minimum of: remaining input charge, remaining output capacity, and transfer rate
                int xfer = Math.min(input.getMaxDamage() - input.getDamageValue(), Math.min(output.getDamageValue(), type.powerConsumption));
                itemHandler.setStackInSlot(SLOT_BATTERY_IN, ich.chargeDischarge(input, xfer, false));
                itemHandler.setStackInSlot(SLOT_BATTERY_OUT, och.chargeDischarge(output, -xfer, false));
                didTransfer = true;
            }
        }

        updatePowerState(didTransfer);
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
