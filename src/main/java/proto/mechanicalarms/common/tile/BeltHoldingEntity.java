package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.api.capability.IDualSidedHandler;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.cap.BeltItemHandler;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;
import proto.mechanicalarms.common.cap.DualSidedHandler;

public abstract class BeltHoldingEntity extends TileEntity implements IGuiHolder {
    protected BeltUpdatingLogic logic;
    protected BeltItemHandler leftItemHandler;
    protected BeltItemHandler leftSideItemHandler;
    protected BeltItemHandler rightItemHandler;
    protected BeltItemHandler rightSideItemHandler;
    protected IDualSidedHandler dualBack;
    protected IDualSidedHandler dualLeft;
    protected IDualSidedHandler dualRight;
    protected int connected = -1;
    AxisAlignedBB renderBB;
    AxisAlignedBB pickerBB;
    Directions direction;

    BeltHoldingEntity() {
        logic = new BeltUpdatingLogic(this, this);
        leftItemHandler = new BeltItemHandler(logic, 1, Side.L);
        leftSideItemHandler = new BeltItemHandler(logic, 1, leftItemHandler, Side.L);
        rightItemHandler = new BeltItemHandler(logic, 1, Side.R);
        rightSideItemHandler = new BeltItemHandler(logic, 1, rightItemHandler, Side.R);
        dualBack = new DualSidedHandler(logic);
        dualLeft = new DualSidedHandler(logic, Side.L);
        dualRight = new DualSidedHandler(logic, Side.R);
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("leftInventory", leftItemHandler.serializeNBT());
        compound.setTag("rightInventory", rightItemHandler.serializeNBT());
        compound.setInteger("directions", direction.ordinal());
        logic.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        leftItemHandler.deserializeNBT(compound.getCompoundTag("leftInventory"));
        rightItemHandler.deserializeNBT(compound.getCompoundTag("rightInventory"));
        direction = Directions.values()[compound.getInteger("directions")];
        logic.readFromNBT(compound);
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
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("leftInventory", leftItemHandler.serializeNBT());
        compound.setTag("rightInventory", rightItemHandler.serializeNBT());
        compound.setInteger("directions", direction.ordinal());
        logic.updatePacket(compound);
        return new SPacketUpdateTileEntity(getPos(), 1, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        // Here we get the packet from the server and read it into our client side tile entity
        NBTTagCompound compound = packet.getNbtCompound();
        leftItemHandler.deserializeNBT(compound.getCompoundTag("leftInventory"));
        rightItemHandler.deserializeNBT(compound.getCompoundTag("rightInventory"));
        logic.onDataPacket(net, packet);
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (connected == -1) {
            updateConnected();
        }
        pickerBB = new AxisAlignedBB(this.pos);
    }

    public boolean isSlope() {
        return direction.getRelativeHeight() != Directions.RelativeHeight.LEVEL;
    }

    public EnumFacing getFront() {
        return direction.getHorizontalFacing();
    }

    public void setDirection(Directions direction) {
        this.direction = direction;
        markDirty();
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (this.direction.getHorizontalFacing().rotateYCCW() == facing) {
                return (T) leftSideItemHandler;
            } else if (this.direction.getHorizontalFacing().rotateY() == facing) {
                return (T) rightSideItemHandler;
            }
            return (T) leftItemHandler;
        } else if (capability == CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY) {
            if (facing.getOpposite() == direction.getHorizontalFacing()) {
                return (T) dualBack;
            }

            //left is 0
            if (this.direction.getHorizontalFacing().rotateYCCW() == facing) {
                if ((connected & (1 << 0)) != 0 && (connected & ~(1 << 0)) == 0) {
                    return (T) dualLeft;
                }
            }
            //right is 1
            if (this.direction.getHorizontalFacing().rotateY() == facing) {
                if ((connected & (1 << 1)) != 0 && (connected & ~(1 << 1)) == 0) {
                    return (T) dualRight;
                }
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY;
    }

    public Directions getDirection() {
        return direction;
    }

    public void updateConnected() {
        int mask = 0; // Initialize the bitmask as an integer

        // Check the left connection and set bit 0 if true
        if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().rotateYCCW())) instanceof TileBeltBasic backBelt) {
            if (backBelt.getFront() == this.getFront().rotateY()) {
                mask |= (1 << 0); // Set bit 0
            }
        }
        // Check the right connection and set bit 1 if true
        if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().rotateY())) instanceof TileBeltBasic backBelt) {
            if (backBelt.getFront() == this.getFront().rotateYCCW()) {
                mask |= (1 << 1); // Set bit 1
            }
        }
        // Check the opposite direction and set bit 3 if true
        if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().getOpposite())) instanceof TileBeltBasic backBelt) {
            if (backBelt.getFront() == this.getFront()) {
                mask |= (1 << 3); // Set bit 3
            }
        }
        // Check the opposite below direction and set bit 3 if true
        if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().getOpposite()).down()) instanceof TileBeltBasic backBelt) {
            if (backBelt.getFront() == this.getFront()) {
                mask |= (1 << 3); // Set bit 3
            }
        }

        // Use the mask for further operations as needed, for example:
        connected = mask; // Store the bitmask in 'connected' (now an integer)
    }

    public BeltUpdatingLogic getLogic() {
        return logic;
    }
}
