package michaelrunzler.fluiddynamics.machines.power_cell;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class PowerCellScreen extends AbstractContainerScreen<PowerCellContainer>
{
    private final ResourceLocation UI = new ResourceLocation(FluidDynamics.MODID, "textures/gui/" + MachineEnum.POWER_CELL.name().toLowerCase() + ".png");

    public PowerCellScreen(PowerCellContainer container, Inventory inventory, Component name) {
        super(container, inventory, name);
    }

    @Override
    public void render(@NotNull PoseStack stack, int x, int y, float ticks) {
        this.renderBackground(stack);
        super.render(stack, x, y, ticks);
        this.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack stack, int x, int y)
    {
        // Draw the energy tooltip if the cursor is within the range of the battery icon
        if(checkBounds(x, y, 76, 100, 12, 54))
            drawString(stack, Minecraft.getInstance().font, menu.getEnergyStored() + "/" + menu.getMaxEnergy() + " RBe",
                    x, y - 8, 0xaaaaaa);

        super.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float ticks, int x, int y)
    {
        // Render the background image
        RenderSystem.setShaderTexture(0, UI);
        this.blit(stack, ((this.width - this.imageWidth) / 2), ((this.height - this.imageHeight) / 2), 0, 0, this.imageWidth, this.imageHeight);

        // Render the energy icon (battery)
        int energyOverlay = Math.min(fpDivideMult(menu.getEnergyStored(), menu.getMaxEnergy(), 42), 42);
        this.blit(stack, this.leftPos + 76, this.topPos + 54 - energyOverlay, 177, 42 - energyOverlay, 42, energyOverlay);
    }

    /**
     * Does a floating-point division and multiplication operation on the input ints, then returns the result cast to an int.
     */
    @SuppressWarnings("SameParameterValue")
    private int fpDivideMult(int num, int denom, int mult){
        return (int)(((float)num) / ((float)denom) * ((float)mult));
    }

    @SuppressWarnings("SameParameterValue")
    private boolean checkBounds(int x, int y, int lbX, int rbX, int tbY, int bbY) {
        return ((this.leftPos + lbX < x) && (x < this.leftPos + rbX)) && ((this.topPos + tbY < y) && (y < this.topPos + bbY));
    }
}
