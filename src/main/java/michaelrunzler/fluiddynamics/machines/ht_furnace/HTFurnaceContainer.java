package michaelrunzler.fluiddynamics.machines.ht_furnace;

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
public class HTFurnaceContainer extends MachineContainerBase
{
    public HTFurnaceContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.HT_FURNACE, ModContainers.CONTAINER_HTFURNACE.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, HTFurnaceBE.SLOT_FUEL, 56, 53));
                addSlot(new SlotItemHandler(c, HTFurnaceBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, HTFurnaceBE.SLOT_OUTPUT, 116, 35));
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
        HTFurnaceBE mbe = (HTFurnaceBE) be;

        // And fuel/max fuel
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
        return ((HTFurnaceBE)be).fuel.get();
    }

    public int getMaxFuel(){
        return ((HTFurnaceBE)be).maxFuel.get();
    }
}
