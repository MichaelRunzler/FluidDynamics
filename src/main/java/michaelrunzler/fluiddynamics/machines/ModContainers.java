package michaelrunzler.fluiddynamics.machines;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.machines.MFMD.MFMDContainer;
import michaelrunzler.fluiddynamics.machines.centrifuge.CentrifugeContainer;
import michaelrunzler.fluiddynamics.machines.e_furnace.EFurnaceContainer;
import michaelrunzler.fluiddynamics.machines.ht_furnace.HTFurnaceContainer;
import michaelrunzler.fluiddynamics.machines.power_cell.PowerCellContainer;
import michaelrunzler.fluiddynamics.machines.purifier.PurifierContainer;
import michaelrunzler.fluiddynamics.machines.rbe_generator.RsBeGenContainer;
import michaelrunzler.fluiddynamics.machines.redstone_generator.RsGenContainer;
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
    public static RegistryObject<MenuType<PurifierContainer>> CONTAINER_PURIFIER;
    public static RegistryObject<MenuType<CentrifugeContainer>> CONTAINER_CENTRIFUGE;
    public static RegistryObject<MenuType<EFurnaceContainer>> CONTAINER_EFURNACE;
    public static RegistryObject<MenuType<HTFurnaceContainer>> CONTAINER_HTFURNACE;
    public static RegistryObject<MenuType<PowerCellContainer>> CONTAINER_POWERCELL;
    public static RegistryObject<MenuType<RsGenContainer>> CONTAINER_RSGEN;
    public static RegistryObject<MenuType<RsBeGenContainer>> CONTAINER_RBEGEN;

    public static void registerAllContainers()
    {
        CONTAINER_MFMD = containers.register(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase(), () -> IForgeMenuType.create((windowId, inv, data) -> new MFMDContainer(windowId, data.readBlockPos(), inv, inv.player)));
        CONTAINER_PURIFIER = containers.register(MachineEnum.PURIFIER.name().toLowerCase(), () -> IForgeMenuType.create((windowId, inv, data) -> new PurifierContainer(windowId, data.readBlockPos(), inv, inv.player)));
        CONTAINER_CENTRIFUGE = containers.register(MachineEnum.CENTRIFUGE.name().toLowerCase(), () -> IForgeMenuType.create((windowId, inv, data) -> new CentrifugeContainer(windowId, data.readBlockPos(), inv, inv.player)));
        CONTAINER_EFURNACE = containers.register(MachineEnum.E_FURNACE.name().toLowerCase(), () -> IForgeMenuType.create(((windowId, inv, data) -> new EFurnaceContainer(windowId, data.readBlockPos(), inv, inv.player))));
        CONTAINER_HTFURNACE = containers.register(MachineEnum.HT_FURNACE.name().toLowerCase(), () -> IForgeMenuType.create(((windowId, inv, data) -> new HTFurnaceContainer(windowId, data.readBlockPos(), inv, inv.player))));
        CONTAINER_POWERCELL = containers.register(MachineEnum.POWER_CELL.name().toLowerCase(), () -> IForgeMenuType.create(((windowId, inv, data) -> new PowerCellContainer(windowId, data.readBlockPos(), inv, inv.player))));
        CONTAINER_RSGEN = containers.register(MachineEnum.RS_GENERATOR.name().toLowerCase(), () -> IForgeMenuType.create(((windowId, inv, data) -> new RsGenContainer(windowId, data.readBlockPos(), inv, inv.player))));
        CONTAINER_RBEGEN = containers.register(MachineEnum.RBE_GENERATOR.name().toLowerCase(), () -> IForgeMenuType.create(((windowId, inv, data) -> new RsBeGenContainer(windowId, data.readBlockPos(), inv, inv.player))));
    }
}
