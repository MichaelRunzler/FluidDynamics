package michaelrunzler.fluiddynamics.machines;

import michaelrunzler.fluiddynamics.block.ModBlocks;
import michaelrunzler.fluiddynamics.machines.MFMD.MFMDScreen;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

/**
 * Associates all screens with their respective containers.
 */
public class ModScreens
{
    public static void registerAllScreens()
    {
        MenuScreens.register(ModContainers.CONTAINER_MFMD.get(), MFMDScreen::new);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.registeredBlocks.get(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase()).get(), RenderType.translucent());
    }
}
