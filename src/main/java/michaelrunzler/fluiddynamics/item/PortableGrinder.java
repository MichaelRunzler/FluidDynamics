package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * A portable version of the Molecular Decompiler, used to dissolve blocks and items into their constituents.
 * Uses Energy Cells for power, and can be recharged via crafting.
 */
public class PortableGrinder extends Item
{
    public static final int DURABILITY = 10;

    public PortableGrinder() {
        super(new Properties().tab(CreativeTabs.TAB_TOOLS).rarity(Rarity.UNCOMMON).setNoRepair().stacksTo(1).defaultDurability(DURABILITY));
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack)
    {
        ItemStack tmp;
        if(itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
            // If the PMD is at or (somehow) past its max damage value (i.e. it is out of charge/on its last charge),
            // replace it with its discharged version instead of the original item
            tmp = new ItemStack(RecipeGenerator.registryToItem("uncharged_portable_grinder"), itemStack.getCount());
        }else{
            // Increment the PMD's damage value by 1 to reflect its use in the recipe
            tmp = new ItemStack(itemStack.getItem(), itemStack.getCount());
            tmp.getItem().setDamage(tmp, itemStack.getDamageValue() + 1);
        }

        return tmp;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true; // The PMD's "container" is in fact itself
    }
}
