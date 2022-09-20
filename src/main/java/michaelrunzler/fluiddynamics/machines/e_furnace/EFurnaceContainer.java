package michaelrunzler.fluiddynamics.machines.e_furnace;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
    }
}
