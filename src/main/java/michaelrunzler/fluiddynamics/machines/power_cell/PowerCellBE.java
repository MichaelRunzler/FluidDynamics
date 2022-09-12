package michaelrunzler.fluiddynamics.machines.power_cell;

import michaelrunzler.fluiddynamics.machines.base.MachineBlockEntityBase;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PowerCellBE extends MachineBlockEntityBase
{
    private final ItemStackHandler itemHandler = createIHandler();
    private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(() -> itemHandler);

    private final FDEnergyStorage energyHandler = createEHandler();
    private final LazyOptional<IEnergyStorage> energyOpt = LazyOptional.of(() -> energyHandler);

    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    private static final String ITEM_NBT_TAG = "Inventory";
    private static final String ENERGY_NBT_TAG = "Energy";
    private static final String INFO_NBT_TAG = "Info";

    public static final int NUM_INV_SLOTS = 2;
    public static final int SLOT_BATTERY_IN = 0;
    public static final int SLOT_BATTERY_OUT = 1;
    public static final int MAX_TRANSFER_RATE = 20;

    public RelativeFacing relativeFacing;
    private boolean lastPowerState; // Used to minimize state updates

    @SuppressWarnings("unchecked")
    public PowerCellBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.POWER_CELL);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        lastPowerState = false;
        optionals.add(itemOpt);
        optionals.add(energyOpt);

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[NUM_INV_SLOTS];
        rawHandlers = new IItemHandler[NUM_INV_SLOTS];
        for(int i = 0; i < NUM_INV_SLOTS; i++) {
            final int k = i;
            rawHandlers[k] = createStackSpecificIHandler(k);
            slotHandlers[k] = LazyOptional.of(() -> rawHandlers[k]);
            optionals.add(slotHandlers[k]);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        if(tag.contains(ITEM_NBT_TAG)) itemHandler.deserializeNBT(tag.getCompound(ITEM_NBT_TAG));
        if(tag.contains(ENERGY_NBT_TAG)) energyHandler.deserializeNBT(tag.get(ENERGY_NBT_TAG));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        tag.put(ITEM_NBT_TAG, itemHandler.serializeNBT());
        tag.put(ENERGY_NBT_TAG, energyHandler.serializeNBT());

        CompoundTag iTag = new CompoundTag();
        tag.put(INFO_NBT_TAG, iTag);
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
            else if(side == relativeFacing.LEFT || side == relativeFacing.RIGHT) return (LazyOptional<T>)slotHandlers[SLOT_BATTERY_OUT];
            else return (LazyOptional<T>) itemOpt;
        }

        if(cap == CapabilityEnergy.ENERGY) return (LazyOptional<T>)energyOpt;
        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        outputPower();
        chargeBatteryItems();
        acceptPower();
        updatePowerState(energyHandler.getEnergyStored() > 0);
    }

    /**
     * Gets power from a cell in the battery slot if there is one.
     */
    private void acceptPower()
    {
        if(energyHandler.getEnergyStored() >= energyHandler.getMaxEnergyStored()) return;

        // Check for an Energy Cell in the cell slot
        ItemStack batt = itemHandler.getStackInSlot(SLOT_BATTERY_IN);
        if(batt.getItem() instanceof IChargeableItem chargeable && chargeable.canDischarge() && batt.getCount() > 0)
        {
            // If there is a valid cell in the slot, check to see if we need energy, and if so, extract some from the cell
            if(energyHandler.getEnergyStored() < energyHandler.getMaxEnergyStored() && batt.getDamageValue() < batt.getMaxDamage())
            {
                // Transfer the lowest out of: remaining cell capacity, remaining storage space, or maximum charge rate
                int rcvd = energyHandler.receiveEnergy(Math.min((batt.getMaxDamage() - batt.getDamageValue()),
                        (energyHandler.getMaxEnergyStored() - energyHandler.getEnergyStored())), false);
                ItemStack tmp = chargeable.chargeDischarge(batt, rcvd, false);

                // The cell might have been depleted, so assign back the stack we get from the cell's charge/discharge
                itemHandler.setStackInSlot(SLOT_BATTERY_IN, tmp);
            }
        }
    }

    /**
     * Outputs power to adjacent energy-receptive blocks if there are any.
     */
    private void outputPower()
    {
        // For each direction surrounding the block, attempt to push up to this block's max transfer rate
        for(Direction d : Direction.values())
        {
            // Don't continue checking directions if there is no energy left to distribute
            if(energyHandler.getEnergyStored() == 0) return;

            // Grab the BE at the adjacent position
            BlockPos rel = this.worldPosition.relative(d);
            if(level == null) continue;
            BlockEntity rbe = level.getBlockEntity(rel);
            if(rbe == null) continue;

            // If the adjacent BE is another power cell, try to balance power instead of blindly transmitting it
            if(rbe instanceof PowerCellBE pbe)
            {
                // Leave a margin of 1 tick to prevent oscillating energy transfers
                if(pbe.energyHandler.getEnergyStored() + MAX_TRANSFER_RATE < this.energyHandler.getEnergyStored()){
                    int xfer = Math.min(this.energyHandler.getEnergyStored(), pbe.energyHandler.getMaxEnergyStored() - pbe.energyHandler.getEnergyStored());
                    int xfered = pbe.energyHandler.receiveEnergy(xfer, false);
                    this.energyHandler.extractEnergy(xfered, false);
                }
            }else
            {
                // See if the BE can accept power, and if so, try to push some
                rbe.getCapability(CapabilityEnergy.ENERGY).ifPresent(c ->
                {
                    if (c.canReceive()) {
                        int xfer = Math.min(this.energyHandler.getEnergyStored(), MAX_TRANSFER_RATE);
                        int xfered = c.receiveEnergy(xfer, false);
                        this.energyHandler.extractEnergy(xfered, false);
                    }
                });
            }
        }
    }

    /**
     * Attempts to charge any available battery items in the output slot.
     */
    private void chargeBatteryItems()
    {
        if(energyHandler.getEnergyStored() == 0) return;

        // Check for an Energy Cell in the cell slot
        ItemStack batt = itemHandler.getStackInSlot(SLOT_BATTERY_OUT);
        if(batt.getItem() instanceof IChargeableItem chargeable && chargeable.canCharge() && batt.getCount() > 0)
        {
            // If there is a valid cell in the slot, check to see if we need energy, and if so, extract some from the cell
            if(energyHandler.getEnergyStored() > 0 && batt.getDamageValue() > 0)
            {
                // Transfer the lowest out of: remaining cell capacity, remaining energy, or maximum charge rate
                int trns = energyHandler.extractEnergy(Math.min(batt.getDamageValue(), energyHandler.getEnergyStored()), false);
                ItemStack tmp = chargeable.chargeDischarge(batt, -trns, false);

                // The cell might have been a depleted cell which has now been charged, so assign the stack back just to be sure
                itemHandler.setStackInSlot(SLOT_BATTERY_OUT, tmp);
            }
        }
    }

    private ItemStackHandler createIHandler()
    {
        return new FDItemHandler(NUM_INV_SLOTS)
        {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                if(slot < NUM_INV_SLOTS) return PowerCellBE.this.isItemValid(slot, stack);
                else return super.isItemValid(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    /**
     * This handler will map a single accessible "slot" to an actual slot in the internal inventory handler.
     */
    private ItemStackHandler createStackSpecificIHandler(int slotID)
    {
        return new ItemStackHandler(1)
        {
            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                // Refuse to extract the item if it isn't a depleted cell
                if(slotID == SLOT_BATTERY_IN && itemHandler.getStackInSlot(slot).is(RecipeGenerator.registryToItem("energy_cell"))) return;
                itemHandler.setStackInSlot(slotID, stack);
            }

            @NotNull
            @Override
            public ItemStack getStackInSlot(int slot) {
                return itemHandler.getStackInSlot(slotID);
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return itemHandler.insertItem(slotID, stack, simulate);
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slotID == SLOT_BATTERY_IN && itemHandler.getStackInSlot(slot).is(RecipeGenerator.registryToItem("energy_cell")))
                    return ItemStack.EMPTY; // Refuse to extract the item if it isn't a depleted cell
                return itemHandler.extractItem(slotID, amount, simulate);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return itemHandler.isItemValid(slotID, stack);
            }
        };
    }

    private FDEnergyStorage createEHandler(){
        return new FDEnergyStorage(type.powerCapacity, MAX_TRANSFER_RATE);
    }

    /**
     * Updates the visual power state of the block. Only actually changes the blockstate if the given power state differs
     * from the current power state.
     */
    private void updatePowerState(boolean state)
    {
        if(state != lastPowerState){
            if(level != null) level.setBlockAndUpdate(worldPosition, this.getBlockState().setValue(BlockStateProperties.POWERED, state));
            lastPowerState = state;
        }
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
