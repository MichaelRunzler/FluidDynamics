package michaelrunzler.fluiddynamics.machines.conduits.power_conduit;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.machines.conduits.BaseConduitBlock;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A variant of the basic Conduit which conducts Rs-Be Energy, and connects to blocks which can produce or consume energy.
 */
public class PowerConduitBlock extends BaseConduitBlock
{
    public PowerConduitBlock(MachineEnum type) {
        super(type);
    }

    @Override
    protected boolean isNeighborValid(@NotNull Level level, @NotNull BlockPos neighborPos) {
        return level.getBlockEntity(neighborPos) instanceof PoweredMachineBE pbe && (pbe.canImportPower || pbe.canExportPower);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PowerConduitBE(pos, state, type);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type)
    {
        // Gets the ticker instance from the BE
        if(!level.isClientSide()) {
            return (lvl, pos, bstate, tile) -> {
                if(tile instanceof PowerConduitBE) ((PowerConduitBE) tile).tickServer();
            };
        }else return null;
    }
}
