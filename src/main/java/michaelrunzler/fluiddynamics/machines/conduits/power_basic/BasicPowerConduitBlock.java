package michaelrunzler.fluiddynamics.machines.conduits.power_basic;

import michaelrunzler.fluiddynamics.machines.conduits.BasePowerConduitBlock;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicPowerConduitBlock extends BasePowerConduitBlock
{
    public BasicPowerConduitBlock() {
        super(MachineEnum.POWER_CONDUIT_BASIC);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BasicPowerConduitBE(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type)
    {
        // Gets the ticker instance from the BE
        if(!level.isClientSide()) {
            return (lvl, pos, bstate, tile) -> {
                if(tile instanceof BasicPowerConduitBE) ((BasicPowerConduitBE) tile).tickServer();
            };
        }else return null;
    }


}
