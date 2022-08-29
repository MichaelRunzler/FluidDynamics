package michaelrunzler.fluiddynamics.blockentity;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.block.ModBlocks;
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
public class ModBlockEntities
{
    public static DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, FluidDynamics.MODID);
    public static HashMap<String, RegistryObject<BlockEntityType<?>>> registeredBEs = new HashMap<>();

    @SuppressWarnings("ConstantConditions")
    public static void registerAllBEs()
    {
        registerBE(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase(), () -> BlockEntityType.Builder.of(MFMDBE::new,
                ModBlocks.registeredBlocks.get(MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase()).get()).build(null));
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
