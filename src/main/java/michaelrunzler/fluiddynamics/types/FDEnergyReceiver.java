package michaelrunzler.fluiddynamics.types;

import net.minecraftforge.energy.EnergyStorage;

/**
 * A custom implementation of the Forge Energy Handler which supports direct modification of energy totals.
 * Only supports energy insertion.
 */
public class FDEnergyReceiver extends EnergyStorage
{
    public FDEnergyReceiver(int capacity, int receive) {
        super(capacity, receive, 0);
    }

    public void setEnergy(int newEnergy){
        this.energy = newEnergy;
    }
}
