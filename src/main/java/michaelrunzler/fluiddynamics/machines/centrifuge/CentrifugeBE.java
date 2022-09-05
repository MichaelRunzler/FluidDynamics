package michaelrunzler.fluiddynamics.machines.centrifuge;

import com.mojang.authlib.GameProfile;
import michaelrunzler.fluiddynamics.item.EnergyCell;
import michaelrunzler.fluiddynamics.item.ModItems;
import michaelrunzler.fluiddynamics.machines.base.MachineBlockEntityBase;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CentrifugeBE extends MachineBlockEntityBase
{
    private final ItemStackHandler itemHandler = createIHandler();
    private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(() -> itemHandler);

    private final FDEnergyStorage energyHandler = createEHandler();
    private final LazyOptional<IEnergyStorage> energyOpt = LazyOptional.of(() -> energyHandler);

    private final FDFluidStorage fluidHandler = createFHandler();
    private final LazyOptional<IFluidHandler> fluidOpt = LazyOptional.of(() -> fluidHandler);

    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    private final IItemHandler outputHandler; // Handles all output slots
    private final LazyOptional<IItemHandler> outputOpt;

    private Player dummyPlayer;

    private static final String ITEM_NBT_TAG = "Inventory";
    private static final String ENERGY_NBT_TAG = "Energy";
    private static final String FLUID_NBT_TAG = "Fluid";
    private static final String INFO_NBT_TAG = "Info";
    private static final String PROGRESS_NBT_TAG = "Progress";

    public static final int NUM_INV_SLOTS = 7;
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_BUCKET = 2;
    public static final int SLOT_EMPTY_BUCKET = 3;
    public static final int SLOT_OUTPUT_1 = 4;
    public static final int SLOT_OUTPUT_2 = 5;
    public static final int SLOT_OUTPUT_3 = 6;
    public static final int MAX_CHARGE_RATE = 15;
    public static final int FLUID_CONSUMPTION_RATE = 1;
    public static final int FLUID_CAPACITY = 4000;

    public static final HashMap<String, GenericMachineRecipe> recipes = addRecipes(); // Stores all valid recipes for this machine tagged by their input item name
    public RelativeFacing relativeFacing;
    public AtomicInteger progress;
    public AtomicInteger maxProgress;
    public GenericMachineRecipe currentRecipe; // Represents the currently processing recipe in the machine
    private boolean invalidOutput; // When 'true', the ticker logic can bypass state checking and assume the output is full
    private boolean lastPowerState; // Used to minimize state updates

    @SuppressWarnings("unchecked")
    public CentrifugeBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.CENTRIFUGE);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        progress = new AtomicInteger(0);
        maxProgress = new AtomicInteger(1);
        currentRecipe = null;
        invalidOutput = false;
        lastPowerState = false;
        dummyPlayer = null;
        optionals.add(itemOpt);
        optionals.add(energyOpt);
        optionals.add(fluidOpt);

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[NUM_INV_SLOTS];
        rawHandlers = new IItemHandler[NUM_INV_SLOTS];
        for(int i = 0; i < NUM_INV_SLOTS; i++) {
            final int k = i;
            rawHandlers[k] = createStackSpecificIHandler(k);
            slotHandlers[k] = LazyOptional.of(() -> rawHandlers[k]);
            optionals.add(slotHandlers[k]);
        }

        outputHandler = createMultiStackSpecificIHandler(SLOT_OUTPUT_1, SLOT_OUTPUT_2, SLOT_OUTPUT_3);
        outputOpt = LazyOptional.of(() -> outputHandler);
        optionals.add(outputOpt);
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
            else if(side == relativeFacing.BOTTOM) return (LazyOptional<T>) outputOpt;
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
        acceptPower();
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

            // Check each output slot for validity; if all pass the checks, proceed with the transfer
            int k = 0;
            boolean allValid = true;
            for(int i = SLOT_OUTPUT_1; i <= SLOT_OUTPUT_3; i++)
            {
                if(k < currentRecipe.out.length)
                {
                    ItemStack output = itemHandler.getStackInSlot(i);
                    RecipeComponent out = currentRecipe.out[k];
                    if (!output.isEmpty() && (output.getCount() >= output.getMaxStackSize() || !output.is(out.ingredient().asItem()))) allValid = false;
                }

                k++;
            }

            // Transfer the input to the output slots
            boolean didOperation = false;
            if(allValid)
            {
                k = 0;
                for(int i = SLOT_OUTPUT_1; i <= SLOT_OUTPUT_3; i++)
                {
                    if(k < currentRecipe.out.length)
                    {
                        ItemStack output = itemHandler.getStackInSlot(i);
                        RecipeComponent out = currentRecipe.out[k];
                        if (output.isEmpty()) itemHandler.setStackInSlot(i, new ItemStack(out.ingredient(), out.count()));
                        else itemHandler.setStackInSlot(i, new ItemStack(output.getItem(), output.getCount() + out.count()));
                    }

                    k++;
                }

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
     * Gets power from a cell in the battery slot if there is one.
     */
    private void acceptPower()
    {
        AtomicInteger capacity = new AtomicInteger(energyHandler.getEnergyStored());
        if(capacity.get() >= energyHandler.getMaxEnergyStored()) return;

        // Check for an Energy Cell in the cell slot
        ItemStack batt = itemHandler.getStackInSlot(SLOT_BATTERY);
        if(batt.is(ModItems.registeredItems.get("energy_cell").get()) && batt.getCount() > 0)
        {
            // If there is a valid cell in the slot, check to see if we need energy, and if so, extract some from the cell
            if(energyHandler.getEnergyStored() < energyHandler.getMaxEnergyStored() && batt.getDamageValue() < batt.getMaxDamage())
            {
                // Transfer the lowest out of: remaining cell capacity, remaining storage space, or maximum charge rate
                int rcvd = energyHandler.receiveEnergy(Math.min((batt.getMaxDamage() - batt.getDamageValue()),
                        (energyHandler.getMaxEnergyStored() - energyHandler.getEnergyStored())), false);
                ItemStack tmp = ((EnergyCell)batt.getItem()).chargeDischarge(batt, rcvd, false);

                // The cell might have been depleted, so assign back the stack we get from the cell's charge/discharge
                itemHandler.setStackInSlot(SLOT_BATTERY, tmp);
            }
        }
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
        return new FDItemHandler(NUM_INV_SLOTS)
        {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                if(slot < NUM_INV_SLOTS) return CentrifugeBE.this.isItemValid(slot, stack);
                else return super.isItemValid(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                // Update recipe and invalidation data if relevant
                if(slot == SLOT_INPUT) updateRecipe(itemHandler.getStackInSlot(SLOT_INPUT));
                else if(slot == SLOT_OUTPUT_1 || slot == SLOT_OUTPUT_2 || slot == SLOT_OUTPUT_3) invalidOutput = false;
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
                if(slotID == SLOT_BATTERY && itemHandler.getStackInSlot(slot).is(ModItems.registeredItems.get("energy_cell").get())) return;
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
                if(slotID == SLOT_BATTERY && itemHandler.getStackInSlot(slot).is(ModItems.registeredItems.get("energy_cell").get()))
                    return ItemStack.EMPTY; // Refuse to extract the item if it isn't a depleted cell
                return itemHandler.extractItem(slotID, amount, simulate);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return itemHandler.isItemValid(slotID, stack);
            }
        };
    }

    /**
     * This handler will map multiple accessible "slots" to actual slots in the internal inventory handler.
     */
    private ItemStackHandler createMultiStackSpecificIHandler(int... slotIDs)
    {
        return new ItemStackHandler(slotIDs.length)
        {
            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                if(slot < slotIDs.length) itemHandler.setStackInSlot(slotIDs[slot], stack);
            }

            @NotNull
            @Override
            public ItemStack getStackInSlot(int slot) {
                if(slot < slotIDs.length) return itemHandler.getStackInSlot(slotIDs[slot]);
                return ItemStack.EMPTY;
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if(slot < slotIDs.length) return itemHandler.insertItem(slotIDs[slot], stack, simulate);
                return stack;
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate)
            {
                if(slot >= slotIDs.length) return ItemStack.EMPTY;

                if(slotIDs[slot] == SLOT_BATTERY && itemHandler.getStackInSlot(slot).is(ModItems.registeredItems.get("energy_cell").get()))
                    return ItemStack.EMPTY; // Refuse to extract the item if it isn't a depleted cell
                else return itemHandler.extractItem(slotIDs[slot], amount, simulate);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return itemHandler.isItemValid(slotIDs[slot], stack);
            }
        };
    }

    private FDEnergyStorage createEHandler(){
        return new FDEnergyStorage(type.powerCapacity, MAX_CHARGE_RATE, 0);
    }

    private FDFluidStorage createFHandler()
    {
        return new FDFluidStorage(FLUID_CAPACITY){
            @Override
            public boolean isFluidValid(FluidStack fluid){
                return fluid.getFluid().isSame(Fluids.LAVA);
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
    @SuppressWarnings("ConstantConditions")
    public boolean isItemValid(int slot, @NotNull ItemStack stack)
    {
        if(slot == SLOT_INPUT) return recipes.containsKey(stack.getItem().getRegistryName().getPath().toLowerCase());
        else if(slot == SLOT_BATTERY) return stack.is(ModItems.registeredItems.get("energy_cell").get())
                || stack.is(ModItems.registeredItems.get("depleted_cell").get());
        else if(slot == SLOT_BUCKET) return stack.getItem() instanceof BucketItem;
        else if(slot == SLOT_EMPTY_BUCKET) return stack.getItem() instanceof DispensibleContainerItem;
        else return false;
    }

    /**
     * Adds all registered recipes for ores and ingots to the internal recipe list.
     */
    private static HashMap<String, GenericMachineRecipe> addRecipes()
    {
        HashMap<String, GenericMachineRecipe> rv = new HashMap<>();

        // Add ore processing recipes
        rv.put("purified_native_copper", new GenericMachineRecipe((int)(OreEnum.NATIVE_COPPER.hardness * 20.0f), ModItems.registeredItems.get("purified_native_copper").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_copper").get(), 1)));
        rv.put("purified_native_tin", new GenericMachineRecipe((int)(OreEnum.NATIVE_TIN.hardness * 20.0f), ModItems.registeredItems.get("purified_native_tin").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_tin").get(), 1)));
        rv.put("purified_bertrandite", new GenericMachineRecipe((int)(OreEnum.BERTRANDITE.hardness * 20.0f), ModItems.registeredItems.get("purified_bertrandite").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_beryllium").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_small_silicon").get(), 1)));
        rv.put("purified_spherocobaltite", new GenericMachineRecipe((int)(OreEnum.SPHEROCOBALTITE.hardness * 20.0f), ModItems.registeredItems.get("purified_spherocobaltite").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_cobalt").get(), 1)));
        rv.put("purified_tetrataenite", new GenericMachineRecipe((int)(OreEnum.TETRATAENITE.hardness * 20.0f), ModItems.registeredItems.get("purified_tetrataenite").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_nickel").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_small_iridium").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_small_osmium").get(), 1)));
        rv.put("purified_bauxite", new GenericMachineRecipe((int)(OreEnum.BAUXITE.hardness * 20.0f), ModItems.registeredItems.get("purified_bauxite").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_aluminium").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_small_titanium").get(), 1)));
        rv.put("purified_wolframite", new GenericMachineRecipe((int)(OreEnum.WOLFRAMITE.hardness * 20.0f), ModItems.registeredItems.get("purified_wolframite").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_tungsten").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_iron").get(), 1)));
        rv.put("purified_pentlandite", new GenericMachineRecipe((int)(OreEnum.PENTLANDITE.hardness * 20.0f), ModItems.registeredItems.get("purified_pentlandite").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_nickel").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_iron").get(), 1)));
        rv.put("purified_gold_ore", new GenericMachineRecipe(80, ModItems.registeredItems.get("purified_gold_ore").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_gold").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_small_palladium").get(), 1)));
        rv.put("purified_iron_ore", new GenericMachineRecipe(80, ModItems.registeredItems.get("purified_iron_ore").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_iron").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_small_nickel").get(), 1)));
        rv.put("crushed_endstone", new GenericMachineRecipe(100, ModItems.registeredItems.get("crushed_endstone").get(),
                new RecipeComponent(ModItems.registeredItems.get("nugget_rare_earth").get(), 1)));

        // Add alloy processing recipes
        rv.put("dust_bronze", new GenericMachineRecipe((int)(MaterialEnum.BRONZE.hardness * 20.0f), ModItems.registeredItems.get("dust_bronze").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_copper").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_small_tin").get(), 1)));

        rv.put("dust_invar", new GenericMachineRecipe((int)(MaterialEnum.INVAR.hardness * 20.0f), ModItems.registeredItems.get("dust_invar").get(),
                new RecipeComponent(ModItems.registeredItems.get("dust_iron").get(), 1),
                new RecipeComponent(ModItems.registeredItems.get("dust_small_nickel").get(), 1)));

        return rv;
    }
}
