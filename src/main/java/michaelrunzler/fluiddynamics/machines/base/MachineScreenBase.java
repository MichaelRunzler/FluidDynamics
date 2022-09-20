package michaelrunzler.fluiddynamics.machines.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

/**
 * A base screen renderer class used for all machine types.
 */
public abstract class MachineScreenBase<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{
    private final ResourceLocation UI;
    public static final int TOOLTIP_SPACING_Y = -8;
    public static final int TOOLTIP_SPACING_X = 0;
    public static final int DEFAULT_TOOLTIP_COLOR = 0xaaaaaa;

    public MachineScreenBase(T container, Inventory inventory, Component name, MachineEnum type)
    {
        super(container, inventory, name);
        UI = new ResourceLocation(FluidDynamics.MODID, "textures/gui/" + type.name().toLowerCase() + ".png");
    }

    @Override
    public void render(@NotNull PoseStack stack, int x, int y, float ticks) {
        this.renderBackground(stack);
        super.render(stack, x, y, ticks);
        this.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float ticks, int x, int y) {
        // Render the background image
        RenderSystem.setShaderTexture(0, UI);
        this.blit(stack, ((this.width - this.imageWidth) / 2), ((this.height - this.imageHeight) / 2), 0, 0, this.imageWidth, this.imageHeight);
    }

    /**
     * Does a floating-point division and multiplication operation on the input ints, then returns the result cast to an int.
     */
    protected static int fpDivideMult(int num, int denom, int mult){
        return (int)(((float)num) / ((float)denom) * ((float)mult));
    }

    /**
     * Checks to see if the given coordinates are within the specified bounds.
     */
    protected boolean checkBounds(int x, int y, int lbX, int rbX, int tbY, int bbY) {
        return ((this.leftPos + lbX < x) && (x < this.leftPos + rbX)) && ((this.topPos + tbY < y) && (y < this.topPos + bbY));
    }

    /**
     * Renders a tooltip based upon the cursor being within a specified zone. This tooltip takes the form of a fractional
     * display (ex. 23/100 mB) used to indicate power, fluid contents, progress, etc.
     */
    protected void renderFractionalTooltip(PoseStack stack, int x, int y, int minX, int maxX, int minY, int maxY, int q1, int q2, String unit)
    {
        if(checkBounds(x, y, minX, maxX, minY, maxY))
            drawString(stack, Minecraft.getInstance().font, q1 + "/" + q2 + " " + unit,
                x + TOOLTIP_SPACING_X, y + TOOLTIP_SPACING_Y, DEFAULT_TOOLTIP_COLOR);
    }
}
