package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.api.capability.IDualSidedHandler;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;


public class TileBeltBasic extends TileEntity implements ITickable, IGuiHolder {

    protected BeltItemHandler leftItemHandler = new BeltItemHandler(1, Side.L);
    protected BeltItemHandler leftSideItemHandler = new BeltItemHandler(1, leftItemHandler, Side.L);
    protected BeltItemHandler rightItemHandler = new BeltItemHandler(1, Side.R);
    protected BeltItemHandler rightSideItemHandler = new BeltItemHandler(1, rightItemHandler, Side.R);
    protected IDualSidedHandler dualBack = new b();
    protected IDualSidedHandler dualLeft = new b(Side.L);
    protected IDualSidedHandler dualRight = new b(Side.R);
    protected int connected = -1;
    protected long insertedTick;
    AxisAlignedBB renderBB;
    AxisAlignedBB pickerBB;
    int progressLeft = 0;
    int progressRight = 0;
    int previousProgressLeft = 0;
    int previousProgressRight = 0;
    EnumFacing front;
    Slope slope;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("leftInventory", leftItemHandler.serializeNBT());
        compound.setTag("rightInventory", rightItemHandler.serializeNBT());
        if (front == null) {
            front = EnumFacing.NORTH;
        }
        compound.setInteger("front", front.ordinal());
        if (slope == null) {
            slope = Slope.HORIZONTAL;
        }
        compound.setInteger("slope", slope.ordinal());
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
        front = EnumFacing.byIndex(compound.getInteger("front"));
        slope = Slope.values()[compound.getInteger("slope")];
        progressLeft = compound.getInteger("progressLeft");
        progressRight = compound.getInteger("progressRight");
        previousProgressLeft = compound.getInteger("previousProgressLeft");
        previousProgressRight = compound.getInteger("previousProgressRight");
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

    @Override
    public ModularPanel buildUI(GuiData guiData, GuiSyncManager guiSyncManager) {
        ModularPanel panel = ModularPanel.defaultPanel("tutorial_gui");
        panel.child(new ItemSlot().slot(leftItemHandler, 0));
        panel.child(new ItemSlot().slot(rightItemHandler, 0).left(18));
        panel.bindPlayerInventory();
        return panel;
    }

    public ItemStackHandler getLeftItemHandler() {
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
        if (connected == -1) {
            updateConnected();
        }
        if (progressLeft == 0 && progressRight == 0 && insertedTick == world.getTotalWorldTime()) {
            progressLeft = progressRight = 0;
            previousProgressLeft = previousProgressRight = -1;
            insertedTick = -1;
            return;
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

            if (progressLeft < 3 && !leftItemHandler.getStackInSlot(0).isEmpty()) {
                previousProgressLeft++;
                progressLeft++;
            }
            if (progressRight < 3 && !rightItemHandler.getStackInSlot(0).isEmpty()) {
                previousProgressRight++;
                progressRight++;
            }

            if (progressLeft >= 3) {
                handleItemTransfer(true);
            }
            if (progressRight >= 3) {
                handleItemTransfer(false);
            }
        } else {
            if (progressLeft < 3) {
                previousProgressLeft = progressLeft;
                progressLeft++;
            }
            if (progressRight < 3) {
                previousProgressRight = progressRight;
                progressRight++;
            }
        }
    }

