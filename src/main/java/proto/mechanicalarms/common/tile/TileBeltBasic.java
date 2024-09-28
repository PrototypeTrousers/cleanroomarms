package proto.mechanicalarms.common.tile;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.common.block.BlockBelt;


public class TileBeltBasic extends TileEntity implements ITickable {

    AxisAlignedBB renderBB;
    AxisAlignedBB pickerBB;
    int progress = 0;

    protected ItemStackHandler leftItemHandler = new BeltItemHandler(5);
    protected ItemStackHandler rightItemHandler = new BeltItemHandler(5);

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("leftInventory", leftItemHandler.serializeNBT());
        compound.setTag("rightInventory", rightItemHandler.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        leftItemHandler.deserializeNBT(compound.getCompoundTag("leftInventory"));
        rightItemHandler.deserializeNBT(compound.getCompoundTag("rightInventory"));
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        // getUpdateTag() is called whenever the chunkdata is sent to the
        // client. In contrast getUpdatePacket() is called when the tile entity
        // itself wants to sync to the client. In many cases you want to send
        // over the same information in getUpdateTag() as in getUpdatePacket().
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        // Prepare a packet for syncing our TE to the client. Since we only have to sync the stack
        // and that's all we have we just write our entire NBT here. If you have a complex
        // tile entity that doesn't need to have all information on the client you can write
        // a more optimal NBT here.
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        // Here we get the packet from the server and read it into our client side tile entity
        this.readFromNBT(packet.getNbtCompound());
    }

    public ItemStackHandler getleftItemHandler() {
        return leftItemHandler;
    }

    public ItemStackHandler getRightItemHandler() {
        return rightItemHandler;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (renderBB == null) {
            renderBB = super.getRenderBoundingBox();
        }
        return renderBB;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }


    @Override
    public void update() {
        for (EntityItem e : this.getWorld().getEntitiesWithinAABB(EntityItem.class, pickerBB)) {
            if (leftItemHandler.insertItem(0, e.getItem(), true) != e.getItem()) {
                ItemStack insert = e.getItem().copy();
                insert.setCount(1);
                leftItemHandler.insertItem(0, insert, false);
                e.getItem().shrink(1);
            }
        }
        if (leftItemHandler.getStackInSlot(0).isEmpty()) {
            progress = 0;
            return;
        }
        progress++;
        if (progress >= 20) {
            EnumFacing facing = world.getBlockState(pos).getValue(BlockBelt.FACING);
            TileEntity frontTe = world.getTileEntity(pos.offset(facing));
            if (frontTe != null) {
                IItemHandler cap = frontTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                if ( cap != null) {
                    if (cap.insertItem(0, leftItemHandler.extractItem(0,1, true), true) != leftItemHandler.getStackInSlot(0)){
                        cap.insertItem(0,leftItemHandler.extractItem(0,1,false),false);
                        progress = 0;
                    } else {
                        progress = 20;
                    }
                    return;
                }
            }
            progress = 0;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        pickerBB = new AxisAlignedBB(this.pos);
    }

    public double getProgress() {
        return progress;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) leftItemHandler;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    public class BeltItemHandler extends ItemStackHandler {
        public BeltItemHandler(int i) {
            super(i);
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileBeltBasic.this.markDirty();
        }
    }
}
