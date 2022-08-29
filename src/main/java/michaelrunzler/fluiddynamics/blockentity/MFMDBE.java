package michaelrunzler.fluiddynamics.blockentity;

import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.item.EnergyCell;
import michaelrunzler.fluiddynamics.item.ModItems;
import michaelrunzler.fluiddynamics.recipes.GenericMachineRecipe;
import michaelrunzler.fluiddynamics.types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    private static final String ITEM_NBT_TAG = "Inventory";

    private final FDEnergyReceiver energyHandler = createEHandler();
    private final LazyOptional<IEnergyStorage> energyOpt = LazyOptional.of(() -> energyHandler);
    private static final String ENERGY_NBT_TAG = "Energy";

    private static final String INFO_NBT_TAG = "Info";
    private static final String PROGRESS_NBT_TAG = "Progress";

    public static final int NUM_INV_SLOTS = 3;
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;

    // Stores all valid recipes for this machine tagged by their input item name
    public final HashMap<String, GenericMachineRecipe> recipes = addRecipes();
    public int progress;
    public GenericMachineRecipe currentRecipe;

    public MFMDBE(BlockPos pos, BlockState state)
    {
        super(pos, state, MachineEnum.MOLECULAR_DECOMPILER);

        progress = 0;
        currentRecipe = null;
        optionals.add(itemOpt);
        optionals.add(energyOpt);
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        if(tag.contains(ITEM_NBT_TAG)) itemHandler.deserializeNBT(tag.getCompound(ITEM_NBT_TAG));
        if(tag.contains(ENERGY_NBT_TAG)) itemHandler.deserializeNBT(tag.getCompound(ENERGY_NBT_TAG));
        if(tag.contains(INFO_NBT_TAG)) progress = tag.getCompound(INFO_NBT_TAG).getInt(PROGRESS_NBT_TAG);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        tag.put(ITEM_NBT_TAG, itemHandler.serializeNBT());
        tag.put(ENERGY_NBT_TAG, energyHandler.serializeNBT());

        CompoundTag iTag = new CompoundTag();
        iTag.putInt(PROGRESS_NBT_TAG, progress);
        tag.put(INFO_NBT_TAG, iTag);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (LazyOptional<T>)itemOpt;
        if(cap == CapabilityEnergy.ENERGY) return (LazyOptional<T>)energyOpt;
        return super.getCapability(cap, side);
    }

    //
    // Handlers and Lambdas
    //

    public void tickServer()
    {
        acceptPower();
        if(currentRecipe == null) return;

        if(progress < currentRecipe.time && energyHandler.getEnergyStored() >= type.powerConsumption)
        {
            // The current recipe is still in progress; try to consume some energy and advance the recipe
            energyHandler.extractEnergy(type.powerConsumption, false);
            progress++;
            setChanged();
        }else if(progress > currentRecipe.time)
        {
            // The current recipe is done; try to "transfer" item to output
            ItemStack input = itemHandler.getStackInSlot(SLOT_INPUT);
            ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
            boolean didOperation = false;

            if(output.getCount() == 0) { // If the output is empty, add the new item
                itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(currentRecipe.out[0], 1));
                didOperation = true;
            } else { // If the output is not empty, check if the item in the output is the same. If it is, add one; if not, stop
                if(output.is(currentRecipe.out[0].asItem())) {
                    output.setCount(output.getCount() + 1);
                    input.setCount(input.getCount() - 1);
                }
            }

            // If we successfully added an output, remove the input
            if(didOperation) input.setCount(input.getCount() - 1);
        }
    }

    /**
     * Gets power from surrounding blocks if they are capable of generating it.
     */
    private void acceptPower()
    {
        AtomicInteger capacity = new AtomicInteger(energyHandler.getEnergyStored());
        if(capacity.get() >= energyHandler.getMaxEnergyStored() || level == null) return;

        // Check each direction for a compatible powered TileEntity
        for(Direction d : Direction.values())
        {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(d));
            if(be == null) continue;

            // Get the BE Energy Handler for this Direction and attempt to extract power from it if it's compatible
            LazyOptional<IEnergyStorage> es = be.getCapability(CapabilityEnergy.ENERGY);
            es.ifPresent(h ->
            {
               if(h.canExtract())
               {
                   int xfer = h.extractEnergy(type.powerConsumption, false);
                   capacity.addAndGet(xfer);
                   energyHandler.receiveEnergy(xfer, false);
                   setChanged();
               }
            });
        }

        // Check for an Energy Cell in the Cell slot
        ItemStack batt = itemHandler.getStackInSlot(SLOT_BATTERY);
        if(batt.is(ModItems.registeredItems.get("energy_cell").get()) && batt.getCount() > 0)
        {
            // If there is a valid cell in the slot, check to see if we need energy, and if so, extract some from the cell
            if(energyHandler.getEnergyStored() < energyHandler.getMaxEnergyStored() && batt.getDamageValue() < batt.getMaxDamage())
            {
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
        return new ItemStackHandler(NUM_INV_SLOTS)
        {
            @SuppressWarnings("ConstantConditions")
            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
            {
                // Check to make sure the item is valid, and update the current recipe
                if(slot == SLOT_INPUT && isItemValid(slot, stack))
                    currentRecipe = recipes.get(stack.getItem().getRegistryName().getPath());

                return super.insertItem(slot, stack, simulate);
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate)
            {
                // If there is a recipe in progress, drop the current recipe and cancel progress
                if(slot == SLOT_INPUT && progress > 0){
                    progress = 0;
                    currentRecipe = null;
                }

                return super.extractItem(slot, amount, simulate);
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                if(slot == SLOT_INPUT) return recipes.containsKey(stack.getItem().getRegistryName().getPath().toLowerCase());
                else if(slot == SLOT_BATTERY) return stack.is(ModItems.registeredItems.get("energy_cell").get())
                        || stack.is(ModItems.registeredItems.get("depleted_cell").get());
                else if(slot == SLOT_OUTPUT) return false;
                else return super.isItemValid(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    private FDEnergyReceiver createEHandler(){
        return new FDEnergyReceiver(type.powerCapacity, type.powerConsumption);
    }

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
            rv.put("ore_" + name, new GenericMachineRecipe((int)(type.hardness * 30.0f), ModBlockItems.registeredBItems.get("ore_" + name).get(),
                    ModItems.registeredItems.get("crushed_" + name).get()));
        }

        return rv;
    }
}
