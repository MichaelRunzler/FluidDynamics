package michaelrunzler.fluiddynamics.machines.ht_furnace;

import com.mojang.blaze3d.vertex.PoseStack;
import michaelrunzler.fluiddynamics.machines.base.MachineScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class HTFurnaceScreen extends MachineScreenBase<HTFurnaceContainer>
{
    public HTFurnaceScreen(HTFurnaceContainer container, Inventory inventory, Component name) {
        super(container, inventory, name, container.type);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack stack, int x, int y) {
        renderFractionalTooltip(stack, x, y, 57, 71, 36, 50, menu.getFuel(), menu.getMaxFuel(), "Fuel");
        super.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float ticks, int x, int y)
    {
        super.renderBg(stack, ticks, x, y);

        // Render the fuel icon and progress bar using their respective values from the BE
        int fuelOverlay = Math.min(fpDivideMult(menu.getFuel(), menu.getMaxFuel(), 14), 14);
        int progressOverlay = Math.min(fpDivideMult(menu.getProgress().get(), menu.getMaxProgress().get(), 16), 16);
        this.blit(stack, this.leftPos + 57, this.topPos + 50 - fuelOverlay, 177, 14 - fuelOverlay, 14, fuelOverlay);
        this.blit(stack, this.leftPos + 80, this.topPos + 35, 177, 14, progressOverlay, 16);
    }
}
