package michaelrunzler.fluiddynamics.machines.conduits;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * A variant of the basic Conduit which conducts Rs-Be Energy, and connects to blocks which can produce or consume energy.
 */
public abstract class BasePowerConduitBlock extends BaseConduitBlock
{
    public BasePowerConduitBlock(MachineEnum type) {
        super(type);
    }

    @Override
    protected boolean isNeighborValid(@NotNull Level level, @NotNull BlockPos neighborPos) {
        return level.getBlockEntity(neighborPos) instanceof PoweredMachineBE pbe && (pbe.canImportPower || pbe.canExportPower);
    }
}
