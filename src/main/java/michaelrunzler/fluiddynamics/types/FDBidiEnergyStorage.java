package michaelrunzler.fluiddynamics.types;

import net.minecraftforge.energy.EnergyStorage;

/**
 * A custom implementation of the Forge Energy Handler which supports direct modification of energy totals.
 */
public class FDBidiEnergyStorage extends EnergyStorage
{
    public FDBidiEnergyStorage(int capacity, int maxTransfer){
        super(capacity, maxTransfer);
    }

    public void setEnergy(int newEnergy){
        this.energy = newEnergy;
    }
}
