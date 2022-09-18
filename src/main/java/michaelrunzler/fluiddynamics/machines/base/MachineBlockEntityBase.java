package michaelrunzler.fluiddynamics.machines.base;

import michaelrunzler.fluiddynamics.machines.ModBlockEntities;
import michaelrunzler.fluiddynamics.types.IInventoriedBE;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Represents the base model class for any mod block entities.
 * Must be overridden and paired with a block definition to form a functional block.
 */
public abstract class MachineBlockEntityBase extends BlockEntity implements IInventoriedBE
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

    /**
     * Drops a properly valued XP orb in the world at the position of the block entity generating the orb.
     * Fractional XP values will be dropped as 1 XP, randomly weighted by their magnitude.
     * See the Vanilla Furnace code for more info.
     */
    protected void dropXP(float amount)
    {
        if(level == null || level.isClientSide) return;

        // Copied from the Vanilla Furnace XP code; obtains the fractional and whole components of the XP amount.
        // If the fractional amount is nonzero, randomly award one whole XP point or no additional XP, weighted upon
        // the magnitude of the fractional component. This results in the "bonus" XP (beyond the whole part) being awarded
        // the correct amount of the time.
        int i = Mth.floor(amount);
        float f = Mth.frac(amount);
        if (f != 0.0F && Math.random() < (double)f) i++;

        // Drop the XP orb if its amount is nonzero
        if(i > 0)
            ExperienceOrb.award((ServerLevel)level, new Vec3(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()), i);
    }

    public abstract boolean isItemValid(int slot, @NotNull ItemStack stack);

    public int getNumSlots() {
        return type.numInvSlots;
    }
}
