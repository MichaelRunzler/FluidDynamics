package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.block.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.ArrayList;

/**
 * Serves block-states for all standard and TE blocks.
 */
public class FDBlockStateProvider extends BlockStateProvider
{
    public static ArrayList<String> nonDefaultModelBlocks = new ArrayList<>();

    public FDBlockStateProvider(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, FluidDynamics.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Register each standard block as a SimpleBlock by running through the registered block dictionary
        ModBlocks.registeredBlocks.forEach((k, v) -> {
            if(!nonDefaultModelBlocks.contains(k)) simpleBlock(v.get());
        });
    }
}
