package michaelrunzler.fluiddynamics.machines.MFMD;

import michaelrunzler.fluiddynamics.machines.base.FDMachineBase;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Represents the physical block associated with a machine.
 */
public class MFMDBlock extends FDMachineBase
{
    public static final String SCREEN_TITLE = "screen.fd.mfmd";

    public MFMDBlock() {
        super(MachineEnum.MOLECULAR_DECOMPILER);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new MFMDBE(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if(!level.isClientSide()) {
            return (lvl, pos, bstate, tile) -> {
                if(tile instanceof MFMDBE) ((MFMDBE) tile).tickServer();
            };
        }else return null;
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result)
    {
        // Don't bother doing anything on the client side, the server will handle client-side interaction through openGui()
        if(level.isClientSide()) return InteractionResult.SUCCESS;

        // Ensure BE is of proper type (i.e. not corrupt)
        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof MFMDBE)) throw new IllegalStateException("Invalid Block Entity state for MFMD block!");

        // Create menu provider interface and call the server-side openGui utility method to automatically handle client-server interactions
        MenuProvider container = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return new TranslatableComponent(SCREEN_TITLE);
            }

            @Override
            public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
                return new MFMDContainer(id, pos, inv, player);
            }
        };
        NetworkHooks.openGui((ServerPlayer)player, container, be.getBlockPos());

        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean p_60519_)
    {
        // Drop all inventory items from the block before it is destroyed
        BlockEntity be = level.getBlockEntity(pos);
        if(be != null)
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c ->{
                for(int i = 0; i < c.getSlots(); i++) spawnItem(level, pos, c.getStackInSlot(i));
            });

        super.onRemove(state, level, pos, newState, p_60519_);
    }

    /**
     * Modified from the 1.14 version of the InventoryHelper class, now removed.
     */
    protected static void spawnItem(Level lvl, BlockPos pos, ItemStack stack)
    {
        Random rng = new Random();

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
