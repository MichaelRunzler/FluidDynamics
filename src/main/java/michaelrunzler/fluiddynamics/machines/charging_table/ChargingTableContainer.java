package michaelrunzler.fluiddynamics.machines.charging_table;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

@SuppressWarnings("SameParameterValue")
public class ChargingTableContainer extends MachineContainerBase
{
    public ChargingTableContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.CHARGING_TABLE, ModContainers.CONTAINER_CHARGING_TABLE.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, ChargingTableBE.SLOT_BATTERY_IN, 80, 51));
                addSlot(new SlotItemHandler(c, ChargingTableBE.SLOT_BATTERY_OUT, 80, 19));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);
    }
}
