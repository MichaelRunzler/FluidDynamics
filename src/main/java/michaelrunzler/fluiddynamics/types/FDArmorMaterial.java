package michaelrunzler.fluiddynamics.types;

import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

/**
 * Translates a {@link MaterialEnum} into its {@link ArmorMaterial} equivalent.
 */
public class FDArmorMaterial implements ArmorMaterial
{
    public MaterialEnum mat;

    public FDArmorMaterial(MaterialEnum mat) {
        this.mat = mat;
    }

    @Override
    public int getDurabilityForSlot(@NotNull EquipmentSlot slot) {
        return (int)(ArmorMaterials.IRON.getDurabilityForSlot(slot) * mat.armorDur);
    }

    @Override
    public int getDefenseForSlot(@NotNull EquipmentSlot slot)
    {
        int value = 0;
        int truncatedValue = Math.min(mat.armorVal, 20);
        // These values use a progressive distribution, rounded to the nearest integer:
        // 40% is distributed to the chestplate
        // 30% is distributed to the leggings
        // 15% is distributed to the helmet
        // The remainder is distributed to the boots
        int chestVal = Math.round(truncatedValue * 0.40f);
        int legVal = Math.round(truncatedValue * 0.30f);
        int helmVal = Math.round(truncatedValue * 0.15f);
        int bootVal = truncatedValue - (chestVal + legVal + helmVal);

        switch (slot){
            case HEAD -> value = helmVal;
            case CHEST -> value = chestVal;
            case LEGS -> value = legVal;
            case FEET -> value = bootVal;
        }

        return value;
    }

    @Override
    public int getEnchantmentValue() {
        return mat.enchantability;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return ArmorMaterials.IRON.getEquipSound();
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(RecipeGenerator.registryToItem("ingot_" + mat.name().toLowerCase()));
    }

    @Override
    public @NotNull String getName() {
        return mat.name();
    }

    @Override
    public float getToughness() {
        return mat.armorVal > 20 ? mat.armorVal - 20 : 0; // If the armor value exceeds 20, add the remainder as toughness
    }

    @Override
    public float getKnockbackResistance() {
        return 0; // No armor in this mod has natural KB resist
    }
}
