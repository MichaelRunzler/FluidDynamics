package michaelrunzler.fluiddynamics.machines.conduits.power_conduit;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PowerConduitBE extends PoweredMachineBE
{
    public PowerConduitBE(BlockPos pos, BlockState state, MachineEnum type) {
        super(pos, state, type, true, true, true);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) return (LazyOptional<T>) energyOpt;
        return super.getCapability(cap, side);
    }

    public void tickServer() {
        // This just acts as a small power cell. That's it.
        exportToNeighbors(Direction.values());
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // This "machine" doesn't have any inventory slots, so we don't care if items are valid or not
        return false;
    }
}
