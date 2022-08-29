package michaelrunzler.fluiddynamics.types;

import net.minecraftforge.energy.EnergyStorage;

/**
 * A custom implementation of the Forge Energy Handler which supports direct modification of energy totals.
 * Only supports energy extraction.
 */
public class FDEnergyGenerator extends EnergyStorage
{
    public FDEnergyGenerator(int capacity, int send) {
        super(capacity, 0, send);
    }

    public void setEnergy(int newEnergy){
        this.energy = newEnergy;
    }
}
