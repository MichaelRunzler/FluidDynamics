package michaelrunzler.fluiddynamics.blockentity;

import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.checkerframework.checker.units.qual.A;
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

    public MachineBlockEntityBase(BlockPos pos, BlockState state, MachineEnum type) {
        super(ModBlockEntities.registeredBEs.get(type.name().toLowerCase()).get(), pos, state);

        this.optionals = new ArrayList<>();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // Invalidate all optional handlers on removal
        for(LazyOptional<?> o : optionals) o.invalidate();
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_); // TODO stopped here
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return super.getCapability(cap, side);
    }
}
