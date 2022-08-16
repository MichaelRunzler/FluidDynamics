package michaelrunzler.fluiddynamics.types;

import michaelrunzler.fluiddynamics.item.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

/**
 * Translates a {@link MaterialEnum} into its {@link Tier} equivalent.
 */
public class FDToolTier implements Tier
{
    private final MaterialEnum mat;

    public FDToolTier(MaterialEnum mat) {
        this.mat = mat;
    }

    @Override
    public int getUses() {
        return (int)(((float)Tiers.IRON.getUses()) * mat.toolDur);
    }

    @Override
    public float getSpeed() {
        return Tiers.IRON.getSpeed() * mat.toolMult;
    }

    @Override
    public float getAttackDamageBonus() {
        return Tiers.IRON.getAttackDamageBonus() * mat.toolMult;
    }

    @Override
    public int getLevel() {
        return mat.toolTier;
    }

    @Override
    public int getEnchantmentValue() {
        return mat.enchantability;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.registeredItems.get("ingot_" + mat.name().toLowerCase()).get());
    }
}
