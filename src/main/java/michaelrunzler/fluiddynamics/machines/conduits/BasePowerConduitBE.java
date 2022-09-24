package michaelrunzler.fluiddynamics.machines.conduits;

import michaelrunzler.fluiddynamics.machines.base.PoweredMachineBE;
import michaelrunzler.fluiddynamics.types.MachineEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public abstract class BasePowerConduitBE extends PoweredMachineBE
{
    protected static final String PACKET_NBT_TAG = "EnergyPacket";
    protected static final String PACKET_DIR_TAG = "PacketDir";
    protected static final String PACKET_IMPORT_TAG = "PacketImported";
    protected static final String PACKET_AMT_TAG = "PacketAmount";
    protected ConduitPowerPacket storedPacket;
    protected Direction lastSideAccess;
    protected Random rng;

    public BasePowerConduitBE(BlockPos pos, BlockState state, MachineEnum type) {
        super(pos, state, type, true, true, false);
        rng = new Random();
        storedPacket = null;
        lastSideAccess = null;
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
            storedPacket = new ConduitPowerPacket(lastSideAccess, energyHandler.getEnergyStored(), true);
            energyHandler.extractEnergy(energyHandler.getEnergyStored(), false);
        }

        // Transfer the stored power packet (if any) to neighboring energy-capable blocks
        // Shuffle the direction array first
        exportToNeighbors(shuffle(Direction.values()));
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        super.load(tag);
        // Load packet data if it was saved
        if(tag.contains(PACKET_NBT_TAG)) {
            CompoundTag packetData = tag.getCompound(PACKET_NBT_TAG);
            storedPacket = new ConduitPowerPacket(Direction.byName(packetData.getString(PACKET_DIR_TAG)),
                    tag.getInt(PACKET_AMT_TAG), tag.getBoolean(PACKET_IMPORT_TAG));
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
            pTag.putBoolean(PACKET_IMPORT_TAG, storedPacket.imported());
            pTag.putInt(PACKET_AMT_TAG, storedPacket.amount());
        }
    }

    /**
     * This is overridden for a somewhat complicated reason, which I will attempt to explain here for future me to
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
     */
    @Override
    protected void exportToNeighbors(Direction... dirs)
    {
        if(level == null) return;

        // TODO not evenly distributing power, check TE maybe?
        for(Direction d : dirs)
        {
            // Don't bother checking more neighbors if there is no "energy" to distribute
            if(storedPacket == null) return;

            // Only attempt to "forward" energy to this side if the current packet wasn't generated from this side
            if(storedPacket.origin() == d) continue;

            // Grab the BE at the position and ensure it's a valid network component
            BlockEntity rbe = this.level.getBlockEntity(this.worldPosition.relative(d));
            if(rbe == null) continue;
            if(rbe instanceof PoweredMachineBE pbe && pbe.canImportPower)
            {
                // If the BE is a conduit, handle its transfer with the packetization system
                if(pbe instanceof BasePowerConduitBE cbe) {
                    if(cbe.tryAcceptPacket(storedPacket, d.getOpposite())) storedPacket = null; // Clear the stored packet if transfer was successful
                }else {
                    // Otherwise, export the current packet to the BE. If the BE can't take all the packet's energy,
                    // just export what it can take and update the packet
                    int xfered = pbe.getCapability(CapabilityEnergy.ENERGY).map(c -> c.receiveEnergy(storedPacket.amount(), false)).orElse(0);
                    if(xfered == storedPacket.amount()) storedPacket = null;
                    else storedPacket = new ConduitPowerPacket(storedPacket.origin(), storedPacket.amount() - xfered, storedPacket.imported());
                }
            }
        }
    }

    /**
     * Attempts to move a packet to this conduit from the specified direction.
     */
    protected boolean tryAcceptPacket(ConduitPowerPacket packet, Direction d)
    {
        if(this.storedPacket == null) {
            this.storedPacket = new ConduitPowerPacket(d, packet.amount(), false);
            return true;
        }

        return false;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // This "machine" doesn't have any inventory slots, so we don't care if items are valid or not
        return false;
    }

    /**
     * Randomizes the given Direction array.
     */
    protected Direction[] shuffle(Direction[] dirs)
    {
        // This is an implementation of the general Fisher-Yates shuffle algorithm
        int index;
        Direction tmp;
        for(int i = dirs.length - 1; i > 0; i--)
        {
            index = rng.nextInt(i + 1);
            tmp = dirs[index];
            dirs[index] = dirs[i];
            dirs[i] = tmp;
        }

        return dirs;
    }
}

/**
 * Represents a packet of "energy" which is used to abstract energy transfer inside the conduit network.
 */
record ConduitPowerPacket(Direction origin, int amount, boolean imported){
    @Override
    public String toString() {
        return "ConduitPacket: power " + amount + "; origin " + origin.getName() + "; imported: " + imported;
    }
}

