package michaelrunzler.fluiddynamics.machines.power_cell;

import michaelrunzler.fluiddynamics.machines.base.FDMachineBase;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PowerCellBlock extends FDMachineBase
{
    public static final String SCREEN_TITLE = "screen.fd.power_cell";

    public PowerCellBlock() {
        super(MachineEnum.POWER_CELL);
        super.doItemDrops = false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PowerCellBE(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type)
    {
        // Gets the ticker instance from the BE
        if(!level.isClientSide()) {
            return (lvl, pos, bstate, tile) -> {
                if(tile instanceof PowerCellBE) ((PowerCellBE) tile).tickServer();
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
        if(!(be instanceof PowerCellBE)) throw new IllegalStateException("Invalid Block Entity state for Power Cell block!");

        // Create menu provider interface and call the server-side openGui utility method to automatically handle client-server interactions
        MenuProvider container = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return new TranslatableComponent(SCREEN_TITLE);
            }

            @Override
            public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
                return new PowerCellContainer(id, pos, inv, player);
            }
        };

        NetworkHooks.openGui((ServerPlayer)player, container, be.getBlockPos());

        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player)
    {
        // Save block data (power/items) to item before destroying
        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof PowerCellBE pbe && !player.isCreative())
        {
            ItemStack is = new ItemStack(this, 1);
            // Only save block metadata if the block has any inventory or energy
            boolean allEmpty = pbe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(c -> {
                boolean empty = true;
                for(int i = 0; i < c.getSlots(); i++) if(!c.getStackInSlot(i).isEmpty()) empty = false;
                return empty;
            }).orElse(true);

            if(!allEmpty || pbe.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0) > 0)
                pbe.saveToItem(is);

            FDMachineBase.spawnItem(level, pos, is);
        }

        super.playerWillDestroy(level, pos, state, player);
    }
}
