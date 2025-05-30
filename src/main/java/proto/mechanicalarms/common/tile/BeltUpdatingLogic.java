package proto.mechanicalarms.common.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import proto.mechanicalarms.api.IBeltLogic;
import proto.mechanicalarms.api.capability.IDualSidedHandler;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.cap.BeltItemHandler;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;

public class BeltUpdatingLogic implements IBeltLogic {
    protected final BeltHoldingEntity beltHoldingEntity;
    protected BeltHoldingEntity holder;
    protected long insertedTickLeft;
    protected long insertedTickRight;
    int progressLeft = 0;
    int progressRight = 0;
    int previousProgressLeft = 0;
    int previousProgressRight = 0;

    BeltUpdatingLogic(BeltHoldingEntity beltHoldingEntity, BeltHoldingEntity holder) {
        this.beltHoldingEntity = beltHoldingEntity;
        this.holder = holder;
    }

    //TODO remove this shit.
    //Only here due to the fact that the client side tile entity can reliabily check if the inventory change
    //When it receives the inventory change from the server side tile entity packet

    void updatePacket(NBTTagCompound compound) {
        if (progressLeft == 0) {
            compound.setInteger("progressLeft", progressLeft);
        }
        if (progressRight == 0) {
            compound.setInteger("progressRight", progressRight);
        }
    }

    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        // Here we get the packet from the server and read it into our client side tile entity
        NBTTagCompound compound = packet.getNbtCompound();
        if (compound.hasKey("progressLeft")) {
            progressLeft = compound.getInteger("progressLeft");
            previousProgressLeft = progressLeft;
        }
        if (compound.hasKey("progressRight")) {
            progressRight = compound.getInteger("progressRight");
            previousProgressRight = progressRight;
        }
    }

    public void update() {
        boolean tickLeft = true;
        if (progressLeft == 0 && insertedTickLeft == beltHoldingEntity.getWorld().getTotalWorldTime()) {
            progressLeft = 0;
            previousProgressLeft = -1;
            insertedTickLeft = -1;
            tickLeft = false;
        }

        if (beltHoldingEntity.leftItemHandler.getStackInSlot(0).isEmpty()) {
            progressLeft = 0;
            previousProgressLeft = 0;
            tickLeft = false;
        }

        boolean tickRight = true;
        if (progressRight == 0 && insertedTickRight == beltHoldingEntity.getWorld().getTotalWorldTime()) {
            progressRight = 0;
            previousProgressRight = -1;
            insertedTickRight = -1;
            tickRight = false;
        }

        if (beltHoldingEntity.rightItemHandler.getStackInSlot(0).isEmpty()) {
            progressRight = 0;
            previousProgressRight = 0;
            tickRight = false;
        }

        if (this.holder.isDisabledByRedstone()) {
            this.previousProgressLeft = progressLeft;
            this.previousProgressRight = progressRight;
            return;
        }
        if (!tickRight && !tickLeft) {
            previousProgressLeft = progressLeft;
            previousProgressRight = progressRight;
            return;
        }
        if (!this.holder.getWorld().isRemote) {
            if (tickLeft) {
                if (progressLeft < 7) {
                    progressLeft++;
                }
                if (progressLeft >= 7) {
                    handleItemTransfer(true);
                }
            }
            if (tickRight) {
                if (progressRight < 7) {
                    progressRight++;
                }
                if (progressRight >= 7) {
                    handleItemTransfer(false);
                }
            }
        } else {
            if (tickLeft) {
                if (progressLeft < 7) {
                    previousProgressLeft = progressLeft++;
                } else {
                    previousProgressLeft = progressLeft = 7;
                }
            }
            if (tickRight) {
                if (progressRight < 7) {
                    previousProgressRight = progressRight++;
                } else {
                    previousProgressRight = progressRight = 7;
                }
            }
        }
    }

    protected void handleItemTransfer(boolean left) {
        TileEntity frontTe;
        EnumFacing facing = holder.direction.getHorizontalFacing();
        if (beltHoldingEntity.direction.getRelativeHeight() == Directions.RelativeHeight.LEVEL) {
            frontTe = beltHoldingEntity.getWorld().getTileEntity(beltHoldingEntity.getPos().offset(facing));
            if (frontTe == null) {
                frontTe = beltHoldingEntity.getWorld().getTileEntity(beltHoldingEntity.getPos().offset(facing).down());
            }
        } else {
            if (beltHoldingEntity.direction.getRelativeHeight() == Directions.RelativeHeight.ABOVE) {
                frontTe = beltHoldingEntity.getWorld().getTileEntity(beltHoldingEntity.getPos().offset(facing).up());
            } else {
                frontTe = beltHoldingEntity.getWorld().getTileEntity(beltHoldingEntity.getPos().offset(facing).down());
                if (frontTe == null) {
                    frontTe = beltHoldingEntity.getWorld().getTileEntity(beltHoldingEntity.getPos().offset(facing));
                }
            }
        }

        attemptTransfer(frontTe, facing, left);
    }

    boolean attemptTransfer(TileEntity frontTe, EnumFacing facing, boolean left) {
        if (frontTe != null) {
            if (frontTe.hasCapability(CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY, facing.getOpposite())) {
                IDualSidedHandler cap = frontTe.getCapability(CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY, facing.getOpposite());
                if (cap != null) {
                    if (left) {
                        if (cap.insertLeft(beltHoldingEntity.leftItemHandler.extractItem(0, 1, true), true) != beltHoldingEntity.leftItemHandler.getStackInSlot(0)) {
                            cap.insertLeft(beltHoldingEntity.leftItemHandler.extractItem(0, 1, false), false);
                            return true;
                        }
                    } else {
                        if (cap.insertRight(beltHoldingEntity.rightItemHandler.extractItem(0, 1, true), true) != beltHoldingEntity.rightItemHandler.getStackInSlot(0)) {
                            cap.insertRight(beltHoldingEntity.rightItemHandler.extractItem(0, 1, false), false);
                            return true;
                        }
                    }
                } else {
                    IItemHandler icap = frontTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                    if (icap != null) {
                        if (left) {
                            if (icap.insertItem(0, beltHoldingEntity.leftItemHandler.extractItem(0, 1, true), true) != beltHoldingEntity.leftItemHandler.getStackInSlot(0)) {
                                icap.insertItem(0, beltHoldingEntity.leftItemHandler.extractItem(0, 1, false), false);
                                return true;
                            }
                        } else {
                            if (icap.insertItem(0, beltHoldingEntity.rightItemHandler.extractItem(0, 1, true), true) != beltHoldingEntity.rightItemHandler.getStackInSlot(0)) {
                                icap.insertItem(0, beltHoldingEntity.rightItemHandler.extractItem(0, 1, false), false);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("progressLeft", progressLeft);
        compound.setInteger("progressRight", progressRight);
    }

    public void readFromNBT(NBTTagCompound compound) {
        progressLeft = compound.getInteger("progressLeft");
        previousProgressLeft = progressLeft;
        progressRight = compound.getInteger("progressRight");
        previousProgressRight = progressRight;
    }

    public ItemStackHandler getLeftItemHandler() {
        return beltHoldingEntity.leftItemHandler;
    }

    public BeltItemHandler getLeftSideItemHandler() {
        return beltHoldingEntity.leftSideItemHandler;
    }

    public ItemStackHandler getRightItemHandler() {
        return beltHoldingEntity.rightItemHandler;
    }

    public BeltItemHandler getRightSideItemHandler() {
        return beltHoldingEntity.rightSideItemHandler;
    }

    @Override
    public void setProgressLeft(int progressLeft) {
        this.progressLeft = progressLeft;
    }

    @Override
    public void setProgressRight(int progressRight) {
        this.progressRight = progressRight;
    }

    @Override
    public void updateLastTickLeft() {
        insertedTickLeft = beltHoldingEntity.getWorld().getTotalWorldTime();
    }

    @Override
    public void updateLastTickRight() {
        insertedTickRight = beltHoldingEntity.getWorld().getTotalWorldTime();
    }

    @Override
    public void markTileDirty() {
        this.holder.getWorld().getChunk(holder.getPos()).markDirty();
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

    @Override
    public BlockPos getHolderPosition() {
        return holder.getPos();
    }

    @Override
    public World getHolderWorld() {
        return holder.getWorld();
    }
}
