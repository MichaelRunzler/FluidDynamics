package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.block.ModBlocks;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Serves block-states for all standard and TE blocks.
 */
public class FDBlockStateProvider extends BlockStateProvider
{
    public FDBlockStateProvider(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, FluidDynamics.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Register each standard block as a SimpleBlock by running through the registered block dictionary
        ModBlocks.registeredBlocks.forEach((k, v) -> simpleBlock(v.get()));

        modelMFMD();
    }

    private void modelMFMD()
    {
        BlockModelBuilder model = models().getBuilder("block/machine/mfmd/main");
        model.parent(models().getExistingFile(mcLoc("cube")));

        cube(model, "default", 0f, 0f, 0f, 16f, 16f, 16f);

        model.texture("default", modLoc("block/" + MachineEnum.MOLECULAR_DECOMPILER.name().toLowerCase()));
    }

    /**
     * Generates a cube in the given model builder spanning from the given initial coordinates to the final coordinates.
     * Textures all sides of the cube with the given texture ID.
     */
    private void cube(BlockModelBuilder b, String texID, float ix, float iy, float iz, float fx, float fy, float fz) {
        b.element().from(ix, iy, iz).to(fx, fy, fz).allFaces(((direction, faceBuilder) -> faceBuilder.texture("#" + texID))).end();
    }
}
