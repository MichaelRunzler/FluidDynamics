package michaelrunzler.fluiddynamics.interfaces;

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

/**
 * Represents the rendered GUI component of a machine's container interface.
 */
public class MFMDScreen extends AbstractContainerScreen<MFMDContainer>
{
    private final ResourceLocation UI = new ResourceLocation(FluidDynamics.MODID, "textures/gui/" + MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase() + ".png");

    public MFMDScreen(MFMDContainer container, Inventory inventory, Component name) {
        super(container, inventory, name);
    }

    @Override
    public void render(@NotNull PoseStack stack, int x, int y, float ticks) {
        this.renderBackground(stack);
        super.render(stack, x, y, ticks);
        this.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack stack, int x, int y) {
        drawString(stack, Minecraft.getInstance().font, "Energy: " + menu.getEnergyStored(), 64, 64, 0xffffff);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float ticks, int x, int y)
    {
        RenderSystem.setShaderTexture(0, UI);
        this.blit(stack, ((this.width - this.imageWidth) / 2), ((this.height - this.imageHeight) / 2), 0, 0, this.imageWidth, this.imageHeight);

        int energyOverlay = (int)((float)14 * (menu.getEnergyStored() / menu.getMaxEnergy())); // TODO getEnergy way too high?
        int progressOverlay = (int)((float)22 * (menu.getProgress() / menu.getMaxProgress()));
        this.blit(stack, this.leftPos + 57, this.topPos + 36, 177, 0, 14, energyOverlay);
        this.blit(stack, this.leftPos + 80, this.topPos + 35, 177, 14, progressOverlay, 16);
    }
}
