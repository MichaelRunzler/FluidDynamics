package michaelrunzler.fluiddynamics.block;

import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
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
@SuppressWarnings({"NullableProblems", "deprecation"})
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

    /**
     * Overriding classes should not modify this method. unless they need a specific render shape.
     * @param state the blockstate that is being evaluated for its render shape
     * @param getter used to get the block/BE from the BlockPos
     * @param pos the position of the block/BE being modified
     * @return the current state's occlusion shape
     */
    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos) {
        return RENDER_SHAPE;
    }

    /**
     * Overriding classes should use this method to generate a BlockEntity for a given BlockState by instantiating a new
     * instance of the associated BlockEntity subclass.
     * @param pos the position of the block being queried
     * @param state the current BlockState
     */
    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state);

    /**
     * Overriding classes should use this method to tick either the client or server depending on what they need to do.
     * Typically, machines only need to tick the server for inventory and BE updates, so nothing should be done for
     * level.isClientSide() == true. Otherwise, tick the server if the associated BE is the BE being ticked.
     * @param level the level doing the tick
     * @param state the current block state
     * @param type a reference to the BE being ticked
     * @return a lambda which ticks the server under specific conditions
     */
    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type);

    /**
     * Gets the default BlockState when a block is first placed. This defaults to POWERED = false, but additional states
     * can be added by overriding this method and modifying the State returned by super(ctx).
     * @return the completed BlockState
     */
    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(BlockStateProperties.POWERED, false);
    }

    /**
     * Treated as above; call builder.add(StateProperty) after calling super(builder) to add more properties.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Override as above for more properties
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWERED);
    }

    /**
     * Called when a Player right-clicks on the block. Overriding classes should use this method to open a GUI or change
     * states as required, typically using a GUIProvider lambda.
     * @param state the current state of the block
     * @param level the level in which the right-click is being performed
     * @param pos the position of the block being clicked
     * @param player the player clicking the block
     * @param hand the hand being used to use the block
     * @param result the actual use interaction result
     * @return the result of the interaction; typically InteractionResult.SUCCESS if nothing went wrong
     */
    @Override
    public abstract InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                 @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result);

    @Override
    public PushReaction getPistonPushReaction(@NotNull BlockState state) {
        // This just stops the block from being pushed around
        return PushReaction.BLOCK;
    }
}