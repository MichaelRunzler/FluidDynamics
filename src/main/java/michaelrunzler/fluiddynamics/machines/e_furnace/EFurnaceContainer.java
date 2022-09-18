package michaelrunzler.fluiddynamics.machines.e_furnace;

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
public class EFurnaceContainer extends MachineContainerBase
{
    public EFurnaceContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.E_FURNACE, ModContainers.CONTAINER_EFURNACE.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, EFurnaceBE.SLOT_BATTERY, 56, 53));
                addSlot(new SlotItemHandler(c, EFurnaceBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, EFurnaceBE.SLOT_OUTPUT, 116, 35));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);

     
        syncProgress();
    }

    /**
     * Synchronizes progress data between the server and client by splitting it into two data slots.
     */
    private void syncProgress()
    {
        EFurnaceBE mbe = (EFurnaceBE) be;

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
        return ((EFurnaceBE) be).progress.get();
    }

    public int getMaxProgress(){
        return ((EFurnaceBE)be).maxProgress.get();
    }
}
