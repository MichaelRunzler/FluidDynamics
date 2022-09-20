package michaelrunzler.fluiddynamics.machines.MFMD;

import michaelrunzler.fluiddynamics.machines.ModContainers;
import michaelrunzler.fluiddynamics.machines.base.MachineContainerBase;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

@SuppressWarnings("SameParameterValue")
public class MFMDContainer extends MachineContainerBase
{
    public MFMDContainer(int windowID, BlockPos pos, Inventory inventory, Player player)
    {
        super(MachineEnum.MOLECULAR_DECOMPILER, ModContainers.CONTAINER_MFMD.get(), windowID, pos, inventory, player);

        // Register and add block inventory slots
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_BATTERY, 56, 53));
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_INPUT, 56, 17));
                addSlot(new SlotItemHandler(c, MFMDBE.SLOT_OUTPUT, 116, 35));
            });
        else throw new IllegalStateException("Missing BlockEntity interface for container instance!");

        layoutPlayerInventory(8, 84);
    }
}
