package michaelrunzler.fluiddynamics.types;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.energy.EnergyStorage;

/**
 * A derivative of the Forge Energy Storage type which allows direct access to and modification of energy storage totals.
 * Can support bi-directional, receive-only, send-only, or read-only energy buffer types.
 */
public class FDEnergyStorage extends EnergyStorage
{
    /**
     * Standard constructor for a storage buffer which can both send and receive (so long as both transfer stats are >0).
     * Sending or receiving can be disabled by setting one or both of the transfer stats to zero.
     */
    public FDEnergyStorage(int capacity, int maxReceive, int maxSend) {
        super(capacity, maxReceive, maxSend);
    }

    /**
     * Constructs a storage buffer with the same send and receive limits.
     * Identical to calling {@link FDEnergyStorage#FDEnergyStorage(int, int, int)} with maxReceive and maxSend equal.
     */
    public FDEnergyStorage(int capacity, int transfer){
        super(capacity, transfer);
    }

    /**
     * Modifies the buffered energy total for this storage.
     * Checks to ensure that the energy total is within bounds first.
     */
    public void setEnergy(int newEnergy)
    {
        if(newEnergy < 0 || newEnergy > this.getMaxEnergyStored())
            throw new IllegalArgumentException("Requested energy total (" + newEnergy + ") is outside of bounds [0," + this.getMaxEnergyStored() + "]");

        this.energy = newEnergy;
    }

    /**
     * Copied from {@link EnergyStorage} with some modifications.
     */
    @Override
    public void deserializeNBT(Tag nbt)
    {
        if (!(nbt instanceof IntTag intNbt))
            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
        this.setEnergy(intNbt.getAsInt());
    }
}
