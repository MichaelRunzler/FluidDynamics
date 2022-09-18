package michaelrunzler.fluiddynamics.machines.redstone_generator;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

@SuppressWarnings("SameParameterValue")
public class RsGenContainer extends MachineContainerBase
{
    public RsGenContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.RS_GENERATOR, ModContainers.CONTAINER_RSGEN.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, RsGenBE.SLOT_BATTERY, 80, 61));
                addSlot(new SlotItemHandler(c, RsGenBE.SLOT_FUEL, 152, 61));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);

     
        syncFuel();
    }

    /**
     * Synchronizes fuel data between the server and client by splitting it into two data slots.
     */
    private void syncFuel()
    {
        RsGenBE mbe = (RsGenBE) be;

        // Upper 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getFuel(), true);
            }

            @Override
            public void set(int value) {
                mbe.fuel.set(merge16b(mbe.fuel.get(), value, true));
            }
        });

        // Lower 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getFuel(), false);
            }

            @Override
            public void set(int value) {
                mbe.fuel.set(merge16b(mbe.fuel.get(), value, false));
            }
        });

        // Sync MaxFuel as well
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxFuel(), true);
            }

            @Override
            public void set(int value) {
                mbe.maxFuel.set(merge16b(mbe.maxFuel.get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getMaxFuel(), false);
            }

            @Override
            public void set(int value) {
                mbe.maxFuel.set(merge16b(mbe.maxFuel.get(), value, false));
            }
        });
    }

    //
    // Accessors for the BE's properties
    //

    public int getFuel(){
        return ((RsGenBE) be).fuel.get();
    }

    public int getMaxFuel(){
        return ((RsGenBE)be).maxFuel.get();
    }
}
