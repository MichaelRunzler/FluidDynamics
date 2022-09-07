package michaelrunzler.fluiddynamics.machines.e_furnace;

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

public class EFurnaceScreen extends AbstractContainerScreen<EFurnaceContainer>
{
    private final ResourceLocation UI = new ResourceLocation(FluidDynamics.MODID, "textures/gui/" + MachineEnum.E_FURNACE.name().toLowerCase() + ".png");

    public EFurnaceScreen(EFurnaceContainer container, Inventory inventory, Component name) {
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
        if(checkBounds(x, y, 57, 71, 36, 50))
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

        // Render the energy icon (battery) and progress bar using their respective values from the BE
        int energyOverlay = Math.min(fpDivideMult(menu.getEnergyStored(), menu.getMaxEnergy(), 14), 14);
        int progressOverlay = Math.min(fpDivideMult(menu.getProgress(), menu.getMaxProgress(), 16), 16);
        this.blit(stack, this.leftPos + 57, this.topPos + 50 - energyOverlay, 177, 14 - energyOverlay, 14, energyOverlay);
        this.blit(stack, this.leftPos + 80, this.topPos + 35, 177, 14, progressOverlay, 16);
    }

    /**
     * Does a floating-point division and multiplication operation on the input ints, then returns the result cast to an int.
     */
    private int fpDivideMult(int num, int denom, int mult){
        return (int)(((float)num) / ((float)denom) * ((float)mult));
    }

    @SuppressWarnings("SameParameterValue")
    private boolean checkBounds(int x, int y, int lbX, int rbX, int tbY, int bbY) {
        return ((this.leftPos + lbX < x) && (x < this.leftPos + rbX)) && ((this.topPos + tbY < y) && (y < this.topPos + bbY));
    }
}
