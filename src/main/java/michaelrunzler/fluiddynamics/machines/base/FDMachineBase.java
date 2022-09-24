package michaelrunzler.fluiddynamics.machines.base;

import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Represents a block which can take the properties of any {@link MachineEnum} type.
 * Must be subclassed and use a separate BE class in order to represent a functional block.
 */
@SuppressWarnings({"NullableProblems", "deprecation"})
public abstract class FDMachineBase extends Block implements EntityBlock
{
    protected final MachineEnum type;
    protected boolean doItemDrops; // If set to false, this block will not attempt to drop its contents when it is destroyed
    private static final VoxelShape RENDER_SHAPE = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    public FDMachineBase(MachineEnum type)
    {
        super(Properties.of(Material.METAL)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .strength(type.strength)
                .lightLevel((state) -> {
                    if(state.hasProperty(BlockStateProperties.POWERED))
                        return state.getValue(BlockStateProperties.POWERED) ? 12 : 0;
                    else return 0;
                }));

        this.doItemDrops = true;
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
     * Gets the default BlockState when a block is first placed. Defaults to orienting the block to the player facing.
     * @return the completed BlockState
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState blockstate = super.getStateForPlacement(context);
        if (blockstate != null) {
            blockstate = blockstate.setValue(BlockStateProperties.FACING, context.getHorizontalDirection());
            blockstate = blockstate.setValue(BlockStateProperties.POWERED, false);
        }

        return blockstate;
    }

    /**
     * Treated as above; call builder.add(StateProperty) after calling super(builder) to add more properties.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Override as above for more properties
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.FACING);
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

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean p_60519_)
    {
        // If this is a removal as a consequence of a state change, don't do anything and just delegate to super
        if(!newState.is(this) && doItemDrops)
        {
            // Drop all inventory items from the block before it is destroyed
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null)
                be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                    for (int i = 0; i < c.getSlots(); i++) spawnItem(level, pos, c.getStackInSlot(i));
                });
        }

        super.onRemove(state, level, pos, newState, p_60519_);
    }

    /**
     * Modified from the 1.14 version of the InventoryHelper class, now removed.
     */
    protected static void spawnItem(Level lvl, BlockPos pos, ItemStack stack)
    {
        Random rng = new Random();

        // Give the exiting item a random velocity and direction
        double w = EntityType.ITEM.getWidth();
        double rem = 1.0d - w;
        double mid = w / 2.0d;
        double dx = ((double)pos.getX()) + (rng.nextDouble() * rem) + mid;
        double dy = ((double)pos.getY()) + (rng.nextDouble() * rem);
        double dz = ((double)pos.getZ()) + (rng.nextDouble() * rem) + mid;

        ItemEntity ent = new ItemEntity(lvl, dx, dy, dz, stack);
        ent.setDeltaMovement(rng.nextGaussian() * 0.05d, rng.nextGaussian() * 0.25d, rng.nextGaussian() * 0.05d);
        lvl.addFreshEntity(ent);
    }
}