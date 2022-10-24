package michaelrunzler.fluiddynamics.machines.redstone_generator;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.recipes.RecipeIndex;
import michaelrunzler.fluiddynamics.types.*;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class RsGenBE extends PoweredMachineBE
{
    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    private static final String INFO_NBT_TAG = "Info";
    private static final String FUEL_NBT_TAG = "Fuel";
    private static final String MAX_FUEL_NBT_TAG = "MaxFuel";

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FUEL = 1;

    public RelativeFacing relativeFacing;
    public AtomicInteger fuel;
    public AtomicInteger maxFuel;

    @SuppressWarnings("unchecked")
    public RsGenBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.RS_GENERATOR, true, false, PowerInteraction.MACHINE);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        lastPowerState = false;

        fuel = new AtomicInteger(0);
        maxFuel = new AtomicInteger(1);

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[type.numInvSlots];
        rawHandlers = new IItemHandler[type.numInvSlots];
        for(int i = 0; i < type.numInvSlots; i++) {
            final int k = i;
            rawHandlers[k] = createStackSpecificIHandler(itemHandler, k == SLOT_BATTERY ? BatterySlotAction.CHARGE : BatterySlotAction.NOTHING, k);
            slotHandlers[k] = LazyOptional.of(() -> rawHandlers[k]);
            optionals.add(slotHandlers[k]);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        super.load(tag);
        if(tag.contains(INFO_NBT_TAG)){
            fuel.set(tag.getCompound(INFO_NBT_TAG).getInt(FUEL_NBT_TAG));
            maxFuel.set(tag.getCompound(INFO_NBT_TAG).getInt(MAX_FUEL_NBT_TAG));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        super.saveAdditional(tag);
        CompoundTag iTag = new CompoundTag();
        iTag.putInt(FUEL_NBT_TAG, fuel.get());
        iTag.putInt(MAX_FUEL_NBT_TAG, maxFuel.get());
        tag.put(INFO_NBT_TAG, iTag);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(side == relativeFacing.TOP || side == relativeFacing.BOTTOM || side == relativeFacing.FRONT || side == relativeFacing.BACK) return (LazyOptional<T>)slotHandlers[SLOT_BATTERY];
            else if(side == relativeFacing.LEFT || side == relativeFacing.RIGHT) return (LazyOptional<T>)slotHandlers[SLOT_FUEL];
            else return (LazyOptional<T>) itemOpt;
        }

        if(cap == CapabilityEnergy.ENERGY) return (LazyOptional<T>)energyOpt;
        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        ItemStack bStack = chargeBatteryItem(SLOT_BATTERY, itemHandler);
        if(bStack != null) itemHandler.setStackInSlot(SLOT_BATTERY, bStack);

        exportToNeighborsRR();

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

        updatePowerState(powered);
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
