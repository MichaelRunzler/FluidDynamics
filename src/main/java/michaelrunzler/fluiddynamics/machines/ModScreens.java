package michaelrunzler.fluiddynamics.machines;

import michaelrunzler.fluiddynamics.machines.MFMD.MFMDScreen;
import michaelrunzler.fluiddynamics.machines.centrifuge.CentrifugeScreen;
import michaelrunzler.fluiddynamics.machines.e_furnace.EFurnaceScreen;
import michaelrunzler.fluiddynamics.machines.ht_furnace.HTFurnaceScreen;
import michaelrunzler.fluiddynamics.machines.purifier.PurifierScreen;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
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
        ItemBlockRenderTypes.setRenderLayer(RecipeGenerator.registryToBlock(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase()), RenderType.translucent());

        MenuScreens.register(ModContainers.CONTAINER_PURIFIER.get(), PurifierScreen::new);
        ItemBlockRenderTypes.setRenderLayer(RecipeGenerator.registryToBlock(MachineEnum.PURIFIER.name().toLowerCase()), RenderType.translucent());

        MenuScreens.register(ModContainers.CONTAINER_CENTRIFUGE.get(), CentrifugeScreen::new);
        ItemBlockRenderTypes.setRenderLayer(RecipeGenerator.registryToBlock(MachineEnum.CENTRIFUGE.name().toLowerCase()), RenderType.translucent());

        MenuScreens.register(ModContainers.CONTAINER_EFURNACE.get(), EFurnaceScreen::new);
        ItemBlockRenderTypes.setRenderLayer(RecipeGenerator.registryToBlock(MachineEnum.E_FURNACE.name().toLowerCase()), RenderType.translucent());

        MenuScreens.register(ModContainers.CONTAINER_HTFURNACE.get(), HTFurnaceScreen::new);
        ItemBlockRenderTypes.setRenderLayer(RecipeGenerator.registryToBlock(MachineEnum.HT_FURNACE.name().toLowerCase()), RenderType.translucent());
    }
}
