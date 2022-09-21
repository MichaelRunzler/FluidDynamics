package michaelrunzler.fluiddynamics.machines.charging_table;

import michaelrunzler.fluiddynamics.machines.base.MachineScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ChargingTableScreen extends MachineScreenBase<ChargingTableContainer>
{
    public ChargingTableScreen(ChargingTableContainer container, Inventory inventory, Component name) {
        super(container, inventory, name, container.type);
    }
}
