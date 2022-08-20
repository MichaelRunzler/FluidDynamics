package michaelrunzler.fluiddynamics.blockentity;

import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Represents the base model class for any mod block entities.
 * Must be overridden and paired with a block definition to form a functional block.
 */
public abstract class MachineBlockEntityBase extends BlockEntity
{
    protected MachineEnum type;
    protected ArrayList<LazyOptional<?>> optionals;

    /**
     * Overriding classes should declare LazyOptionals containing Handlers for each function that the BE provides
     * (storage, power, RS output, etc) and load/unload data from them in the load() and saveAdditional() methods.
     * All of these handlers must be declared as capabilities in the getCapability() method.
     */
    public MachineBlockEntityBase(BlockPos pos, BlockState state, MachineEnum type) {
        super(ModBlockEntities.registeredBEs.get(type.name().toLowerCase()).get(), pos, state);

        this.optionals = new ArrayList<>();

        this.type = type;
    }

    /**
     * Subclasses should not override this method unless they need to do something special when the BE is removed.
     */
    @Override
    public void setRemoved() {
        super.setRemoved();
        // Invalidate all optional handlers on removal
        for(LazyOptional<?> o : optionals) o.invalidate();
    }

    /**
     * Overriding classes should use this method to load NBT data which was stored when the block was unloaded.
     * @param tag the NBT data to load from
     */
    @Override
    public abstract void load(@NotNull CompoundTag tag);

    /**
     * Overriding classes should use this method to save any data which needs to be stored while the block is unloaded.
     * @param tag the NBT tag to save data to
     */
    @Override
    protected abstract void saveAdditional(@NotNull CompoundTag tag);

    /**
     * Subclasses should declare all additional capabilities here, with each being represented as a LazyOptional cast
     * to its proper type on return. Directional handling is also possible (I-sidedness) via the provided Direction.
     * @param cap The capability to check
     * @param side The Side to check from, with null being internal checks (i.e. from the BE itself)
     * @return the LazyOptional containing the handler for the given capability and side, or a call to super if none
     * is available
     */
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return super.getCapability(cap, side);
    }
}
