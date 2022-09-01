package michaelrunzler.fluiddynamics.machines.MFMD;

import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.machines.base.MachineBlockEntityBase;
import michaelrunzler.fluiddynamics.item.EnergyCell;
import michaelrunzler.fluiddynamics.item.ModItems;
import michaelrunzler.fluiddynamics.machines.base.GenericMachineRecipe;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MFMDBE extends MachineBlockEntityBase
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
    private static final String PROGRESS_NBT_TAG = "Progress";

    public static final int NUM_INV_SLOTS = 3;
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int MAX_CHARGE_RATE = 10;

    public final HashMap<String, GenericMachineRecipe> recipes = addRecipes(); // Stores all valid recipes for this machine tagged by their input item name
    public RelativeFacing relativeFacing;
    public AtomicInteger progress;
    public AtomicInteger maxProgress;
    public GenericMachineRecipe currentRecipe; // Represents the currently processing recipe in the machine
    private boolean invalidOutput; // When 'true', the ticker logic can bypass state checking and assume the output is full

    @SuppressWarnings("unchecked")
    public MFMDBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.MOLECULAR_DECOMPILER);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        progress = new AtomicInteger(0);
        maxProgress = new AtomicInteger(1);
        currentRecipe = null;
        invalidOutput = false;
        optionals.add(itemOpt);
        optionals.add(energyOpt);

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[NUM_INV_SLOTS];
        rawHandlers = new IItemHandler[NUM_INV_SLOTS];
        for(int i = 0; i < NUM_INV_SLOTS; i++) {
            final int k = i;
            rawHandlers[k] = createStackSpecificIHandler(k);
            slotHandlers[k] = LazyOptional.of(() -> rawHandlers[k]);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        if(tag.contains(ITEM_NBT_TAG)) itemHandler.deserializeNBT(tag.getCompound(ITEM_NBT_TAG));
        if(tag.contains(ENERGY_NBT_TAG)) energyHandler.deserializeNBT(tag.get(ENERGY_NBT_TAG));
        if(tag.contains(INFO_NBT_TAG)) progress.set(tag.getCompound(INFO_NBT_TAG).getInt(PROGRESS_NBT_TAG));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        tag.put(ITEM_NBT_TAG, itemHandler.serializeNBT());
        tag.put(ENERGY_NBT_TAG, energyHandler.serializeNBT());

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
            if(side == relativeFacing.TOP || side == relativeFacing.LEFT) return (LazyOptional<T>)slotHandlers[SLOT_INPUT];
            else if(side == relativeFacing.BOTTOM || side == relativeFacing.RIGHT) return (LazyOptional<T>)slotHandlers[SLOT_OUTPUT];
            else if(side == relativeFacing.FRONT || side == relativeFacing.BACK) return (LazyOptional<T>) slotHandlers[SLOT_BATTERY];
            else return (LazyOptional<T>) itemOpt;
        }

        // Energy is accessible from all sides, so we don't need sided logic for this
        if(cap == CapabilityEnergy.ENERGY) return (LazyOptional<T>)energyOpt;
        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        // Just run power handling if no recipe is in progress
        acceptPower();
        if(currentRecipe == null) return;

        if(progress.get() < currentRecipe.time && energyHandler.getEnergyStored() >= type.powerConsumption)
        {
            // If the current recipe is still in progress, try to consume some energy and advance the recipe
            energyHandler.setEnergy(energyHandler.getEnergyStored() - type.powerConsumption);
            progress.incrementAndGet();
            setChanged();
        }else if(progress.get() >= currentRecipe.time) // If the current recipe is done, try to transfer the input to the output
        {
            // Shortcut the output-checking logic if we already know the output is blocking the recipe from finishing
            if(invalidOutput) return;

            ItemStack input = itemHandler.getStackInSlot(SLOT_INPUT);
            ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
            boolean didOperation = false;

            if(output.getCount() == 0)
            {
                // If the output is empty, add the new item
                itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(currentRecipe.out[0], 1));
                didOperation = true;
            }else if (output.getCount() >= output.getMaxStackSize() || output.is(currentRecipe.out[0].asItem()))
            {
                // If the output is not empty, check that the item in the output is the same and not a full stack, then
                // add one to the stack
                itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(output.getItem(), output.getCount() + 1));
                didOperation = true;
            }

            // If we successfully added an output, remove one item from the input
            if(didOperation) {
                itemHandler.setStackInSlot(SLOT_INPUT, new ItemStack(input.getItem(), input.getCount() - 1));
                progress.set(0);
            }else invalidOutput = true; // Otherwise, mark the output as invalid until we see an item change
        }
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

    private ItemStackHandler createIHandler()
    {
        return new FDItemHandler(NUM_INV_SLOTS)
        {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                if(slot < NUM_INV_SLOTS) return MFMDBE.this.isItemValid(slot, stack);
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

    private FDEnergyStorage createEHandler(){
        return new FDEnergyStorage(type.powerCapacity, MAX_CHARGE_RATE, 0);
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
        else if(slot == SLOT_BATTERY) return stack.is(ModItems.registeredItems.get("energy_cell").get())
                || stack.is(ModItems.registeredItems.get("depleted_cell").get());
        else if(slot == SLOT_OUTPUT) return false;
        else return false;
    }

    /**
     * Adds all registered recipes for ores and ingots to the internal recipe list.
     */
    private static HashMap<String, GenericMachineRecipe> addRecipes()
    {
        HashMap<String, GenericMachineRecipe> rv = new HashMap<>();

        // Add ingot grinding recipes
        for(MaterialEnum mat : MaterialEnum.values()) {
            String name = mat.name().toLowerCase();
            rv.put("ingot_" + name, new GenericMachineRecipe((int)(mat.hardness * 30.0f), ModItems.registeredItems.get("ingot_" + name).get(),
                    ModItems.registeredItems.get("dust_" + name).get()));
        }

        // Add ore grinding recipes
        for(OreEnum type : OreEnum.values()) {
            String name = type.name().toLowerCase();
            rv.put("ore_" + name, new GenericMachineRecipe((int)(type.hardness * 15.0f), ModBlockItems.registeredBItems.get("ore_" + name).get(),
                    ModItems.registeredItems.get("crushed_" + name).get()));
        }

        return rv;
    }
}
