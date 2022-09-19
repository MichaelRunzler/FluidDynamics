package michaelrunzler.fluiddynamics.machines.redstone_generator;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.recipes.RecipeIndex;
import michaelrunzler.fluiddynamics.types.FDItemHandler;
import michaelrunzler.fluiddynamics.types.IChargeableItem;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import michaelrunzler.fluiddynamics.types.RelativeFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class RsGenBE extends PoweredMachineBE
{
    private final ItemStackHandler itemHandler = createIHandler();
    private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(() -> itemHandler);

    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    private static final String ITEM_NBT_TAG = "Inventory";
    private static final String ENERGY_NBT_TAG = "Energy";
    private static final String INFO_NBT_TAG = "Info";

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FUEL = 1;

    public RelativeFacing relativeFacing;
    private boolean lastPowerState; // Used to minimize state updates
    public AtomicInteger fuel;
    public AtomicInteger maxFuel;

    @SuppressWarnings("unchecked")
    public RsGenBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.RS_GENERATOR, true, false, false);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        lastPowerState = false;
        optionals.add(itemOpt);

        fuel = new AtomicInteger(0);
        maxFuel = new AtomicInteger(1);

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[type.numInvSlots];
        rawHandlers = new IItemHandler[type.numInvSlots];
        for(int i = 0; i < type.numInvSlots; i++) {
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

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(side == relativeFacing.TOP || side == relativeFacing.BOTTOM || side == relativeFacing.FRONT || side == relativeFacing.BACK) return (LazyOptional<T>)slotHandlers[SLOT_FUEL];
            else if(side == relativeFacing.LEFT || side == relativeFacing.RIGHT) return (LazyOptional<T>)slotHandlers[SLOT_BATTERY];
            else return (LazyOptional<T>) itemOpt;
        }

        if(cap == CapabilityEnergy.ENERGY) return (LazyOptional<T>)energyOpt;
        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        ItemStack bStack = chargeBatteryItem(SLOT_BATTERY, itemHandler);
        if(bStack != null) itemHandler.setStackInSlot(SLOT_BATTERY, bStack);

        exportToNeighbors(Direction.values());

        // Consume fuel if required
        boolean powered = false;
        if(fuel.get() == 0)
        {
            ItemStack fStack = itemHandler.getStackInSlot(SLOT_FUEL);
            Integer fuelTime = RecipeIndex.RSGenFuels.get(fStack.getItem());
            if(fuelTime != null)
            {
                // If the fuel item is valid, consume one from the stack and update the fuel time
                maxFuel.set(fuelTime);
                fuel.set(fuel.get() + fuelTime);
                itemHandler.setStackInSlot(SLOT_FUEL, new ItemStack(fStack.getItem(), fStack.getCount() - 1));
            }else{
                // Reset max fuel time if no valid fuel item is present
                maxFuel.set(1);
            }
        }

        // Generate power from the currently loaded fuel
        if(fuel.get() > 0 && energyHandler.getEnergyStored() + type.powerConsumption <= energyHandler.getMaxEnergyStored()){
            energyHandler.setEnergy(energyHandler.getEnergyStored() + type.powerConsumption);
            fuel.decrementAndGet();
            powered = true;
        }

        if(powered != lastPowerState) {
            updatePowerState(powered);
            lastPowerState = powered;
        }
    }

    private ItemStackHandler createIHandler()
    {
        return new FDItemHandler(type.numInvSlots)
        {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                if(slot < type.numInvSlots) return RsGenBE.this.isItemValid(slot, stack);
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
            public void setStackInSlot(int slot, @NotNull ItemStack stack)
            {
                // Refuse to extract the item if it is anything other than full
                if(slotID == SLOT_BATTERY && stack.getCount() == 0) {
                    ItemStack bStack = itemHandler.getStackInSlot(SLOT_BATTERY);
                    if(bStack.getItem() instanceof IChargeableItem && bStack.getDamageValue() > 0) return;
                }
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
            public ItemStack extractItem(int slot, int amount, boolean simulate)
            {
                if(slotID == SLOT_BATTERY){
                    ItemStack bStack = itemHandler.getStackInSlot(SLOT_BATTERY);
                    if(bStack.getItem() instanceof IChargeableItem && bStack.getDamageValue() > 0) return ItemStack.EMPTY;
                }
                return itemHandler.extractItem(slotID, amount, simulate);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return itemHandler.isItemValid(slotID, stack);
            }
        };
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
        if(slot == SLOT_FUEL) return RecipeIndex.RSGenFuels.get(stack.getItem()) != null;
        else if(slot == SLOT_BATTERY) return stack.getItem() instanceof IChargeableItem chargeable && chargeable.canCharge();
        else return false;
    }
}
