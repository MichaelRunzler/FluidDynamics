package michaelrunzler.fluiddynamics.recipes;

import net.minecraft.world.level.ItemLike;

/**
 * Represents a generic single-in, multi-out recipe for a FD machine.
 */
public class GenericMachineRecipe
{
    public int time;
    public ItemLike in;
    public ItemLike[] out;

    public GenericMachineRecipe(int time, ItemLike in, ItemLike... out)
    {
        this.time = time;
        this.in = in;
        this.out = out;
    }
}
