package michaelrunzler.fluiddynamics.machines.rbe_generator;

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

public class RsBeGenBE extends PoweredMachineBE
{
    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    private static final String INFO_NBT_TAG = "Info";
    private static final String RS_FUEL_NBT_TAG = "RsFuel";
    private static final String MAX_RS_FUEL_NBT_TAG = "MaxRsFuel";
    private static final String BE_FUEL_NBT_TAG = "BeFuel";
    private static final String MAX_BE_FUEL_NBT_TAG = "MaxBeFuel";

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FUEL_RS = 1;
    public static final int SLOT_FUEL_BE = 2;

    public RelativeFacing relativeFacing;
    public AtomicInteger rsFuel;
    public AtomicInteger maxRsFuel;
    public AtomicInteger beFuel;
    public AtomicInteger maxBeFuel;

    @SuppressWarnings("unchecked")
    public RsBeGenBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.RBE_GENERATOR, true, false, PowerInteraction.MACHINE);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        lastPowerState = false;
        optionals.add(itemOpt);

        rsFuel = new AtomicInteger(0);
        maxRsFuel = new AtomicInteger(1);
        beFuel = new AtomicInteger(0);
        maxBeFuel = new AtomicInteger(1);

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[type.numInvSlots];
        rawHandlers = new IItemHandler[type.numInvSlots];
        for(int i = 0; i < type.numInvSlots; i++) {
            final int k = i;
            rawHandlers[k] = createStackSpecificIHandler(itemHandler,  k == SLOT_BATTERY ? BatterySlotAction.CHARGE : BatterySlotAction.NOTHING, k);
            slotHandlers[k] = LazyOptional.of(() -> rawHandlers[k]);
            optionals.add(slotHandlers[k]);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        super.load(tag);
        if(tag.contains(INFO_NBT_TAG)){
            rsFuel.set(tag.getCompound(INFO_NBT_TAG).getInt(RS_FUEL_NBT_TAG));
            maxRsFuel.set(tag.getCompound(INFO_NBT_TAG).getInt(MAX_RS_FUEL_NBT_TAG));
            beFuel.set(tag.getCompound(INFO_NBT_TAG).getInt(BE_FUEL_NBT_TAG));
            maxBeFuel.set(tag.getCompound(INFO_NBT_TAG).getInt(MAX_BE_FUEL_NBT_TAG));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        super.saveAdditional(tag);
        CompoundTag iTag = new CompoundTag();
        iTag.putInt(RS_FUEL_NBT_TAG, rsFuel.get());
        iTag.putInt(MAX_RS_FUEL_NBT_TAG, maxRsFuel.get());
        iTag.putInt(BE_FUEL_NBT_TAG, beFuel.get());
        iTag.putInt(MAX_BE_FUEL_NBT_TAG, maxBeFuel.get());
        tag.put(INFO_NBT_TAG, iTag);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(side == relativeFacing.LEFT || side == relativeFacing.FRONT) return (LazyOptional<T>)slotHandlers[SLOT_FUEL_RS];
            else if(side == relativeFacing.TOP || side == relativeFacing.BOTTOM) return (LazyOptional<T>)slotHandlers[SLOT_BATTERY];
            else if(side == relativeFacing.RIGHT || side == relativeFacing.BACK) return (LazyOptional<T>)slotHandlers[SLOT_FUEL_BE];
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

        // Consume RS fuel if required
        boolean powered = false;
        if(rsFuel.get() == 0)
        {
            ItemStack fStack = itemHandler.getStackInSlot(SLOT_FUEL_RS);
            Integer fuelTime = RecipeIndex.RSGenFuels.get(fStack.getItem());
            if(fuelTime != null)
            {
                // If the fuel item is valid, consume one from the stack and update the fuel time
                fuelTime *= RecipeIndex.RBE_GEN_FUEL_MULT; // Multiply the RS values from the RS-gen to account for increased efficiency
                maxRsFuel.set(fuelTime);
                rsFuel.set(rsFuel.get() + fuelTime);
                itemHandler.setStackInSlot(SLOT_FUEL_RS, new ItemStack(fStack.getItem(), fStack.getCount() - 1));
            }else{
                // Reset max fuel time if no valid fuel item is present
                maxRsFuel.set(1);
            }
        }
        
        // Do the same for Be
        if(beFuel.get() == 0)
        {
            ItemStack fStack = itemHandler.getStackInSlot(SLOT_FUEL_BE);
            Integer fuelTime = RecipeIndex.RsBeGenFuels.get(fStack.getItem());
            if(fuelTime != null)
            {
                // If the fuel item is valid, consume one from the stack and update the fuel time
                maxBeFuel.set(fuelTime);
                beFuel.set(beFuel.get() + fuelTime);
                itemHandler.setStackInSlot(SLOT_FUEL_BE, new ItemStack(fStack.getItem(), fStack.getCount() - 1));
            }else{
                // Reset max fuel time if no valid fuel item is present
                maxBeFuel.set(1);
            }
        }

        // Generate power from the currently loaded fuel
        if(rsFuel.get() > 0  && beFuel.get() > 0 && energyHandler.getEnergyStored() + type.powerConsumption <= energyHandler.getMaxEnergyStored()){
            energyHandler.setEnergy(energyHandler.getEnergyStored() + type.powerConsumption);
            rsFuel.decrementAndGet();
            beFuel.decrementAndGet();
            powered = true;
        }

        updatePowerState(powered);
    }

    /**
     * Checks if a given ItemStack is valid for a given slot.
     */
    public boolean isItemValid(int slot, @NotNull ItemStack stack)
    {
        if(slot == SLOT_FUEL_RS) return RecipeIndex.RSGenFuels.get(stack.getItem()) != null;
        else if(slot == SLOT_FUEL_BE) return RecipeIndex.RsBeGenFuels.get(stack.getItem()) != null;
        else if(slot == SLOT_BATTERY) return stack.getItem() instanceof IChargeableItem chargeable && chargeable.canCharge();
        else return false;
    }
}
