package michaelrunzler.fluiddynamics.machines.ht_furnace;

import michaelrunzler.fluiddynamics.machines.base.MachineBlockEntityBase;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HTFurnaceBE extends MachineBlockEntityBase
{
    private final ItemStackHandler itemHandler = createIHandler();
    private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(() -> itemHandler);

    private final LazyOptional<IItemHandler>[] slotHandlers; // Contains individual slot handlers for each block side
    private final IItemHandler[] rawHandlers;

    private static final String ITEM_NBT_TAG = "Inventory";
    private static final String INFO_NBT_TAG = "Info";
    private static final String PROGRESS_NBT_TAG = "Progress";
    private static final String FUEL_NBT_TAG = "Fuel";
    private static final String MAX_FUEL_NBT_TAG = "MaxFuel";

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;

    public static final Map<String, XPGeneratingMachineRecipe> recipes = RecipeIndex.HTFurnaceRecipes; // Stores all valid recipes for this machine tagged by their input item name
    public RelativeFacing relativeFacing;
    public AtomicInteger progress;
    public AtomicInteger maxProgress;
    public AtomicInteger fuel;
    public AtomicInteger maxFuel;
    public XPGeneratingMachineRecipe currentRecipe; // Represents the currently processing recipe in the machine
    private boolean invalidOutput; // When 'true', the ticker logic can bypass state checking and assume the output is full
    private boolean tryTickRecipeCB; // Used to determine if the BE should attempt to re-query recipes on tick instead of on load

    @SuppressWarnings("unchecked")
    public HTFurnaceBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.HT_FURNACE);

        relativeFacing = new RelativeFacing(super.getBlockState().getValue(BlockStateProperties.FACING));
        progress = new AtomicInteger(0);
        maxProgress = new AtomicInteger(1);
        fuel = new AtomicInteger(0);
        maxFuel = new AtomicInteger(1);
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
            rawHandlers[k] = createStackSpecificIHandler(itemHandler, BatterySlotAction.NOTHING, k);
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
        updateRecipe(itemHandler.getStackInSlot(SLOT_INPUT));
        if(tag.contains(INFO_NBT_TAG)) {
            progress.set(tag.getCompound(INFO_NBT_TAG).getInt(PROGRESS_NBT_TAG));
            fuel.set(tag.getCompound(INFO_NBT_TAG).getInt(FUEL_NBT_TAG));
            maxFuel.set(tag.getCompound(INFO_NBT_TAG).getInt(MAX_FUEL_NBT_TAG));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        tag.put(ITEM_NBT_TAG, itemHandler.serializeNBT());

        CompoundTag iTag = new CompoundTag();
        iTag.putInt(PROGRESS_NBT_TAG, progress.get());
        iTag.putInt(FUEL_NBT_TAG, fuel.get());
        iTag.putInt(MAX_FUEL_NBT_TAG, maxFuel.get());
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
            else if(side == relativeFacing.FRONT || side == relativeFacing.BACK) return (LazyOptional<T>) slotHandlers[SLOT_FUEL];
            else return (LazyOptional<T>) itemOpt;
        }

        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        // If needed, try re-querying the recipe index
        if(tryTickRecipeCB) addVanillaRecipes();

        // Just run fuel handling if no recipe is in progress
        handleFuel();
        if(currentRecipe == null) {
            // Forcibly update power state to ensure that it remains synced
            updatePowerState(false);
            return;
        }

        boolean powered = false;

        if(progress.get() < currentRecipe.time && fuel.get() > 0)
        {
            // If the current recipe is still in progress, try advance the recipe
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
        }else if(fuel.get() <= 0){
            // Reset the recipe if the furnace runs out of fuel - unlike the powered variants, this acts like the
            // Vanilla furnace, and cannot hold a recipe's state if it gets stalled
            progress.set(0);
            maxFuel.set(1);
        }

        updatePowerState(powered);
    }

    /**
     * Runs fuel item updates - refueling, validity, etc.
     */
    private void handleFuel()
    {
        ItemStack fuelStack = itemHandler.getStackInSlot(SLOT_FUEL);
        if(currentRecipe != null && !fuelStack.isEmpty() && fuel.get() <= 0)
        {
            // Ensure the item in the fuel slot is valid, then attempt to consume it and add to the burn time
            int burnTime = ForgeHooks.getBurnTime(fuelStack, RecipeType.BLASTING);
            if (burnTime > 0) {
                fuel.set(burnTime);
                maxFuel.set(burnTime);
                itemHandler.setStackInSlot(SLOT_FUEL, new ItemStack(fuelStack.getItem(), fuelStack.getCount() - 1));
            }
        }

        // Drain one fuel if there is any to drain
        if(fuel.get() > 0) fuel.set(fuel.get() - 4); // This furnace uses 2x the fuel that the vanilla one does (like a less efficient blast furnace)
    }

    private ItemStackHandler createIHandler()
    {
        return new FDItemHandler(type.numInvSlots)
        {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                if(slot < type.numInvSlots) return HTFurnaceBE.this.isItemValid(slot, stack);
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
        else if(slot == SLOT_FUEL) return ForgeHooks.getBurnTime(stack, RecipeType.BLASTING) > 0;
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
