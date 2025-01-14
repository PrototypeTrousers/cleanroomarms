package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.api.IDualInventory;
import proto.mechanicalarms.api.capability.IDualSidedHandler;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.cap.BeltItemHandler;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;
import proto.mechanicalarms.common.cap.DualSidedHandler;

public abstract class BeltTileEntity extends TileEntity implements ITickable, IGuiHolder, IDualInventory {
    protected BeltItemHandler leftItemHandler = new BeltItemHandler(this, 1, Side.L);
    protected BeltItemHandler leftSideItemHandler = new BeltItemHandler(this, 1, leftItemHandler, Side.L);
    protected BeltItemHandler rightItemHandler = new BeltItemHandler(this, 1, Side.R);
    protected BeltItemHandler rightSideItemHandler = new BeltItemHandler(this, 1, rightItemHandler, Side.R);
    protected IDualSidedHandler dualBack = new DualSidedHandler(this);
    protected IDualSidedHandler dualLeft = new DualSidedHandler(this, Side.L);
    protected IDualSidedHandler dualRight = new DualSidedHandler(this, Side.R);
    protected int connected = -1;
    protected long insertedTickLeft;
    protected long insertedTickRight;
    AxisAlignedBB renderBB;
    AxisAlignedBB pickerBB;
    int progressLeft = 0;
    int progressRight = 0;
    int previousProgressLeft = 0;
    int previousProgressRight = 0;
    Directions direction;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("leftInventory", leftItemHandler.serializeNBT());
        compound.setTag("rightInventory", rightItemHandler.serializeNBT());
        compound.setInteger("directions", direction.ordinal());
        compound.setInteger("progressLeft", progressLeft);
        compound.setInteger("progressRight", progressRight);
        compound.setInteger("previousProgressLeft", previousProgressLeft);
        compound.setInteger("previousProgressRight", previousProgressRight);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        leftItemHandler.deserializeNBT(compound.getCompoundTag("leftInventory"));
        rightItemHandler.deserializeNBT(compound.getCompoundTag("rightInventory"));
        direction = Directions.values()[compound.getInteger("directions")];
        progressLeft = compound.getInteger("progressLeft");
        progressRight = compound.getInteger("progressRight");
        previousProgressLeft = compound.getInteger("previousProgressLeft");
        previousProgressRight = compound.getInteger("previousProgressRight");
    }

    @Override
    public BlockPos getPosition() {
        return super.getPos();
    }

    @Override
    public World getTileWorld() {
        return super.getWorld();
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
        compound.setInteger("progressLeft", progressLeft);
        compound.setInteger("progressRight", progressRight);
        return new SPacketUpdateTileEntity(getPos(), 1, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        // Here we get the packet from the server and read it into our client side tile entity
        NBTTagCompound compound = packet.getNbtCompound();
        leftItemHandler.deserializeNBT(compound.getCompoundTag("leftInventory"));
        rightItemHandler.deserializeNBT(compound.getCompoundTag("rightInventory"));
        progressLeft = compound.getInteger("progressLeft");
        progressRight = compound.getInteger("progressRight");
    }

    public ItemStackHandler getLeftItemHandler() {
        return leftItemHandler;
    }

    public ItemStackHandler getRightItemHandler() {
        return rightItemHandler;
    }

    @Override
    public void setProgressLeft(int progressLeft) {
        this.progressLeft = progressLeft;
    }

    @Override
    public void setPreviousProgressLeft(int previousProgressLeft) {
        this.previousProgressLeft = previousProgressLeft;
    }

    @Override
    public void setProgressRight(int progressRight) {
        this.progressRight = progressRight;
    }

    @Override
    public void setPreviousProgressRight(int previousProgressRight) {
        this.previousProgressRight = previousProgressRight;
    }

    @Override
    public void updateLastTickLeft() {
        insertedTickLeft = world.getTotalWorldTime();
    }

    @Override
    public void updateLastTickRight() {
        insertedTickRight = world.getTotalWorldTime();
    }

    @Override
    public void markTileDirty() {
        if (this.world != null)
        {
            this.world.markChunkDirty(this.pos, this);
        }
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public void update() {
        boolean tickLeft = true;
        if (progressLeft == 0 && insertedTickLeft == world.getTotalWorldTime()) {
            progressLeft = 0;
            previousProgressLeft = -1;
            insertedTickLeft = -1;
            tickLeft = false;
        }

        boolean tickRight = true;
        if (progressRight == 0 && insertedTickRight == world.getTotalWorldTime()) {
            progressRight = 0;
            previousProgressRight = -1;
            insertedTickRight = -1;
            tickRight = false;
        }

        if (this.world.isBlockPowered(this.getPos())) {
            this.previousProgressLeft = progressLeft;
            this.previousProgressRight = progressRight;
            return;
        }
        if (leftItemHandler.getStackInSlot(0).isEmpty() && rightItemHandler.getStackInSlot(0).isEmpty()) {
            previousProgressLeft = previousProgressRight = 0;
            progressLeft = progressRight = 0;
            return;
        }
        if (!this.world.isRemote) {
            if (tickLeft) {
                if (progressLeft < 3 && !leftItemHandler.getStackInSlot(0).isEmpty()) {
                    previousProgressLeft++;
                    progressLeft++;
                }
                if (progressLeft >= 3) {
                    handleItemTransfer(true);
                }
            } if (tickRight) {
                if (progressRight < 3 && !rightItemHandler.getStackInSlot(0).isEmpty()) {
                    previousProgressRight++;
                    progressRight++;
                }
                if (progressRight >= 3) {
                    handleItemTransfer(false);
                }
            }
        } else {
            if (tickLeft) {
                if (progressLeft < 3) {
                    previousProgressLeft = progressLeft;
                    progressLeft++;
                }else {
                    previousProgressLeft = progressLeft;
                }
            }
            if (tickRight) {
                if(progressRight < 3) {
                    previousProgressRight = progressRight;
                    progressRight++;
                } else {
                    previousProgressRight = progressRight;
                }
            }
        }
    }

    protected void handleItemTransfer(boolean left) {
        TileEntity frontTe;
        EnumFacing facing = this.direction.getHorizontalFacing();
        if (direction.getRelativeHeight() == Directions.RelativeHeight.LEVEL) {
            frontTe = world.getTileEntity(pos.offset(facing));
            if (frontTe == null) {
                frontTe = world.getTileEntity(pos.offset(facing).down());
            }
        } else {
            if (direction.getRelativeHeight() == Directions.RelativeHeight.ABOVE) {
                frontTe = world.getTileEntity(pos.offset(facing).up());
            } else {
                frontTe = world.getTileEntity(pos.offset(facing).down());
                if (frontTe == null) {
                    frontTe = world.getTileEntity(pos.offset(facing));
                }
            }
        }

        if (frontTe != null) {
            if (frontTe.hasCapability(CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY, facing.getOpposite())) {
                IDualSidedHandler cap = frontTe.getCapability(CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY, facing.getOpposite());
                if (cap != null) {
                    if (left) {
                        if (cap.insertLeft(leftItemHandler.extractItem(0, 1, true), true) != leftItemHandler.getStackInSlot(0)) {
                            cap.insertLeft(leftItemHandler.extractItem(0, 1, false), false);
                            progressLeft = 0;
                        }
                    } else {
                        if (cap.insertRight(rightItemHandler.extractItem(0, 1, true), true) != rightItemHandler.getStackInSlot(0)) {
                            cap.insertRight(rightItemHandler.extractItem(0, 1, false), false);
                            progressRight = 0;
                        }
                    }
                } else {
                    IItemHandler icap = frontTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                    if (icap != null) {
                        if (left) {
                            if (icap.insertItem(0, leftItemHandler.extractItem(0, 1, true), true) != leftItemHandler.getStackInSlot(0)) {
                                icap.insertItem(0, leftItemHandler.extractItem(0, 1, false), false);
                                progressLeft = 0;
                            }
                        } else {
                            if (icap.insertItem(0, rightItemHandler.extractItem(0, 1, true), true) != rightItemHandler.getStackInSlot(0)) {
                                icap.insertItem(0, rightItemHandler.extractItem(0, 1, false), false);
                                progressRight = 0;
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onLoad() {
        super.onLoad();
        if (connected == -1) {
            updateConnected();
        }
        pickerBB = new AxisAlignedBB(this.pos);
    }

    public int getProgressLeft() {
        return progressLeft;
    }

    public int getPreviousProgressLeft() {
        return previousProgressLeft;
    }

    public int getProgressRight() {
        return progressRight;
    }

    public int getPreviousProgressRight() {
        return previousProgressRight;
    }

    public boolean isSlope() {
        return direction.getRelativeHeight() != Directions.RelativeHeight.LEVEL;
    }

    public EnumFacing getFront() {
        return direction.getHorizontalFacing();
    }

    public void setDirection(Directions direction) {
        this.direction = direction;
        markTileDirty();
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
}
