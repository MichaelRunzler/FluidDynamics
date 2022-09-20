package michaelrunzler.fluiddynamics.machines.power_cell;

import com.mojang.blaze3d.vertex.PoseStack;
import michaelrunzler.fluiddynamics.machines.base.MachineScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class PowerCellScreen extends MachineScreenBase<PowerCellContainer>
{
    public PowerCellScreen(PowerCellContainer container, Inventory inventory, Component name) {
        super(container, inventory, name, container.type);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack stack, int x, int y) {
        renderFractionalTooltip(stack, x, y, 76, 100, 12, 54, menu.getEnergyStored(), menu.getMaxEnergy(), "RBe");
        super.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float ticks, int x, int y)
    {
        super.renderBg(stack, ticks, x, y);

        // Render the energy icon (battery)
        int energyOverlay = Math.min(fpDivideMult(menu.getEnergyStored(), menu.getMaxEnergy(), 42), 42);
        this.blit(stack, this.leftPos + 76, this.topPos + 54 - energyOverlay, 177, 42 - energyOverlay, 42, energyOverlay);
    }
}
