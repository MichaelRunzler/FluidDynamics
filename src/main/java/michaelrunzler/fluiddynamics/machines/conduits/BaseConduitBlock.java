package michaelrunzler.fluiddynamics.machines.conduits;

import michaelrunzler.fluiddynamics.machines.base.FDMachineBase;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A base class which represents the general behavior expected of a conduit-type block - that is, a block capable of
 * "connecting" to neighboring blocks of the same type, or blocks satisfying a specific condition (for example, blocks
 * which can accept power). The model for this type of block is expected to be a multipart model which uses the Vanilla
 * Direction property to determine its shape.
 */
@SuppressWarnings("deprecation")
public abstract class BaseConduitBlock extends FDMachineBase
{
    public BaseConduitBlock(MachineEnum type) {
        super(type);
    }

    /**
     * This can be overridden by subclasses if right-click behavior is desired.
     */
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result) {
        // There isn't a GUI or other interaction, so don't do anything
        return InteractionResult.PASS;
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos, @NotNull PathComputationType type) {
        return false;
    }

    @Override
    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(BlockStateProperties.UP);
        builder.add(BlockStateProperties.DOWN);
        builder.add(BlockStateProperties.NORTH);
        builder.add(BlockStateProperties.SOUTH);
        builder.add(BlockStateProperties.EAST);
        builder.add(BlockStateProperties.WEST);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState blockState = this.defaultBlockState();
        // Check all neighbors for validity when placing
        for(Direction d : Direction.values())
            blockState = updateFromNeighbors(context.getClickedPos(), context.getClickedPos().relative(d), blockState, context.getLevel());
        return blockState;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighbor, @NotNull BlockPos neighborPos, boolean simulate)
    {
        BlockState newState = updateFromNeighbors(pos, neighborPos, state, level);
        if(state != newState)
            level.setBlockAndUpdate(pos, newState); // Only update if any changes were made
        super.neighborChanged(newState, level, pos, neighbor, neighborPos, simulate);
    }

    /**
     * Called when updating the conduit's state from its neighbors. Should return true if the conduit should connect to
     * the neighbor at the specified position, or false if it should not connect.
     */
    protected abstract boolean isNeighborValid(@NotNull Level level, @NotNull BlockPos neighborPos);

    /**
     * Updates this block's neighboring connection state based upon a neighboring block.
     */
    protected BlockState updateFromNeighbors(@NotNull BlockPos pos, @NotNull BlockPos neighborPos, @NotNull BlockState state, @NotNull Level level)
    {
        // If the neighbor is capable of transmitting or receiving power, form a connection to it; otherwise, ensure
        // that there is no connection present
        BooleanProperty dirProp = getDirProperty(pos, neighborPos);
        if(dirProp != null) state = state.setValue(dirProp, isNeighborValid(level, neighborPos));
        return state;
    }

    /**
     * Converts two absolute positions into their relative direction offset. Note that this is only designed to work in one
     * axis at a time, and goes in the order (x,y,z).
     */
    protected static @Nullable BooleanProperty getDirProperty(@NotNull BlockPos pos, @NotNull BlockPos neighborPos)
    {
        // Get offsets in each direction, then map those offsets to a direction
        int xOff = pos.getX() - neighborPos.getX();
        int yOff = pos.getY() - neighborPos.getY();
        int zOff = pos.getZ() - neighborPos.getZ();

        if(xOff < 0) return BlockStateProperties.EAST;
        else if(xOff > 0) return BlockStateProperties.WEST;
        else if(yOff < 0) return BlockStateProperties.DOWN;
        else if(yOff > 0) return BlockStateProperties.UP;
        else if(zOff < 0) return BlockStateProperties.SOUTH;
        else if(zOff > 0) return BlockStateProperties.NORTH;
        else return null; // In this case, the neighbor IS this block
    }
}
