package michaelrunzler.fluiddynamics.machines.base;

import michaelrunzler.fluiddynamics.types.FDEnergyStorage;
import michaelrunzler.fluiddynamics.types.IChargeableItem;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

/**
 * A variant of the {@link MachineBlockEntityBase} which includes various utilities for power storage and I/O handling.
 */
@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class PoweredMachineBE extends MachineBlockEntityBase
{
    public boolean canExportPower;
    public boolean canImportPower;
    protected boolean isPowerStorage;

    protected final FDEnergyStorage energyHandler;
    protected final LazyOptional<IEnergyStorage> energyOpt;

    /**
     * A variant of the standard constructor which allows specification of power import/export properties.
     */
    public PoweredMachineBE(BlockPos pos, BlockState state, MachineEnum type, boolean canExportPower, boolean canImportPower, boolean isPowerStorage)
    {
        super(pos, state, type);

        energyHandler = new FDEnergyStorage(type.powerCapacity, type.powerInputRate, type.powerOutputRate);
        energyOpt = LazyOptional.of(() -> energyHandler);
        optionals.add(energyOpt);

        this.canExportPower = canExportPower;
        this.canImportPower = canImportPower;
        this.isPowerStorage = isPowerStorage;
    }

    /**
     * @inheritDoc
     */
    public PoweredMachineBE(BlockPos pos, BlockState state, MachineEnum type) {
        this(pos, state, type, true, true, false);
    }

    /**
     * Gets power (if needed) from a discharge-able item in the given slot.
     * Returns the modified item-stack which should be re-assigned back into the slot, or NULL if no changes were made.
     */
    protected ItemStack chargeFromBattery(int slotID, IItemHandler itemHandler)
    {
        if(energyHandler.getEnergyStored() >= energyHandler.getMaxEnergyStored()) return null;

        // Check for an Energy Cell in the cell slot
        ItemStack batt = itemHandler.getStackInSlot(slotID);
        if(batt.getItem() instanceof IChargeableItem chargeable && chargeable.canDischarge() && batt.getCount() > 0)
        {
            // If there is a valid cell in the slot, check to see if we need energy, and if so, extract some from the cell
            if(energyHandler.getEnergyStored() < energyHandler.getMaxEnergyStored() && batt.getDamageValue() < batt.getMaxDamage())
            {
                // Transfer the lowest out of: remaining cell capacity, remaining storage space, or maximum charge rate
                int rcvd = energyHandler.receiveEnergy(Math.min((batt.getMaxDamage() - batt.getDamageValue()),
                        (energyHandler.getMaxEnergyStored() - energyHandler.getEnergyStored())), false);

                // The cell might have been depleted, so assign back the stack we get from the cell's charge/discharge
                return chargeable.chargeDischarge(batt, rcvd, false);
            }
        }

        return null;
    }

    /**
     * Attempts to charge an item in the given slot from the internal energy reserve.
     * Returns the modified item-stack which should be re-assigned back into the slot, or NULL if no changes were made.
     */
    protected ItemStack chargeBatteryItem(int slotID, IItemHandler itemHandler)
    {
        if(energyHandler.getEnergyStored() == 0) return null;

        // Check for an Energy Cell in the cell slot
        ItemStack batt = itemHandler.getStackInSlot(slotID);
        if(batt.getItem() instanceof IChargeableItem chargeable && chargeable.canCharge() && batt.getCount() > 0)
        {
            // If there is a valid cell in the slot, check to see if we need energy, and if so, extract some from the cell
            if(energyHandler.getEnergyStored() > 0 && batt.getDamageValue() > 0)
            {
                // Transfer the lowest out of: remaining cell capacity, remaining energy, or maximum charge rate
                int trns = energyHandler.extractEnergy(Math.min(batt.getDamageValue(), energyHandler.getEnergyStored()), false);

                // The cell might have been a depleted cell which has now been charged, so assign the stack back just to be sure
                return chargeable.chargeDischarge(batt, -trns, false);
            }
        }

        return null;
    }

    /**
     * Attempts to distribute power to neighboring powered blocks by pushing energy packets out of the internal storage.
     */
    protected void exportToNeighbors(Direction... dirs)
    {
        // For each direction surrounding the block, attempt to push up to this block's max transfer rate
        for(Direction d : dirs)
        {
            // Don't continue checking directions if there is no energy left to distribute
            if(energyHandler.getEnergyStored() == 0) return;

            // Grab the BE at the adjacent position
            BlockPos rel = this.worldPosition.relative(d);
            if(level == null) continue;
            BlockEntity rbe = level.getBlockEntity(rel);
            if(rbe == null) continue;

            // If the adjacent BE is another power storage block, try to balance power instead of blindly transmitting it
            if(rbe instanceof PoweredMachineBE pbe && isPowerStorage && pbe.isPowerStorage)
            {
                // Leave a margin of 1 tick's worth of energy to prevent oscillating energy transfers
                if(pbe.energyHandler.getEnergyStored() + type.powerOutputRate < this.energyHandler.getEnergyStored()){
                    int xfer = Math.min(this.energyHandler.getEnergyStored(), pbe.energyHandler.getMaxEnergyStored() - pbe.energyHandler.getEnergyStored());
                    int xfered = pbe.energyHandler.receiveEnergy(xfer, false);
                    this.energyHandler.extractEnergy(xfered, false);
                }
            }else {
                // See if the BE can accept power, and if so, try to push some
                rbe.getCapability(CapabilityEnergy.ENERGY).ifPresent(c ->
                {
                    if (c.canReceive()) {
                        int xfer = Math.min(this.energyHandler.getEnergyStored(), type.powerOutputRate);
                        int xfered = c.receiveEnergy(xfer, false);
                        this.energyHandler.extractEnergy(xfered, false);
                    }
                });
            }
        }
    }
}
