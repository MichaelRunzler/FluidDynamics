package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.types.FDArmorMaterial;
import michaelrunzler.fluiddynamics.types.MaterialEnum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Class that represents an armor item of a given material type.
 */
public class FDArmorItem extends ArmorItem
{
    private final MaterialEnum mat;

    public FDArmorItem(MaterialEnum mat, EquipmentSlot slot) {
        super(new FDArmorMaterial(mat), slot, new Properties().tab(CreativeTabs.TAB_ARMOR).rarity(Rarity.UNCOMMON).stacksTo(1));
        this.mat = mat;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return FluidDynamics.MODID + ":models/armor/armor_" + mat.name().toLowerCase() + "_" + (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
    }
}
