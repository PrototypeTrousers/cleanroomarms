package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
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
import proto.mechanicalarms.common.block.properties.Directions;


public class TileSplitter extends TileEntity implements ITickable, IGuiHolder {

    protected ItemStackHandler leftItemHandler = new SplitterItemHandler(1);
    protected ItemStackHandler rightItemHandler = new SplitterItemHandler(1);
    protected long insertedTick;
    Side lastOutputSide = Side.L;
    AxisAlignedBB renderBB;
    AxisAlignedBB pickerBB;
    int progressLeft = 0;
    int progressRight = 0;
    int previousProgressLeft = 0;
    int previousProgressRight = 0;
    EnumFacing front;
    Directions directions;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("leftInventory", leftItemHandler.serializeNBT());
        compound.setTag("rightInventory", rightItemHandler.serializeNBT());
        compound.setInteger("front", front.ordinal());
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
        progressLeft = compound.getInteger("progressLeft");
        progressRight = compound.getInteger("progressRight");
        previousProgressLeft = compound.getInteger("previousProgressLeft");
        previousProgressRight = compound.getInteger("previousProgressRight");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public ModularPanel buildUI(GuiData guiData, GuiSyncManager guiSyncManager) {
        ModularPanel panel = new ModularPanel("Splitter");
        panel.padding(2);
        panel.align(Alignment.Center);
        panel.child(new ItemSlot().slot(leftItemHandler, 0));
        panel.child(new ItemSlot().slot(rightItemHandler, 0).left(20));

        panel.bindPlayerInventory();
        return panel;
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
        // Reset progress if necessary
        resetProgressIfNeeded();

        // If block is powered, skip further updates
        if (this.world.isBlockPowered(this.getPos())) {
            updatePreviousProgress();
            return;
        }

        // If no items in either handler, reset progress
        boolean emptyLeft = leftItemHandler.getStackInSlot(0).isEmpty();
        boolean emptyRight = rightItemHandler.getStackInSlot(0).isEmpty();
        if (emptyLeft && emptyRight) {
            resetProgress();
            return;
        }

        // Handle item progress updates and item transfer logic for both sides
        if (!this.world.isRemote) {
            if (!emptyLeft)
                updateProgressForHandler(leftItemHandler);
            if (!emptyRight)
                updateProgressForHandler(rightItemHandler);

            if (progressLeft >= 3) {
                transferItemsToFront(leftItemHandler);
                updatePreviousProgress();
            }
            if (progressRight >= 3) {
                transferItemsToFront(rightItemHandler);
                updatePreviousProgress();
            }
        } else {
            updateProgressForHandler(leftItemHandler);
            updateProgressForHandler(rightItemHandler);
        }
    }

    private void resetProgressIfNeeded() {
        if (progressLeft == 0 && insertedTick == world.getWorldTime()) {
            progressLeft = 0;
            previousProgressLeft = -1;
            insertedTick = -1;
        }

        if (progressRight == 0 && insertedTick == world.getWorldTime()) {
            progressRight = 0;
            previousProgressRight = -1;
            insertedTick = -1;
        }
    }

    private void resetProgress() {
        progressLeft = 0;
        progressRight = 0;
        previousProgressLeft = 0;
        previousProgressRight = 0;
    }

    private void updatePreviousProgress() {
        previousProgressLeft = progressLeft;
        previousProgressRight = progressRight;
    }

    private void updateProgressForHandler(ItemStackHandler handler) {
        if (handler == leftItemHandler) {
            if (progressLeft < 3) {
                previousProgressLeft++;
                progressLeft++;
            }
        } else {
            if (progressRight < 3) {
                previousProgressRight++;
                progressRight++;
            }
        }
    }

    private void transferItemsToFront(ItemStackHandler handler) {
        TileEntity frontTe;

        if (lastOutputSide == Side.R) {
            frontTe = world.getTileEntity(pos.offset(front.rotateY()).offset(front));
            lastOutputSide = Side.L;
        } else {
            frontTe = world.getTileEntity(pos.offset(front));
            lastOutputSide = Side.R;
        }


        if (frontTe != null) {
            IItemHandler cap = frontTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, front.getOpposite());
            if (cap != null) {
                transferItemToFrontHandler(handler, cap);
            }
        }
    }

    private void transferItemToFrontHandler(ItemStackHandler handler, IItemHandler cap) {
        if (cap.insertItem(0, handler.extractItem(0, 1, true), true) != handler.getStackInSlot(0)) {
            cap.insertItem(0, handler.extractItem(0, 1, false), false);
            if (handler == leftItemHandler) {
                progressLeft = 0;
            } else {
                progressRight = 0;
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        pickerBB = new AxisAlignedBB(this.pos);
    }

    public EnumFacing getFront() {
        return front;
    }

    public void setFront(EnumFacing facing) {
        this.front = facing;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) leftItemHandler; // For now, returning left item handler, modify as needed
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    enum Side {
        L, R
    }

    public class SplitterItemHandler extends ItemStackHandler {

        ItemStackHandler main;

        public SplitterItemHandler(int i) {
            super(i);
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
                    setStackInSlot(slot, ItemStack.EMPTY);
                }
                return returnStack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    }
}

