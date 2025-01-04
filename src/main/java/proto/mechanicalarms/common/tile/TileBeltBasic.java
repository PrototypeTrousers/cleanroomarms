package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TileBeltBasic extends TileEntity implements ITickable, IGuiHolder {

    AxisAlignedBB renderBB;
    AxisAlignedBB pickerBB;
    int progress = 0;
    int previousProgress = 0;

    EnumFacing front;
    Slope slope;

    protected ItemStackHandler mainItemHandler = new BeltItemHandler(5);
    protected ItemStackHandler sideItemHandler = new BeltItemHandler(1, mainItemHandler);
    protected long insertedTick;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("mainInventory", mainItemHandler.serializeNBT());
        if (front == null) {
            front = EnumFacing.NORTH;
        }
        compound.setInteger("front", front.ordinal());
        if (slope == null) {
            slope = Slope.HORIZONTAL;
        }
        compound.setInteger("slope", slope.ordinal());
        compound.setInteger("progress", progress);
        compound.setInteger("previousProgress", previousProgress);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        mainItemHandler.deserializeNBT(compound.getCompoundTag("mainInventory"));
        front = EnumFacing.byIndex(compound.getInteger("front"));
        slope = Slope.values()[compound.getInteger("slope")];
        progress = compound.getInteger("progress");
        previousProgress = compound.getInteger("previousProgress");
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
        panel.child(new ProgressWidget()
                .size(20)
                .leftRel(0.5f).topRelAnchor(0.25f, 0.5f)
                .texture(GuiTextures.PROGRESS_ARROW, 20)
                .value(new DoubleSyncValue(() -> this.progress / 100.0, val -> this.progress = (int) (val * 100))));
        panel.child(new ItemSlot().slot(mainItemHandler, 0));
        panel.bindPlayerInventory();
        return panel;
    }

    public ItemStackHandler getMainItemHandler() {
        return mainItemHandler;
    }

    public ItemStackHandler getSideItemHandler() {
        return sideItemHandler;
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
        if (progress == 0 && insertedTick == world.getWorldTime()) {
            progress = 0;
            previousProgress = -1;
            insertedTick = -1;
            return;
        }

        for (EntityItem e : this.getWorld().getEntitiesWithinAABB(EntityItem.class, pickerBB)) {
            if (mainItemHandler.insertItem(0, e.getItem(), true) != e.getItem()) {
                ItemStack insert = e.getItem().copy();
                insert.setCount(1);
                mainItemHandler.insertItem(0, insert, false);
                e.getItem().shrink(1);
            }
        }
        if (this.world.isBlockPowered(this.getPos())) {
            this.previousProgress = progress;
            return;
        }
        if (mainItemHandler.getStackInSlot(0).isEmpty()) {
            previousProgress = 0;
            progress = 0;
            return;
        }
        if (!this.world.isRemote) {
            if (progress < 19) {
                previousProgress++;
                progress++;
            }
            if (progress == 19) {
                EnumFacing facing = front;
                TileEntity frontTe;
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
                    IItemHandler cap = frontTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                    if (cap != null) {
                        if (cap.insertItem(0, mainItemHandler.extractItem(0, 1, true), true) != mainItemHandler.getStackInSlot(0)) {
                            cap.insertItem(0, mainItemHandler.extractItem(0, 1, false), false);
                            progress = 0;
                        }
                    }
                }
                previousProgress = progress;
            }
        } else {
            if (progress < 19) {
                previousProgress = progress;
                progress++;
            } else {
                previousProgress = progress;
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        pickerBB = new AxisAlignedBB(this.pos);
    }

    public int getProgress() {
        return progress;
    }
    public int getPreviousProgress() {
        return previousProgress;
    }

    public boolean isSlope(){
        return slope != Slope.HORIZONTAL;
    }

    public void setSlope(EnumFacing enumFacing) {
        if (enumFacing == EnumFacing.UP) {
            this.slope = Slope.UP;
        } else if (enumFacing == EnumFacing.DOWN ) {
            this.slope = Slope.DOWN;
        } else {
            this.slope = Slope.HORIZONTAL;
        }

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
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (this.front.rotateYCCW() == facing || this.front.rotateY() == facing) {
                return (T) sideItemHandler;
            }
            return (T) mainItemHandler;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    public Slope getSlope() {
        return slope;
    }

    public class BeltItemHandler extends ItemStackHandler {

        ItemStackHandler main;

        public BeltItemHandler(int i) {
            super(i);
        }

        public BeltItemHandler(int i, ItemStackHandler mainHandler) {
            super(i);
            main = mainHandler;
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
            if(stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (main != null) {
                ItemStack returnStack = main.insertItem(0, stack, simulate);
                if (!simulate && returnStack.isEmpty()) {
                    progress = 10;
                    previousProgress = 10;
                }
                return returnStack;
            }

            ItemStack returnStack = super.insertItem(slot, stack, simulate);
            if (returnStack.isEmpty()) {
                previousProgress = -1;
                progress = 0;
                insertedTick = world.getWorldTime();
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

        enum SIDE {
            L,
            R;
        }
    }

}
