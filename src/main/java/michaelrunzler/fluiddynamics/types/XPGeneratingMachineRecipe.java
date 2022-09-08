package michaelrunzler.fluiddynamics.types;

import net.minecraft.world.level.ItemLike;

/**
 * Represents a single-in, multi-out recipe which generates XP upon completion, like a Vanilla smelting recipe.
 */
public class XPGeneratingMachineRecipe extends GenericMachineRecipe
{
    public float xp;

    public XPGeneratingMachineRecipe(int time, float xp, ItemLike in, RecipeIngredient... out)
    {
        super(time, in, out);
        this.xp = xp;
    }
}
