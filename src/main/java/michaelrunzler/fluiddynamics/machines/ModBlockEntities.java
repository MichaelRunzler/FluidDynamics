package michaelrunzler.fluiddynamics.machines;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.machines.MFMD.MFMDBE;
import michaelrunzler.fluiddynamics.machines.centrifuge.CentrifugeBE;
import michaelrunzler.fluiddynamics.machines.e_furnace.EFurnaceBE;
import michaelrunzler.fluiddynamics.machines.ht_furnace.HTFurnaceBE;
import michaelrunzler.fluiddynamics.machines.power_cell.PowerCellBE;
import michaelrunzler.fluiddynamics.machines.purifier.PurifierBE;
import michaelrunzler.fluiddynamics.machines.rbe_generator.RsBeGenBE;
import michaelrunzler.fluiddynamics.machines.redstone_generator.RsGenBE;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Tracks all registered Block Entities for the mod.
 */
@SuppressWarnings("UnusedReturnValue")
public class ModBlockEntities
{
    public static DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, FluidDynamics.MODID);
    public static HashMap<String, RegistryObject<BlockEntityType<?>>> registeredBEs = new HashMap<>();

    @SuppressWarnings("ConstantConditions")
    public static void registerAllBEs()
    {
        registerBE(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase(), () -> BlockEntityType.Builder.of(MFMDBE::new,
                RecipeGenerator.registryToBlock(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase())).build(null));

        registerBE(MachineEnum.PURIFIER.name().toLowerCase(), () -> BlockEntityType.Builder.of(PurifierBE::new,
                RecipeGenerator.registryToBlock(MachineEnum.PURIFIER.name().toLowerCase())).build(null));

        registerBE(MachineEnum.CENTRIFUGE.name().toLowerCase(), () -> BlockEntityType.Builder.of(CentrifugeBE::new,
                RecipeGenerator.registryToBlock(MachineEnum.CENTRIFUGE.name().toLowerCase())).build(null));

        registerBE(MachineEnum.E_FURNACE.name().toLowerCase(), () -> BlockEntityType.Builder.of(EFurnaceBE::new,
                RecipeGenerator.registryToBlock(MachineEnum.E_FURNACE.name().toLowerCase())).build(null));

        registerBE(MachineEnum.HT_FURNACE.name().toLowerCase(), () -> BlockEntityType.Builder.of(HTFurnaceBE::new,
                RecipeGenerator.registryToBlock(MachineEnum.HT_FURNACE.name().toLowerCase())).build(null));

        registerBE(MachineEnum.POWER_CELL.name().toLowerCase(), () -> BlockEntityType.Builder.of(PowerCellBE::new,
                RecipeGenerator.registryToBlock(MachineEnum.POWER_CELL.name().toLowerCase())).build(null));

        registerBE(MachineEnum.RS_GENERATOR.name().toLowerCase(), () -> BlockEntityType.Builder.of(RsGenBE::new,
                RecipeGenerator.registryToBlock(MachineEnum.RS_GENERATOR.name().toLowerCase())).build(null));

        registerBE(MachineEnum.RBE_GENERATOR.name().toLowerCase(), () -> BlockEntityType.Builder.of(RsBeGenBE::new,
                RecipeGenerator.registryToBlock(MachineEnum.RBE_GENERATOR.name().toLowerCase())).build(null));
    }

    /**
     * Registers a block entity with the BE Registry with the specified ID, then adds the resulting registry entry to the registered BE map.
     */
    public static RegistryObject<BlockEntityType<?>> registerBE(String id, Supplier<? extends BlockEntityType<?>> be)
    {
        RegistryObject<BlockEntityType<?>> obj = blockEntities.register(id, be);
        registeredBEs.put(id, obj);

        return obj;
    }
}