    private void handleItemTransfer(boolean left) {
        TileEntity frontTe;
        EnumFacing facing = this.front;
        if (slope == Slope.HORIZONTAL) {
            frontTe = world.getTileEntity(pos.offset(facing));
            if (frontTe == null) {
                frontTe = world.getTileEntity(pos.offset(facing).down());
            }
        } else {
            if (slope == Slope.UP) {
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
        return slope != Slope.HORIZONTAL;
    }

    public EnumFacing getFront() {
        return front;
    }

    public void setFront(EnumFacing facing) {
        this.front = facing;
        markDirty();
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (connected == -1) {
            return null;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (this.front.rotateYCCW() == facing) {
                return (T) leftSideItemHandler;
            } else if (this.front.rotateY() == facing) {
                return (T) rightSideItemHandler;
            }
            return (T) leftItemHandler;
        } else if (capability == CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY) {
            if (facing.getOpposite() == front) {
                return (T) dualBack;
            }

            //left is 0
            if (this.front.rotateYCCW() == facing) {
                if ((connected & (1 << 0)) != 0 && (connected & ~(1 << 0)) == 0) {
                    return (T) dualLeft;
                }
            }
            //right is 1
            if (this.front.rotateY() == facing) {
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

    public Slope getSlope() {
        return slope;
    }

    public void setSlope(EnumFacing enumFacing) {
        if (enumFacing == EnumFacing.UP) {
            this.slope = Slope.UP;
        } else if (enumFacing == EnumFacing.DOWN) {
            this.slope = Slope.DOWN;
        } else {
            this.slope = Slope.HORIZONTAL;
        }

    }

    public void updateConnected() {
        int mask = 0; // Initialize the bitmask as an integer

        // Check the left connection and set bit 0 if true
        if (world.getTileEntity(this.pos.offset(front.rotateYCCW())) instanceof TileBeltBasic backBelt) {
            mask |= (1 << 0); // Set bit 0
        }

        // Check the right connection and set bit 1 if true
        if (world.getTileEntity(this.pos.offset(front.rotateY())) instanceof TileBeltBasic backBelt) {
            mask |= (1 << 1); // Set bit 1
        }

        // Check the opposite direction and set bit 3 if true
        if (world.getTileEntity(this.pos.offset(front.getOpposite())) instanceof TileBeltBasic backBelt) {
            mask |= (1 << 3); // Set bit 3
        }

        // Use the mask for further operations as needed, for example:
        connected = mask; // Store the bitmask in 'connected' (now an integer)
    }

    public class BeltItemHandler extends ItemStackHandler {

        BeltItemHandler main;
        Side side;

        public BeltItemHandler(int i, Side side) {
            super(i);
            this.side = side;
        }

        public BeltItemHandler(int i, BeltItemHandler mainHandler, Side side) {
            super(i);
            this.main = mainHandler;
            this.side = side;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (main != null) {
                main.setStackInSlot(0, stack);
                return;
            }
            super.setStackInSlot(slot, stack);
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (main != null) {
                return main.getStackInSlot(0);
            }
            return super.getStackInSlot(slot);
        }

        @Override
        public int getSlots() {
            if (main != null) {
                return 1;
            }
            return super.getSlots();
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (main != null) {
                ItemStack returnStack = main.insertItem(0, stack, simulate);
                if (!simulate && returnStack.isEmpty()) {
                    if (side == Side.L) {
                        progressLeft = 1;
                        previousProgressLeft = 1;
                    } else {
                        progressRight = 1;
                        previousProgressRight = 1;
                    }
                    insertedTick = world.getTotalWorldTime();
                }
                return returnStack;
            }

            ItemStack returnStack = super.insertItem(slot, stack, simulate);
            if (!simulate && returnStack.isEmpty()) {
                if (side == Side.L) {
                    previousProgressLeft = -1;
                    progressLeft = 0;
                } else {
                    previousProgressRight = -1;
                    progressRight = 0;
                }
                insertedTick = world.getTotalWorldTime();
            }
            return returnStack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    public class b implements IDualSidedHandler {
        Side side;

        public b(Side side) {
            this.side = side;
        }

        public b() {
        }

        @Override
        public ItemStack insertLeft(ItemStack insert, boolean simulate) {
            if (side == Side.L) {
                return leftSideItemHandler.insertItem(0, insert, simulate);
            }
            return leftItemHandler.insertItem(0, insert, simulate);
        }

        @Override
        public ItemStack insertRight(ItemStack insert, boolean simulate) {
            if (side == Side.R) {
                return rightSideItemHandler.insertItem(0, insert, simulate);
            }
            return rightItemHandler.insertItem(0, insert, simulate);
        }
    }

}
