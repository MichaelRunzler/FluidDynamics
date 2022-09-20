package michaelrunzler.fluiddynamics.machines.rbe_generator;

import com.mojang.blaze3d.vertex.PoseStack;
import michaelrunzler.fluiddynamics.machines.base.MachineScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class RsBeGenScreen extends MachineScreenBase<RsBeGenContainer>
{
    public RsBeGenScreen(RsBeGenContainer container, Inventory inventory, Component name) {
        super(container, inventory, name, container.type);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack stack, int x, int y) {
        renderFractionalTooltip(stack, x, y, 76, 100, 12, 54, menu.getEnergyStored(), menu.getMaxEnergy(), "RBe");
        renderFractionalTooltip(stack, x, y, 154, 166, 12, 54, menu.getRsFuel(), menu.getRsMaxFuel(), "Rs");
        renderFractionalTooltip(stack, x, y, 1332, 144, 12, 54, menu.getBeFuel(), menu.getBeMaxFuel(), "Be");
        super.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float ticks, int x, int y)
    {
        super.renderBg(stack, ticks, x, y);

        // Render the energy icon (battery)
        int energyOverlay = Math.min(fpDivideMult(menu.getEnergyStored(), menu.getMaxEnergy(), 42), 42);
        this.blit(stack, this.leftPos + 76, this.topPos + 54 - energyOverlay, 177, 42 - energyOverlay, 24, energyOverlay);

        // Render the fuel gauges
        int rsFuelOverlay = Math.min(fpDivideMult(menu.getRsFuel(), menu.getRsMaxFuel(), 42), 42);
        this.blit(stack, this.leftPos + 154, this.topPos + 54 - rsFuelOverlay, 177, 84 - rsFuelOverlay, 12, rsFuelOverlay);

        int beFuelOverlay = Math.min(fpDivideMult(menu.getBeFuel(), menu.getBeMaxFuel(), 42), 42);
        this.blit(stack, this.leftPos + 132, this.topPos + 54 - beFuelOverlay, 190, 84 - beFuelOverlay, 12, beFuelOverlay);
    }
}
