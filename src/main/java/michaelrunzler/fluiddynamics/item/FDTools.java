package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import michaelrunzler.fluiddynamics.types.FDToolTier;
import michaelrunzler.fluiddynamics.types.MaterialEnum;
import net.minecraft.world.item.*;

// Contains classes that represent tools made of a given material.

class FDPickaxe extends PickaxeItem
{
    public FDPickaxe(MaterialEnum mat) {
        super(new FDToolTier(mat), 1, -2.8f, new Properties().tab(CreativeTabs.TAB_TOOLS).stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}

class FDAxe extends AxeItem
{
    public FDAxe(MaterialEnum mat) {
        super(new FDToolTier(mat), 6, -3.0f, new Properties().tab(CreativeTabs.TAB_TOOLS).stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}

class FDSpade extends ShovelItem
{
    public FDSpade(MaterialEnum mat) {
        super(new FDToolTier(mat), 1, -3.0f, new Properties().tab(CreativeTabs.TAB_TOOLS).stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}

class FDHoe extends HoeItem
{
    public FDHoe(MaterialEnum mat) {
        super(new FDToolTier(mat), -(int)(mat.toolMult * Tiers.IRON.getAttackDamageBonus()), -3.0f, new Properties().tab(CreativeTabs.TAB_TOOLS).stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}

class FDSword extends SwordItem
{
    public FDSword(MaterialEnum mat) {
        super(new FDToolTier(mat), 3, -2.4f, new Properties().tab(CreativeTabs.TAB_TOOLS).stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}