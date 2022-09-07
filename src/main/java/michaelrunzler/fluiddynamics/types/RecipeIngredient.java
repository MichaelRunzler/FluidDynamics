package michaelrunzler.fluiddynamics.types;

import net.minecraft.world.level.ItemLike;

/**
 * Represents a simpler version of the ItemStack, contianing a paired Item and its quantity.
 * @param ingredient
 * @param count
 */
public record RecipeIngredient(ItemLike ingredient, int count) {
}
