package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.function.Consumer;

/**
 * A single-use Redstone-Beryllium energy cell item which becomes depleted after use and may be recharged through crafting.
 */
public class EnergyCell extends Item
{
    public static final int DURABILITY = 100;

    public EnergyCell() {
        super(new Properties().tab(CreativeTabs.TAB_COMPONENTS).stacksTo(1).defaultDurability(DURABILITY).setNoRepair().rarity(Rarity.COMMON));
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken)
    {
        //TODO generalize to handle depletion by block entities and non-mainhand sources (i.e. in-inventory charging)

        // Convert the cell into a depleted cell when its durability drops below 0
        int dmg = super.damageItem(stack, amount, entity, onBroken);

        if(this.getDamage(stack) == stack.getMaxDamage())
            entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.registeredItems.get("depleted_cell").get(), 1));
        return dmg;
    }
}
