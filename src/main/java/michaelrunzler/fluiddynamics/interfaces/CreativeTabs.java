package michaelrunzler.fluiddynamics.interfaces;

import michaelrunzler.fluiddynamics.recipes.RecipeGenerator;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeTabs
{
    public static final CreativeModeTab TAB_BLOCKS = new CreativeModeTab("tab_blocks") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(RecipeGenerator.registryToItem("ore_native_copper"));
        }
    };

    public static final CreativeModeTab TAB_RESOURCES = new CreativeModeTab("tab_resources") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(RecipeGenerator.registryToItem("ingot_copper"));
        }
    };

    public static final CreativeModeTab TAB_COMPONENTS = new CreativeModeTab("tab_components") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(RecipeGenerator.registryToItem("redstone_dynamo"));
        }
    };

    public static final CreativeModeTab TAB_ARMOR = new CreativeModeTab("tab_armor") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(RecipeGenerator.registryToItem("armor_chest_copper"));
        }
    };

    public static final CreativeModeTab TAB_TOOLS = new CreativeModeTab("tab_tools") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(RecipeGenerator.registryToItem("pickaxe_copper"));
        }
    };

    public static final CreativeModeTab TAB_MACHINES = new CreativeModeTab("tab_machines") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(RecipeGenerator.registryToItem("molecular_decompiler"));
        }
    };
}
