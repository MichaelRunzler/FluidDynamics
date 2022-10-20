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
    protected int lastDir;

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
        lastDir = 0;
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

    protected boolean exportToNeighbor(Direction d)
    {
        // If this machine cannot export energy to the specified direction, skip to the next
        if(!this.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::canExtract).orElse(false) || energyHandler.getEnergyStored() == 0) return false;

        // Grab the BE at the adjacent position
        BlockPos rel = this.worldPosition.relative(d);
        if(level == null) return false;
        BlockEntity rbe = level.getBlockEntity(rel);
        if(rbe == null) return false;

        // If the adjacent BE is another power storage block, try to balance power instead of blindly transmitting it
        if(rbe instanceof PoweredMachineBE pbe && isPowerStorage && pbe.isPowerStorage)
        {
            // Respect sided-ness even with storages
            rbe.getCapability(CapabilityEnergy.ENERGY, d.getOpposite()).ifPresent(c ->
            {
                if(c.canReceive())
                {
                    // Balance energy based on fill levels of the two storages with a hysteresis margin to prevent looping
                    float fullThis = ((float)this.energyHandler.getEnergyStored() - type.powerOutputRate) / ((float)this.energyHandler.getMaxEnergyStored());
                    float fullOther = ((float)c.getEnergyStored()) / ((float)c.getMaxEnergyStored());
                    if (fullOther < fullThis) {
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

        return true;
    }

    /**
     * Attempts to distribute power to neighboring powered blocks by pushing energy packets out of the internal storage.
     * Calls {@link #exportToNeighbor(Direction)} for all Directions surrounding the machine, stopping once all directions
     * have had an export attempted once, or the machine is out of energy.
     */
    protected void exportToNeighbors(Direction... dirs)
    {
        // For each direction surrounding the block, attempt to push up to this block's max transfer rate
        for(Direction d : dirs)
        {
            // Don't continue checking directions if there is no energy left to distribute
            if(energyHandler.getEnergyStored() == 0) return;
            exportToNeighbor(d);
        }
    }

    /**
     * Exports energy to all neighboring blocks in order, one per call, in a round-robin ordering.
     * Calls {@link #exportToNeighbor(Direction)} for the next Direction in the export order. If the Direction cannot
     * be exported to, subsequent directions will be tried until the ordering is back at the first Direction tried or the
     * machine is out of energy, at which point the routine will stop.
     */
    protected void exportToNeighborsRR()
    {
        // TODO add a flag which forces the inventory to ignore gating and empty all of its capacity (for cells only)
        int firstTried = lastDir;
        do {
            if(lastDir < 0 || lastDir >= Direction.values().length) lastDir = 0; // Loop back around to the beginning of the direction list if we hit the end
            if(energyHandler.getEnergyStored() == 0) return; // Stop trying more directions if we're out of energy
            boolean result = exportToNeighbor(Direction.values()[lastDir]); // Attempt to export to the next direction
            lastDir++;
            if(result) return; // If the export was successful, stop trying more directions
        }while(lastDir != firstTried); // If we tried all directions, stop trying until the next tick
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
