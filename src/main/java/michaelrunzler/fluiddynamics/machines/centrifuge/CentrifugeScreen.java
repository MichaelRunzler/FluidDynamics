package michaelrunzler.fluiddynamics.machines.centrifuge;

import com.mojang.blaze3d.vertex.PoseStack;
import michaelrunzler.fluiddynamics.machines.base.MachineScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class CentrifugeScreen extends MachineScreenBase<CentrifugeContainer>
{
    public CentrifugeScreen(CentrifugeContainer container, Inventory inventory, Component name) {
        super(container, inventory, name, container.type);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack stack, int x, int y) {
        renderFractionalTooltip(stack, x, y, 57, 71, 36, 50, menu.getEnergyStored(), menu.getMaxEnergy(), "RBe");
        renderFractionalTooltip(stack, x, y, 8, 24, 16, 86, menu.getFluidLevel(), menu.getMaxFluidLevel(), "mB");
        super.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float ticks, int x, int y)
    {
        super.renderBg(stack, ticks, x, y);

        // Render the energy icon (battery) and progress bar using their respective values from the BE
        int energyOverlay = Math.min(fpDivideMult(menu.getEnergyStored(), menu.getMaxEnergy(), 14), 14);
        int progressOverlay = Math.min(fpDivideMult(menu.getProgress().get(), menu.getMaxProgress().get(), 61), 61);
        this.blit(stack, this.leftPos + 57, this.topPos + 50 - energyOverlay, 177, 14 - energyOverlay, 14, energyOverlay);
        this.blit(stack, this.leftPos + 80, this.topPos + 35, 177, 14, progressOverlay, 15);

        // Render the fluid gauge
        int fluidOverlay = Math.min(fpDivideMult(menu.getFluidLevel(), menu.getMaxFluidLevel(), 52), 52);
        this.blit(stack, this.leftPos + 9, this.topPos + 69 - fluidOverlay, 177, 81 - fluidOverlay, 16, fluidOverlay);
    }
}
