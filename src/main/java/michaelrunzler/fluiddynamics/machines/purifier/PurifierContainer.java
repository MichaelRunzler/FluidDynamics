package michaelrunzler.fluiddynamics.machines.purifier;

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

public class PurifierContainer extends MachineContainerBase
{
    public PurifierContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.PURIFIER, ModContainers.CONTAINER_PURIFIER.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_BATTERY, 56, 53));
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_OUTPUT, 116, 35));
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_BUCKET, 29, 17));
                addSlot(new SlotItemHandler(c, PurifierBE.SLOT_EMPTY_BUCKET, 29, 53));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);

        syncFluids();
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

    //
    // Accessors for the BE's properties
    //

    public int getFluidLevel(){
        return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(iFluidHandler -> iFluidHandler.getFluidInTank(0).getAmount()).orElse(0);
    }

    public int getMaxFluidLevel(){
        return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(iFluidHandler -> iFluidHandler.getTankCapacity(0)).orElse(1);
    }
}
