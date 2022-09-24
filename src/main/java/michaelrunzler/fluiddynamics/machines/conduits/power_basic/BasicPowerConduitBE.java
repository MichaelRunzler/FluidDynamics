package michaelrunzler.fluiddynamics.machines.conduits.power_basic;

import michaelrunzler.fluiddynamics.machines.conduits.BasePowerConduitBE;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicPowerConduitBE extends BasePowerConduitBE
{
    public BasicPowerConduitBE(BlockPos pos, BlockState state) {
        super(pos, state, MachineEnum.POWER_CONDUIT_BASIC);
    }
}
