package michaelrunzler.fluiddynamics.types;

/**
 * Dictates how a {@link michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE} should handle power-transfer interactions
 * with other BEs.
 */
public enum PowerInteraction
{
    MACHINE(false), STORAGE(true), CONDUIT(true);

    public final boolean doBalancing; // If true, this type will attempt to balance power between adjacent blocks of the same type instead of just exporting

    PowerInteraction(boolean doBalancing){
        this.doBalancing = doBalancing;
    }
}
