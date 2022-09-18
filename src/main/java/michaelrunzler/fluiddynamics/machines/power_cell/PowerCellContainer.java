package michaelrunzler.fluiddynamics.machines.power_cell;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

@SuppressWarnings("SameParameterValue")
public class PowerCellContainer extends MachineContainerBase
{
    public PowerCellContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.POWER_CELL, ModContainers.CONTAINER_POWERCELL.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, PowerCellBE.SLOT_BATTERY_IN, 65, 59));
                addSlot(new SlotItemHandler(c, PowerCellBE.SLOT_BATTERY_OUT, 95, 59));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);
    }
}
