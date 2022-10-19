package michaelrunzler.fluiddynamics.machines.base;

import michaelrunzler.fluiddynamics.types.FDEnergyStorage;
import michaelrunzler.fluiddynamics.types.IChargeableItem;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

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
    protected static final String ENERGY_NBT_TAG = "Energy";

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

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if(tag.contains(ENERGY_NBT_TAG)) energyHandler.deserializeNBT(tag.get(ENERGY_NBT_TAG));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ENERGY_NBT_TAG, energyHandler.serializeNBT());
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
            // If there is a valid cell in the slot, check to see if the cell needs energy
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
            // If this machine cannot export energy to the specified direction, skip to the next
            if(!this.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::canExtract).orElse(false)) continue;

            // Grab the BE at the adjacent position
            BlockPos rel = this.worldPosition.relative(d);
            if(level == null) continue;
            BlockEntity rbe = level.getBlockEntity(rel);
            if(rbe == null) continue;

            // If the adjacent BE is another power storage block, try to balance power instead of blindly transmitting it
            if(rbe instanceof PoweredMachineBE pbe && isPowerStorage && pbe.isPowerStorage)
            {
                // Respect sided-ness even with storages
                rbe.getCapability(CapabilityEnergy.ENERGY, d.getOpposite()).ifPresent(c ->
                {
                    if(c.canReceive())
                    {
                        // Balance energy based on fill levels of the two storages with a hysteresis margin to prevent looping
                        // todo shift back to a percentage-based system
                        if (c.getEnergyStored() <= this.energyHandler.getEnergyStored() - type.powerOutputRate) {
                            int xfer = Math.min(this.energyHandler.getEnergyStored(), Math.min(c.getMaxEnergyStored() - c.getEnergyStored(), type.powerOutputRate));
                            int xfered = c.receiveEnergy(xfer, false);
                            this.energyHandler.extractEnergy(xfered, false);
                        }
                    }
                });
            }else {
                // See if the BE can accept power, and if so, try to push some, ensuring that we respect sided-ness
                rbe.getCapability(CapabilityEnergy.ENERGY, d.getOpposite()).ifPresent(c ->
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

    /**
     * Creates a new Energy Handler which is tied to the internal energy storage, but is I/O restricted by the parameters
     * give here. In other words, this acts like it's the internal storage, but can have extraction or insertion restricted.
     */
    protected FDEnergyStorage createDirectionalEHandler(boolean input, boolean output)
    {
        return new FDEnergyStorage(type.powerCapacity, input ? type.powerInputRate : 0, output ? type.powerOutputRate : 0)
        {
            @Override
            public void setEnergy(int newEnergy) {
                energyHandler.setEnergy(newEnergy);
            }

            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                if(!input) return 0;
                return energyHandler.receiveEnergy(maxReceive, simulate);
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                if(!output) return 0;
                return energyHandler.extractEnergy(maxExtract, simulate);
            }

            @Override
            public int getEnergyStored() {
                return energyHandler.getEnergyStored();
            }

            @Override
            public int getMaxEnergyStored() {
                return energyHandler.getMaxEnergyStored();
            }
        };
    }
}
