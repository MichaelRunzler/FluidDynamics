package michaelrunzler.fluiddynamics.interfaces;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Tracks all registered Containers for the mod.
 */
public class ModContainers
{
    public static DeferredRegister<MenuType<?>> containers = DeferredRegister.create(ForgeRegistries.CONTAINERS, FluidDynamics.MODID);

    public static RegistryObject<MenuType<MFMDContainer>> CONTAINER_MFMD;

    public static void registerAllContainers()
    {
        CONTAINER_MFMD = containers.register(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase(), () -> IForgeMenuType.create(((windowId, inv, data) -> new MFMDContainer(windowId, data.readBlockPos(), inv, inv.player))));
    }
}
