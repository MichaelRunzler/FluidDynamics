package michaelrunzler.fluiddynamics.machines.rbe_generator;

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

public class RsBeGenScreen extends AbstractContainerScreen<RsBeGenContainer>
{
    private final ResourceLocation UI = new ResourceLocation(FluidDynamics.MODID, "textures/gui/" + MachineEnum.RBE_GENERATOR.name().toLowerCase() + ".png");

    public RsBeGenScreen(RsBeGenContainer container, Inventory inventory, Component name) {
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

        // Draw the RS and Be fuel tooltips if the cursor is within the range of the fuel gauges
        if(checkBounds(x, y, 154, 166, 12, 54))
            drawString(stack, Minecraft.getInstance().font, menu.getRsFuel() + "/" + menu.getRsMaxFuel() + " RS",
                    x, y - 8, 0xaaaaaa);

        if(checkBounds(x, y, 132, 144, 12, 54))
            drawString(stack, Minecraft.getInstance().font, menu.getBeFuel() + "/" + menu.getBeMaxFuel() + " Be",
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
        this.blit(stack, this.leftPos + 76, this.topPos + 54 - energyOverlay, 177, 42 - energyOverlay, 24, energyOverlay);

        // Render the fuel gauges
        int rsFuelOverlay = Math.min(fpDivideMult(menu.getRsFuel(), menu.getRsMaxFuel(), 42), 42);
        this.blit(stack, this.leftPos + 154, this.topPos + 54 - rsFuelOverlay, 177, 84 - rsFuelOverlay, 12, rsFuelOverlay);

        int beFuelOverlay = Math.min(fpDivideMult(menu.getBeFuel(), menu.getBeMaxFuel(), 42), 42);
        this.blit(stack, this.leftPos + 132, this.topPos + 54 - beFuelOverlay, 190, 84 - beFuelOverlay, 12, beFuelOverlay);
    }

    // TODO move to super
    /**
     * Does a floating-point division and multiplication operation on the input ints, then returns the result cast to an int.
     */
    @SuppressWarnings("SameParameterValue")
    private int fpDivideMult(int num, int denom, int mult){
        return (int)(((float)num) / ((float)denom) * ((float)mult));
    }

    // TODO move to super
    @SuppressWarnings("SameParameterValue")
    private boolean checkBounds(int x, int y, int lbX, int rbX, int tbY, int bbY) {
        return ((this.leftPos + lbX < x) && (x < this.leftPos + rbX)) && ((this.topPos + tbY < y) && (y < this.topPos + bbY));
    }
}
