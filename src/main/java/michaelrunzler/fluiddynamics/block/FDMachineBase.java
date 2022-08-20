package michaelrunzler.fluiddynamics.block;

import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a block which can take the properties of any {@link MachineEnum} type.
 * Must be subclassed and use a separate BE class in order to represent a functional block.
 */
@SuppressWarnings("NullableProblems")
public abstract class FDMachineBase extends Block implements EntityBlock
{
    protected final MachineEnum type;
    private static final VoxelShape RENDER_SHAPE = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    public FDMachineBase(MachineEnum type)
    {
        super(Properties.of(Material.METAL)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .strength(type.strength)
                .lightLevel((state) -> state.getValue(BlockStateProperties.POWERED) ? 12 : 0));

        this.type = type;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos) {
        return RENDER_SHAPE;
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state);

    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type);

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
        // This should be overridden by subclasses which need additional states
        return super.getStateForPlacement(ctx).setValue(BlockStateProperties.POWERED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        // Override as above for more properties
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    public abstract InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                 @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result);

    @Override
    public PushReaction getPistonPushReaction(@NotNull BlockState state) {
        // This just stops the block from being pushed around
        return PushReaction.BLOCK;
    }
}

/**
 * Used to get the properties of a completed Machine object without instantiating a Registry call.
 */
class MachineBaseHelper
{
        MachineEnum type;
        TagKey<Block>[] tags;
        String name;
        String englishName;

        @SuppressWarnings("unchecked")
        MachineBaseHelper(MachineEnum type)
        {
            this.type = type;

            tags = new TagKey[]{BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL};
            this.name = "block_" + type.name().toLowerCase();
            this.englishName = type.englishName + " Block";
        }
}