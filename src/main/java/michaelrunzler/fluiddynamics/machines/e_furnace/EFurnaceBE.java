package michaelrunzler.fluiddynamics.machines.e_furnace;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.recipes.RecipeIndex;
import michaelrunzler.fluiddynamics.types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EFurnaceBE extends PoweredMachineBE
{
    private final ItemStackHandler itemHandler = createIHandler();
    private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(() -> itemHandler);

    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    private static final String ITEM_NBT_TAG = "Inventory";
    private static final String ENERGY_NBT_TAG = "Energy";
    private static final String INFO_NBT_TAG = "Info";
    private static final String PROGRESS_NBT_TAG = "Progress";

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;

    public static final Map<String, XPGeneratingMachineRecipe> recipes = RecipeIndex.EFurnaceRecipes; // Stores all valid recipes for this machine tagged by their input item name
    public RelativeFacing relativeFacing;
    public AtomicInteger progress;
    public AtomicInteger maxProgress;
    public XPGeneratingMachineRecipe currentRecipe; // Represents the currently processing recipe in the machine
    private boolean invalidOutput; // When 'true', the ticker logic can bypass state checking and assume the output is full
    private boolean lastPowerState; // Used to minimize state updates
    private boolean tryTickRecipeCB; // Used to determine if the BE should attempt to re-query recipes on tick instead of on load

    @SuppressWarnings("unchecked")
    public EFurnaceBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.E_FURNACE, false, true, false);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        progress = new AtomicInteger(0);
        maxProgress = new AtomicInteger(1);
        currentRecipe = null;
        invalidOutput = false;
        lastPowerState = false;
        tryTickRecipeCB = false;
        optionals.add(itemOpt);

        // Initialize handlers for each slot
        slotHandlers = new LazyOptional[type.numInvSlots];
        rawHandlers = new IItemHandler[type.numInvSlots];
        for(int i = 0; i < type.numInvSlots; i++) {
            final int k = i;
            rawHandlers[k] = createStackSpecificIHandler(k);
            slotHandlers[k] = LazyOptional.of(() -> rawHandlers[k]);
            optionals.add(slotHandlers[k]);
        }

        // Get vanilla recipes
        addVanillaRecipes();
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        if(tag.contains(ITEM_NBT_TAG)) itemHandler.deserializeNBT(tag.getCompound(ITEM_NBT_TAG));
        if(tag.contains(ENERGY_NBT_TAG)) energyHandler.deserializeNBT(tag.get(ENERGY_NBT_TAG));
        updateRecipe(itemHandler.getStackInSlot(SLOT_INPUT));
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
        // If needed, try re-querying the recipe index
        if(tryTickRecipeCB) addVanillaRecipes();

        // Just run power handling if no recipe is in progress
        ItemStack bStack = chargeFromBattery(SLOT_BATTERY, itemHandler);
        if(bStack != null) itemHandler.setStackInSlot(SLOT_BATTERY, bStack);
        if(currentRecipe == null) {
            // Forcibly update power state to ensure that it remains synced
            updatePowerState(false);
            return;
        }

        boolean powered = false;

        if(progress.get() < currentRecipe.time && energyHandler.getEnergyStored() >= type.powerConsumption)
        {
            // If the current recipe is still in progress, try to consume some energy and advance the recipe
            energyHandler.setEnergy(energyHandler.getEnergyStored() - type.powerConsumption);
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
            if(didOperation)
            {
                dropXP(currentRecipe.xp);
                itemHandler.setStackInSlot(SLOT_INPUT, new ItemStack(input.getItem(), input.getCount() - 1));
                progress.set(0);
                powered = true;
                setChanged();
            }else invalidOutput = true; // Otherwise, mark the output as invalid until we see an item change
        }

        updatePowerState(powered);
    }

    private ItemStackHandler createIHandler()
    {
        return new FDItemHandler(type.numInvSlots)
        {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                if(slot < type.numInvSlots) return EFurnaceBE.this.isItemValid(slot, stack);
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
                if(slotID == SLOT_BATTERY && itemHandler.getStackInSlot(slot).is(RecipeGenerator.registryToItem("energy_cell"))) return;
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
                if(slotID == SLOT_BATTERY && itemHandler.getStackInSlot(slot).is(RecipeGenerator.registryToItem("energy_cell")))
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
        else if(slot == SLOT_BATTERY) return stack.getItem() instanceof IChargeableItem chargeable && chargeable.canDischarge();
        else if(slot == SLOT_OUTPUT) return false;
        else return false;
    }

    /**
     * Adds all Vanilla recipes for the Blast Furnace recipe type.
     */
    private void addVanillaRecipes()
    {
        // If the level isn't loaded yet, try to re-query when the first tick happens
        if(level == null){
            tryTickRecipeCB = true;
            return;
        }

        // Grab the vanilla recipe list from the RecipeHandler and add all of its recipes to the internal registry
        List<BlastingRecipe> vanillaRecipes = level.getRecipeManager().getAllRecipesFor(RecipeType.BLASTING);
        for(BlastingRecipe r : vanillaRecipes) // I know this is long but this is ONE LINE believe it or not, Vanilla recipe handler accesses are a PITA
            recipes.put(RecipeGenerator.getName(r.getIngredients().get(0).getItems()[0].getItem()),
                    new XPGeneratingMachineRecipe(r.getCookingTime(), r.getExperience(), r.getIngredients().get(0).getItems()[0].getItem(),
                            new RecipeIngredient(r.getResultItem().getItem(), r.getResultItem().getCount())));

        // Clear the re-query flag if it was set
        tryTickRecipeCB = false;
    }
}
