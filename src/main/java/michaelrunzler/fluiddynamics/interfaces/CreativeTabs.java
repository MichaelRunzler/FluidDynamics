package michaelrunzler.fluiddynamics.interfaces;

import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.item.ModItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTabs
{
    public static final CreativeModeTab TAB_BLOCKS = new CreativeModeTab("tab_blocks") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlockItems.registeredBItems.get("ore_native_copper").get());
        }
    };

    public static final CreativeModeTab TAB_ITEMS = new CreativeModeTab("tab_items") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.registeredItems.get("ingot_copper").get());
        }
    };
}
