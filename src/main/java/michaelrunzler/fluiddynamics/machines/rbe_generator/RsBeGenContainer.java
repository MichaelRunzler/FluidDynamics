package michaelrunzler.fluiddynamics.machines.rbe_generator;

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
public class RsBeGenContainer extends MachineContainerBase
{
    public RsBeGenContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.RBE_GENERATOR, ModContainers.CONTAINER_RBEGEN.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, RsBeGenBE.SLOT_BATTERY, 80, 61));
                addSlot(new SlotItemHandler(c, RsBeGenBE.SLOT_FUEL_RS, 152, 61));
                addSlot(new SlotItemHandler(c, RsBeGenBE.SLOT_FUEL_BE, 130, 61));
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
        RsBeGenBE mbe = (RsBeGenBE) be;

        // Upper 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getRsFuel(), true);
            }

            @Override
            public void set(int value) {
                mbe.rsFuel.set(merge16b(mbe.rsFuel.get(), value, true));
            }
        });

        // Lower 16 bits of the value
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getRsFuel(), false);
            }

            @Override
            public void set(int value) {
                mbe.rsFuel.set(merge16b(mbe.rsFuel.get(), value, false));
            }
        });

        // Sync MaxFuel as well
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getRsMaxFuel(), true);
            }

            @Override
            public void set(int value) {
                mbe.maxRsFuel.set(merge16b(mbe.maxRsFuel.get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getRsMaxFuel(), false);
            }

            @Override
            public void set(int value) {
                mbe.maxRsFuel.set(merge16b(mbe.maxRsFuel.get(), value, false));
            }
        });

        // ...and also sync Be fuel levels
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getBeFuel(), true);
            }

            @Override
            public void set(int value) {
                mbe.beFuel.set(merge16b(mbe.beFuel.get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getBeFuel(), false);
            }

            @Override
            public void set(int value) {
                mbe.beFuel.set(merge16b(mbe.beFuel.get(), value, false));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getBeMaxFuel(), true);
            }

            @Override
            public void set(int value) {
                mbe.maxBeFuel.set(merge16b(mbe.maxBeFuel.get(), value, true));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return get16b(getBeMaxFuel(), false);
            }

            @Override
            public void set(int value) {
                mbe.maxBeFuel.set(merge16b(mbe.maxBeFuel.get(), value, false));
            }
        });
    }

    //
    // Accessors for the BE's properties
    //

    public int getRsFuel(){
        return ((RsBeGenBE) be).rsFuel.get();
    }

    public int getRsMaxFuel(){
        return ((RsBeGenBE)be).maxRsFuel.get();
    }

    public int getBeFuel(){
        return ((RsBeGenBE) be).beFuel.get();
    }

    public int getBeMaxFuel(){
        return ((RsBeGenBE)be).maxBeFuel.get();
    }
}
