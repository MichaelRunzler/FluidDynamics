package michaelrunzler.fluiddynamics.machines.purifier;

import com.mojang.authlib.GameProfile;
import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.recipes.RecipeIndex;
import michaelrunzler.fluiddynamics.types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PurifierBE extends PoweredMachineBE
{
    private final ItemStackHandler itemHandler = createIHandler();
    private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(() -> itemHandler);

    private final FDFluidStorage fluidHandler = createFHandler();
    private final LazyOptional<IFluidHandler> fluidOpt = LazyOptional.of(() -> fluidHandler);

    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    private Player dummyPlayer;

    private static final String ITEM_NBT_TAG = "Inventory";
    private static final String ENERGY_NBT_TAG = "Energy";
    private static final String FLUID_NBT_TAG = "Fluid";
    private static final String INFO_NBT_TAG = "Info";
    private static final String PROGRESS_NBT_TAG = "Progress";

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_BUCKET = 3;
    public static final int SLOT_EMPTY_BUCKET = 4;
    public static final int FLUID_CONSUMPTION_RATE = 5;
    public static final int FLUID_CAPACITY = 10000;

    public static final Map<String, GenericMachineRecipe> recipes = RecipeIndex.PurifierRecipes; // Stores all valid recipes for this machine tagged by their input item name
    public RelativeFacing relativeFacing;
    public AtomicInteger progress;
    public AtomicInteger maxProgress;
    public GenericMachineRecipe currentRecipe; // Represents the currently processing recipe in the machine
    private boolean invalidOutput; // When 'true', the ticker logic can bypass state checking and assume the output is full

    @SuppressWarnings("unchecked")
    public PurifierBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.PURIFIER, false, true, false);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        progress = new AtomicInteger(0);
        maxProgress = new AtomicInteger(1);
        currentRecipe = null;
        invalidOutput = false;
        lastPowerState = false;
        dummyPlayer = null;
        optionals.add(itemOpt);
        optionals.add(fluidOpt);

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[type.numInvSlots];
        rawHandlers = new IItemHandler[type.numInvSlots];
        for(int i = 0; i < type.numInvSlots; i++) {
            final int k = i;
            rawHandlers[k] = createStackSpecificIHandler(itemHandler, k);
            slotHandlers[k] = LazyOptional.of(() -> rawHandlers[k]);
            optionals.add(slotHandlers[k]);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        if(tag.contains(ITEM_NBT_TAG)) itemHandler.deserializeNBT(tag.getCompound(ITEM_NBT_TAG));
        if(tag.contains(ENERGY_NBT_TAG)) energyHandler.deserializeNBT(tag.get(ENERGY_NBT_TAG));
        if(tag.contains(FLUID_NBT_TAG)) fluidHandler.readFromNBT(tag.getCompound(FLUID_NBT_TAG));
        updateRecipe(itemHandler.getStackInSlot(SLOT_INPUT));
        if(tag.contains(INFO_NBT_TAG)) progress.set(tag.getCompound(INFO_NBT_TAG).getInt(PROGRESS_NBT_TAG));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        tag.put(ITEM_NBT_TAG, itemHandler.serializeNBT());
        tag.put(ENERGY_NBT_TAG, energyHandler.serializeNBT());

        CompoundTag fTag = new CompoundTag();
        fluidHandler.writeToNBT(fTag);
        tag.put(FLUID_NBT_TAG, fTag);

        CompoundTag iTag = new CompoundTag();
        iTag.putInt(PROGRESS_NBT_TAG, progress.get());
        tag.put(INFO_NBT_TAG, iTag);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        // Return appropriate inventory access wrappers for each side
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if(side == relativeFacing.TOP) return (LazyOptional<T>) slotHandlers[SLOT_INPUT];
            else if(side == relativeFacing.LEFT) return (LazyOptional<T>) slotHandlers[SLOT_BUCKET];
            else if(side == relativeFacing.RIGHT) return (LazyOptional<T>) slotHandlers[SLOT_EMPTY_BUCKET];
            else if(side == relativeFacing.BOTTOM) return (LazyOptional<T>) slotHandlers[SLOT_OUTPUT];
            else if(side == relativeFacing.FRONT || side == relativeFacing.BACK) return (LazyOptional<T>) slotHandlers[SLOT_BATTERY];
            else return (LazyOptional<T>) itemOpt;
        }

        // Energy and fluids are both accessible from all sides, so we don't need sided logic for this
        if(cap == CapabilityEnergy.ENERGY) return (LazyOptional<T>)energyOpt;
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (LazyOptional<T>)fluidOpt;
        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        // Just run power and fluid handling if no recipe is in progress
        ItemStack bStack = chargeFromBattery(SLOT_BATTERY, itemHandler);
        if(bStack != null) itemHandler.setStackInSlot(SLOT_BATTERY, bStack);
        acceptFluids();
        if(currentRecipe == null) {
            // Forcibly update power state to ensure that it remains synced
            updatePowerState(false);
            return;
        }

        boolean powered = false;

        if(progress.get() < currentRecipe.time && energyHandler.getEnergyStored() >= type.powerConsumption
           && fluidHandler.getFluidAmount() > FLUID_CONSUMPTION_RATE)
        {
            // If the current recipe is still in progress, try to consume some energy and water and advance the recipe
            energyHandler.setEnergy(energyHandler.getEnergyStored() - type.powerConsumption);
            fluidHandler.drain(FLUID_CONSUMPTION_RATE, IFluidHandler.FluidAction.EXECUTE);
            progress.incrementAndGet();
            powered = true;
        }else if(progress.get() >= currentRecipe.time) // If the current recipe is done, try to transfer the input to the output
        {
            // Shortcut the output-checking logic if we already know the output is blocking the recipe from finishing
            if(invalidOutput) return;

            ItemStack input = itemHandler.getStackInSlot(SLOT_INPUT);
            ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
            RecipeIngredient out = currentRecipe.out[0];
            boolean didOperation = false;

            if(output.getCount() == 0)
            {
                // If the output is empty, add the new item(s)
                itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(out.ingredient(), out.count()));
                didOperation = true;
            }else if (output.getCount() < output.getMaxStackSize() && output.is(out.ingredient().asItem()))
            {
                // If the output is not empty, check that the item in the output is the same and not a full stack, then
                // add the item(s) to the stack
                itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(output.getItem(), output.getCount() + out.count()));
                didOperation = true;
            }

            // If we successfully added an output, remove one item from the input
            if(didOperation) {
                itemHandler.setStackInSlot(SLOT_INPUT, new ItemStack(input.getItem(), input.getCount() - 1));
                progress.set(0);
                powered = true;
                setChanged();
            }else invalidOutput = true; // Otherwise, mark the output as invalid until we see an item change
        }

        updatePowerState(powered);
    }

    /**
     * Gets fluid (water) from a bucket in the bucket slot if there is one.
     */
    private void acceptFluids()
    {
        if(fluidHandler.getSpace() < 1000 || itemHandler.getStackInSlot(SLOT_BUCKET).isEmpty()) return;

        // Initialize a dummy player for use with the fluid system's bucket handler
        if(dummyPlayer == null)
            //noinspection ConstantConditions
            dummyPlayer = new Player(level, worldPosition, 0.0f, new GameProfile(UUID.randomUUID(), "dummy_player")) {
                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }
            };

        // Check the fluid slot for fluid-containing items
        ItemStack bucket = itemHandler.getStackInSlot(SLOT_BUCKET);
        if(fluidHandler.getSpace() >= 1000 && bucket.getItem() instanceof BucketItem b && fluidHandler.isFluidValid(new FluidStack(b.getFluid(), 1000)))
        {
            // Check to ensure we have space to place the empty container
            ItemStack output = itemHandler.getStackInSlot(SLOT_EMPTY_BUCKET);
            ItemStack result = BucketItem.getEmptySuccessItem(bucket, dummyPlayer);
            if(output.isEmpty() || (output.is(result.getItem()) && output.getCount() < output.getMaxStackSize()))
            {
                // Empty the container into the tank
                fluidHandler.fill(new FluidStack(b.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);

                // Remove one item from the input and add its resultant container to the output
                itemHandler.setStackInSlot(SLOT_BUCKET, new ItemStack(bucket.getItem(), bucket.getCount() - 1));
                if(output.isEmpty()) itemHandler.setStackInSlot(SLOT_EMPTY_BUCKET, new ItemStack(result.getItem(), result.getCount()));
                else itemHandler.setStackInSlot(SLOT_EMPTY_BUCKET, new ItemStack(output.getItem(), output.getCount() + result.getCount()));
            }
        }
    }

    /**
     * Attempts to add fluid from the given ItemStack to the internal reservoir as a result of a right-click interaction
     * from a player holding the ItemStack. Returns the resultant empty container as an ItemStack, or ItemStack.EMPTY
     * if the interaction failed (i.e. if the tank is full or the fluid is of the wrong type).
     */
    public ItemStack tryAddFluid(ItemStack incoming, Player player)
    {
        ItemStack result = ItemStack.EMPTY;
        if(fluidHandler.getSpace() >= 1000 && incoming.getItem() instanceof BucketItem b && fluidHandler.isFluidValid(new FluidStack(b.getFluid(), 1000))) {
            result = BucketItem.getEmptySuccessItem(incoming, player);
            fluidHandler.fill(new FluidStack(b.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }

        return result;
    }

    private ItemStackHandler createIHandler()
    {
        return new FDItemHandler(type.numInvSlots)
        {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                if(slot < type.numInvSlots) return PurifierBE.this.isItemValid(slot, stack);
                else return super.isItemValid(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                // Update recipe and invalidation data if relevant
                if(slot == SLOT_INPUT) updateRecipe(itemHandler.getStackInSlot(SLOT_INPUT));
                else if(slot == SLOT_OUTPUT) invalidOutput = false;
            }
        };
    }

    private FDFluidStorage createFHandler()
    {
        return new FDFluidStorage(FLUID_CAPACITY){
            @Override
            public boolean isFluidValid(FluidStack fluid){
                return fluid.getFluid().isSame(Fluids.WATER);
            }
        };
    }

    /**
     * Updates the currently-cached recipe to match the type of the given ItemStack.
     * Also updates the output-invalidation flag and the current maximum progress.
     */
    @SuppressWarnings("ConstantConditions")
    public void updateRecipe(@Nullable ItemStack stack)
    {
        // Don't update anything if the item type hasn't changed
        if(currentRecipe != null && stack != null && stack.is(currentRecipe.in.asItem())) return;

        // If the stack is empty or invalid, clear the recipe
        if(stack == null || stack.getCount() == 0 || !isItemValid(SLOT_INPUT, stack)) {
            currentRecipe = null;
            maxProgress.set(1);
        }else{
            // Otherwise, look up the item in the recipe list
            currentRecipe = recipes.get(stack.getItem().getRegistryName().getPath());
            maxProgress.set(currentRecipe == null ? 1 : currentRecipe.time);
        }

        progress.set(0);
        invalidOutput = false;
    }

    /**
     * Checks if a given ItemStack is valid for a given slot.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean isItemValid(int slot, @NotNull ItemStack stack)
    {
        if(slot == SLOT_INPUT) return recipes.containsKey(stack.getItem().getRegistryName().getPath().toLowerCase());
        else if(slot == SLOT_BATTERY) return stack.getItem() instanceof IChargeableItem chargeable && chargeable.canDischarge();
        else if(slot == SLOT_OUTPUT) return false;
        else if(slot == SLOT_BUCKET) return stack.getItem() instanceof BucketItem;
        else if(slot == SLOT_EMPTY_BUCKET) return stack.getItem() instanceof DispensibleContainerItem;
        else return false;
    }
}
