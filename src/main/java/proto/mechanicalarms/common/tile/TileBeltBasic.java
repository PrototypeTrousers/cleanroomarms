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
import org.jetbrains.annotations.Nullable;


public class TileBeltBasic extends TileEntity implements ITickable, IGuiHolder {

    AxisAlignedBB renderBB;
    AxisAlignedBB pickerBB;
    int progress = 0;
    EnumFacing front;
    Slope slope;

    protected ItemStackHandler leftItemHandler = new BeltItemHandler(5);
    protected ItemStackHandler rightItemHandler = new BeltItemHandler(5);

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("leftInventory", leftItemHandler.serializeNBT());
        compound.setTag("rightInventory", rightItemHandler.serializeNBT());
        compound.setInteger("front", front.ordinal());
        compound.setInteger("slope", slope.ordinal());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        leftItemHandler.deserializeNBT(compound.getCompoundTag("leftInventory"));
        rightItemHandler.deserializeNBT(compound.getCompoundTag("rightInventory"));
        front = EnumFacing.byIndex(compound.getInteger("front"));
        slope = Slope.values()[compound.getInteger("slope")];
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
        panel.child(new ItemSlot().slot(leftItemHandler, 0));
        panel.bindPlayerInventory();
        return panel;
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

    public Slope getSlope() {
        return slope;
    }

    public class BeltItemHandler extends ItemStackHandler {
        public BeltItemHandler(int i) {
            super(i);
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

}
