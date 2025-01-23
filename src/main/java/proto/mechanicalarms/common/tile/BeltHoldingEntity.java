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
        this.initLogic();
        this.initCaps();
    }

    protected void initLogic() {
        logic = new BeltUpdatingLogic(this, this);
    }

    protected void initCaps() {
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
        NBTTagCompound compound = super.getUpdateTag();
        return writeToNBT(compound);
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

            int maskedConnected = connected & ~(1 << 1);

            //left is 0
            if (this.direction.getHorizontalFacing().rotateYCCW() == facing) {
                if ((connected & (1 << 0)) != 0 && (maskedConnected & ~(1 << 0)) == 0) {
                    return (T) dualLeft;
                }
            }
            //right is 2
            if (this.direction.getHorizontalFacing().rotateY() == facing) {
                if ((connected & (1 << 2)) != 0 && (maskedConnected & ~(1 << 2)) == 0) {
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

        //bit 0 = left
        //bit 1 = front
        //bit 2 = right
        //bit 3 = opposite
        //bit 4 = opposite below
        //bit 5 = opposite above
        //bit 6 = front below
        //bit 7 = front above


        int mask = 0; // Initialize the bitmask as an integer
        if (this.direction.getRelativeHeight() == Directions.RelativeHeight.LEVEL) {
            // Check the left connection and set bit 0 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().rotateYCCW())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront().rotateY()) {
                    mask |= (1 << 0); // Set bit 0
                }
            }

            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().rotateYCCW()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront().rotateY()) {
                    mask |= (1 << 0); // Set bit 0
                }
            }

            // Check the front connection and set bit 1 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() != this.getFront().getOpposite()) {
                    mask |= (1 << 1); // Set bit 1
                }
            }

            // Check the right connection and set bit 2 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().rotateY())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront().rotateYCCW()) {
                    mask |= (1 << 2); // Set bit 2
                }
            }

            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().rotateY()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront().rotateYCCW()) {
                    mask |= (1 << 2); // Set bit 2
                }
            }

            // Check the opposite direction and set bit 3 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().getOpposite())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront()) {
                    mask |= (1 << 3); // Set bit 3
                }
            }

            // Check the opposite below direction and set bit 4 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().getOpposite()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront()) {
                    mask |= (1 << 4); // Set bit 4
                }
            }
        } else if (this.direction.getRelativeHeight() == Directions.RelativeHeight.ABOVE) {
            // Check the front above connection and set bit 7 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing()).up()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() != this.getFront().getOpposite()) {
                    mask |= (1 << 7); // Set bit 7
                }
            }

            // Check the opposite direction and set bit 3 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().getOpposite())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront()) {
                    mask |= (1 << 3); // Set bit 3
                }
            }

            // Check the opposite below direction and set bit 4 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().getOpposite()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront()) {
                    mask |= (1 << 4); // Set bit 4
                }
            }
        } else if (this.direction.getRelativeHeight() == Directions.RelativeHeight.BELOW) {
            // Check the front connection and set bit 1 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() != this.getFront().getOpposite()) {
                    mask |= (1 << 1); // Set bit 1
                }
            }

            // Check the opposite above direction and set bit 3 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing().getOpposite()).up()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == this.getFront()) {
                    mask |= (1 << 5); // Set bit 5
                }
            }

            // Check the front below connection and set bit 6 if true
            if (world.getTileEntity(this.pos.offset(direction.getHorizontalFacing()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() != this.getFront().getOpposite()) {
                    mask |= (1 << 6); // Set bit 5
                }
            }
        }

        // Use the mask for further operations as needed, for example:
        connected = mask; // Store the bitmask in 'connected' (now an integer)
    }

    // Helper method to check if a specific bit is set
    private boolean isBitSet(int bitIndex) {
        return (connected & (1 << bitIndex)) != 0;
    }

    // Helper method to check left connection (bit 0)
    public boolean isLeftConnected() {
        return isBitSet(0);
    }

    // Helper method to check front connection (bit 1)
    public boolean isFrontConnected() {
        return isBitSet(1);
    }

    // Helper method to check right connection (bit 2)
    public boolean isRightConnected() {
        return isBitSet(2);
    }

    // Check if only connected to the front (bit 1)
    public boolean isOnlyFrontConnected() {
        int frontMask = (1 << 1); // Bit for front connection
        return (connected & frontMask) != 0 && (connected & ~frontMask) == 0;
    }

    // Check if only connected to the left (bit 0)
    public boolean isOnlyLeftConnected() {
        int leftMask = (1 << 0); // Bit for left connection
        int ignoreBit1Mask = ~(1 << 1);           // Create a mask to ignore bit 1
        return (connected & leftMask) != 0 && (connected & ~leftMask & ignoreBit1Mask) == 0;
    }

    // Check if only connected to the right (bit 2)
    public boolean isOnlyRightConnected() {
        int rightMask =  (1 << 2); // Bit for right connection
        int ignoreBit1Mask = ~(1 << 1);           // Create a mask to ignore bit 1
        return (connected & rightMask) != 0 && (connected & ~rightMask & ignoreBit1Mask) == 0;
    }

    public boolean isBackConnected() {
        return isBitSet(3) || isBitSet(4) || isBitSet(5);
    }

    public boolean isOnlyBackConnected() {
        int mask = (1 << 3) | (1 << 4) | (1 << 5); // Create a bitmask for bits 3, 4, and 5
        int ignoreBit1Mask = ~(1 << 1);           // Create a mask to ignore bit 1
        return (connected & mask) != 0 && (connected & ~mask & ignoreBit1Mask) == 0;
    }

    public BeltUpdatingLogic getLogic() {
        return logic;
    }

    public int getConnected() {
        return connected;
    }

    public boolean isOnlyConnectedToSide(EnumFacing facing) {
        EnumFacing front = getFront();
        if (facing == front) {
            return isOnlyBackConnected();
        } else if (facing == front.rotateY()) {
            return isOnlyLeftConnected();
        } else if (facing == front.rotateYCCW()) {
            return isOnlyRightConnected();
        } else {
            return false;
        }
    }
}
