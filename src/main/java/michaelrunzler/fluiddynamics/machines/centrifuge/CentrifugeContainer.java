package michaelrunzler.fluiddynamics.machines.centrifuge;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.types.FDFluidStorage;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CentrifugeContainer extends MachineContainerBase
{
    public CentrifugeContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.CENTRIFUGE, ModContainers.CONTAINER_CENTRIFUGE.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, CentrifugeBE.SLOT_BATTERY, 56, 53));
                addSlot(new SlotItemHandler(c, CentrifugeBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, CentrifugeBE.SLOT_BUCKET, 29, 17));
                addSlot(new SlotItemHandler(c, CentrifugeBE.SLOT_EMPTY_BUCKET, 29, 53));
                addSlot(new SlotItemHandler(c, CentrifugeBE.SLOT_OUTPUT_1, 116, 17));
                addSlot(new SlotItemHandler(c, CentrifugeBE.SLOT_OUTPUT_2, 116, 52));
                addSlot(new SlotItemHandler(c, CentrifugeBE.SLOT_OUTPUT_3, 143, 35));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);

        syncFluids();
        syncProgress();
    }

    /**
     * Synchronizes fluid tank data between the server and client by splitting it into two data slots.
     */
    @SuppressWarnings("ConstantConditions")
    private void syncFluids()
    {
        // Upper 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getFluidLevel(), true);
            }

            @Override
            public void set(int value) {
                be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(c -> ((FDFluidStorage) c).setFluidInTank(
                        0, new FluidStack(Fluids.WATER, merge16b(c.getFluidInTank(0).getAmount(), value, true))));
            }
        });

        // Lower 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getFluidLevel(), false);
            }

            @Override
            public void set(int value) {
                be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(c -> ((FDFluidStorage) c).setFluidInTank(
                        0, new FluidStack(Fluids.WATER, merge16b(c.getFluidInTank(0).getAmount(), value, false))));
            }
        });
    }

    /**
     * Synchronizes progress data between the server and client by splitting it into two data slots.
     */
    private void syncProgress()
    {
        CentrifugeBE mbe = (CentrifugeBE) be;

        // Upper 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getProgress(), true);
            }

            @Override
            public void set(int value) {
                mbe.progress.set(merge16b(mbe.progress.get(), value, true));
            }
        });

        // Lower 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getProgress(), false);
            }

            @Override
            public void set(int value) {
                mbe.progress.set(merge16b(mbe.progress.get(), value, false));
            }
        });

        // Sync MaxProgress as well
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxProgress(), true);
            }

            @Override
            public void set(int value) {
                mbe.maxProgress.set(merge16b(mbe.maxProgress.get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxProgress(), false);
            }

            @Override
            public void set(int value) {
                mbe.maxProgress.set(merge16b(mbe.maxProgress.get(), value, false));
            }
        });
    }

    //
    // Accessors for the BE's properties
    //

    public int getProgress(){
        return ((CentrifugeBE)be).progress.get();
    }

    public int getMaxProgress(){
        return ((CentrifugeBE)be).maxProgress.get();
    }

    public int getFluidLevel(){
        return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(iFluidHandler -> iFluidHandler.getFluidInTank(0).getAmount()).orElse(0);
    }

    public int getMaxFluidLevel(){
        return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(iFluidHandler -> iFluidHandler.getTankCapacity(0)).orElse(1);
    }
}
