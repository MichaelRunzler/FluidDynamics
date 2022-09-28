package michaelrunzler.fluiddynamics.machines.conduits;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public abstract class BasePowerConduitBE extends PoweredMachineBE
{
    protected static final String PACKET_NBT_TAG = "EnergyPacket";
    protected static final String PACKET_DIR_TAG = "PacketDir";
    protected static final String PACKET_AMT_TAG = "PacketAmount";
    protected static final int MAX_CONSECUTIVE_INSERTIONS = 2;
    protected static final int FORWARD_MODE_THRESHOLD = 4;
    protected boolean forwardMode;
    protected ConduitPowerPacket storedPacket;
    protected Direction lastSideAccess; // The direction from which the conduit's power API was last accessed by another BE
    protected Direction lastPacketInsertion; // The direction from which a packet was last accepted
    protected int nextForwardDir; // The next direction ordinal that the conduit should attempt to forward held packets to
    protected int sequentialInsertionCount; // The number of times the conduit has received a packet from the same side sequentially
    protected int sequentialRetryCount; // The number of times another conduit has attempted to deliver a packet from the same side sequentially

    public BasePowerConduitBE(BlockPos pos, BlockState state, MachineEnum type) {
        super(pos, state, type, true, true, false);
        forwardMode = true;
        storedPacket = null;
        lastSideAccess = null;
        lastPacketInsertion = null;
        nextForwardDir = 0;
        sequentialInsertionCount = 0;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityEnergy.ENERGY) {
            lastSideAccess = side; // Cache which side the energy handler was last accessed from
            return (LazyOptional<T>)energyOpt;
        }

        return super.getCapability(cap, side);
    }

    public void tickServer()
    {
        // Transform any power pushed into this conduit by external machines into a power packet
        if(storedPacket == null && energyHandler.getEnergyStored() > 0) {
            storedPacket = new ConduitPowerPacket(lastSideAccess, energyHandler.getEnergyStored());
            energyHandler.extractEnergy(energyHandler.getEnergyStored(), false);
        }

        // Transfer the stored power packet (if any) to neighboring energy-capable blocks
        boolean successful;
        int looped = nextForwardDir;
        Direction[] values = Direction.values();
        do {
            // Starting with the next direction, iterate through the list of directions until we hit the original nextDir
            // or succeed with a power transfer (round-robin switching)
            successful = exportToNeighbor(values[nextForwardDir]);
            nextForwardDir++;
            if(nextForwardDir >= values.length) nextForwardDir = 0;
        }while(!successful && nextForwardDir != looped);
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        super.load(tag);
        // Load packet data if it was saved
        if(tag.contains(PACKET_NBT_TAG)) {
            CompoundTag packetData = tag.getCompound(PACKET_NBT_TAG);
            storedPacket = new ConduitPowerPacket(Direction.byName(packetData.getString(PACKET_DIR_TAG)),
                    tag.getInt(PACKET_AMT_TAG));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        super.saveAdditional(tag);
        // Store packet data if a packet is being held by this conduit
        if(storedPacket != null)
        {
            CompoundTag pTag = new CompoundTag();
            pTag.putString(PACKET_DIR_TAG, storedPacket.origin().getName());
            pTag.putInt(PACKET_AMT_TAG, storedPacket.amount());
        }
    }

    /**
     * This is written as it is for a somewhat complicated reason, which I will attempt to explain here for future me to
     * (hopefully) understand.
     * Normally, blocks transmit "packets" of energy to their neighbors by directly inserting power into their neighbors'
     * energy storages and subtracting it from their own. This works great for block-to-block transfer, since the only
     * block type that needs to worry about potential energy loops is the power cell (and similar types of storage),
     * and that has its own handling system for balancing energy. We can't use this for conduits since this would result
     * in a large amount of energy getting "bound up" in conduits, making transfer of small amounts of energy very frustrating.
     * To solve this issue, we use virtual "packets" of energy, which aren't actually stored in any internal energy reserve
     * until they reach a non-conduit device. Since these "packets" have extra directional data associated with them that
     * tells the conduit which direction they came from, they don't run into the same looping issue that actual energy
     * transfer has.
     * ===
     * To address some additional issues with this system (namely uneven distribution), we also add two further characteristics
     * to the packet network.
     * First, we use a "round-robin" output system to evenly distribute incoming packets to all outgoing
     * connections. For 1-in, 1-out (straight-through) connections, this is a minor efficiency penalty.
     * Second, we use a "routing/forwarding" scheme for incoming packets. This evaluates whether the node is a straight-through
     * or multi-input node, and acts accordingly. For straight-through, the conduit just blindly accepts all packets that
     * will fit into its cache and forwards them out as normal. For multi-input, the conduit "holds" inputs on an alternating
     * cycle, ensuring that all feeders can get an opportunity to deliver their packets.
     */
    protected boolean exportToNeighbor(Direction d)
    {
        // Ensure that the given direction is valid and has a connection; that we have a packet to give out; and that
        // said packet did not originate from the given direction.
        if(level == null || storedPacket == null || d == null || storedPacket.origin() == d ||
                !this.getBlockState().getValue(dirMap.get(d))) return false;

        // Grab the BE at the position and ensure it's a valid network component
        BlockEntity rbe = this.level.getBlockEntity(this.worldPosition.relative(d));
        if(rbe instanceof PoweredMachineBE pbe && pbe.canImportPower)
        {
            // If the BE is a conduit, handle its transfer with the packetization system
            if(pbe instanceof BasePowerConduitBE cbe) {
                if(cbe.tryAcceptPacket(storedPacket, d.getOpposite())) {
                    storedPacket = null; // Clear the stored packet if transfer was successful
                    return true;
                }
            }else
            {
                // Otherwise, export the current packet to the BE. If the BE can't take all the packet's energy,
                // just export what it can take and update the packet
                int xfered = pbe.getCapability(CapabilityEnergy.ENERGY).map(c -> c.receiveEnergy(storedPacket.amount(), false)).orElse(0);

                if(xfered == storedPacket.amount()) storedPacket = null;
                else if(xfered == 0) return false;
                else storedPacket = new ConduitPowerPacket(storedPacket.origin(), storedPacket.amount() - xfered);

                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to move a packet to this conduit from the specified direction.
     * Obeys the routing paradigm set forth in the documentation for {@link #exportToNeighbor(Direction)}.
     */
    protected boolean tryAcceptPacket(ConduitPowerPacket packet, Direction d)
    {
        // Change to routing mode if a packet insertion is attempted by another direction
        // Also manage retry counters for changing back to forwarding mode
        if(forwardMode && (lastPacketInsertion != null && lastPacketInsertion != d)) {
            forwardMode = false;
        } else if(!forwardMode && lastPacketInsertion == d) sequentialRetryCount++;
        else sequentialRetryCount = 0;

        // We already have a packet; insertion is not allowed
        if(this.storedPacket != null) return false;

        // Update mode. If we have seen quite a few consecutive insertions, switch back to forwarding mode.
        // If we are in routing mode and have seen more than the allowed number of
        // consecutive insertions, deny any further insertions from that direction.
        if(!forwardMode && sequentialRetryCount >= FORWARD_MODE_THRESHOLD) forwardMode = true;
        else if(!forwardMode && sequentialInsertionCount >= MAX_CONSECUTIVE_INSERTIONS) return false;

        // Register sequential insertions in routing mode
        if(!forwardMode && (lastPacketInsertion == d || lastPacketInsertion == null)) sequentialInsertionCount++;
        else sequentialInsertionCount = 0;
        lastPacketInsertion = d;

        // This is a validated insertion, so we accept the packet
        this.storedPacket = new ConduitPowerPacket(d, packet.amount());
        return true;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // This "machine" doesn't have any inventory slots, so we don't care if items are valid or not
        return false;
    }

    protected static final HashMap<Direction, BooleanProperty> dirMap = generateDirMap();

    private static HashMap<Direction, BooleanProperty> generateDirMap()
    {
        HashMap<Direction, BooleanProperty> map = new HashMap<>();
        map.put(Direction.UP, BlockStateProperties.UP);
        map.put(Direction.DOWN, BlockStateProperties.DOWN);
        map.put(Direction.NORTH, BlockStateProperties.NORTH);
        map.put(Direction.SOUTH, BlockStateProperties.SOUTH);
        map.put(Direction.EAST, BlockStateProperties.EAST);
        map.put(Direction.WEST, BlockStateProperties.WEST);
        return map;
    }
}

/**
 * Represents a packet of "energy" which is used to abstract energy transfer inside the conduit network.
 */
record ConduitPowerPacket(Direction origin, int amount){
    @Override
    public String toString() {
        return "ConduitPacket: power " + amount + "; origin " + origin.getName();
    }
}

