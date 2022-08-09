package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.item.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class FDModelProvider extends ItemModelProvider
{
    public FDModelProvider(DataGenerator gen, ExistingFileHelper helper){
        super(gen, FluidDynamics.MODID, helper);
    }

    @Override
    protected void registerModels()
    {
        // Register each item with their textures; special cases can be handled individually
        ModItems.registeredItems.forEach((k, v) -> singleTexture(v.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/" + k)));
        ModBlockItems.registeredBItems.forEach((k, v) -> withExistingParent(v.getId().getPath(), modLoc("block/" + k)));
    }
}
