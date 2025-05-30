package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;
import proto.mechanicalarms.common.logic.belt.BeltNet;


public class TileSplitter extends TileBeltBasic implements IGuiHolder {
    public Side lastOutputSide = Side.L;
    private TileSplitterDummy dummy;
    public BeltHoldingEntity lastPushed = this;
    boolean switchSide;

    @Override
    protected void initLogic() {
        logic = new SplitterUpdatingLogic(this, this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("lastOutput", this.lastOutputSide.ordinal());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.lastOutputSide = Side.values()[compound.getInteger("lastOutput")];
    }

    @Override
    public ModularPanel buildUI(GuiData guiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
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
    public void update() {
        if (!world.isRemote) {
            if (lastPushed == this) {
                dummy.logic.update();
                if (switchSide) {
                    lastOutputSide = lastOutputSide.opposite();
                    switchSide = false;
                }
                this.logic.update();
            } else {
                this.logic.update();
                if (switchSide) {
                    lastOutputSide = lastOutputSide.opposite();
                    switchSide = false;
                }
                dummy.logic.update();
            }
            if (switchSide) {
                lastOutputSide = lastOutputSide.opposite();
                switchSide = false;
            }
        }
    }


    @Override
    public void onLoad() {
        BeltNet.addToGroup(this);
        //super.onLoad();
    }

    public void setDummy(TileSplitterDummy dummy) {
        this.dummy = dummy;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY) {
            return (T) dualBack;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY;
    }

    class SplitterUpdatingLogic extends BeltUpdatingLogic {

        SplitterUpdatingLogic(BeltHoldingEntity beltHoldingEntity, BeltHoldingEntity holder) {
            super(beltHoldingEntity, holder);
        }

        @Override
        protected void handleItemTransfer(boolean left) {
            TileEntity frontTe;
            EnumFacing facing = beltHoldingEntity.getFront();

            if (lastOutputSide == Side.L) {
                frontTe = world.getTileEntity(pos.offset(facing.rotateY()).offset(facing));
            } else {
                frontTe = world.getTileEntity(pos.offset(facing));
            }

            boolean transferred = attemptTransfer(frontTe, facing, left);
            if (!transferred) {
                if (lastOutputSide == Side.R) {
                    frontTe = world.getTileEntity(pos.offset(facing.rotateY()).offset(facing));
                } else {
                    frontTe = world.getTileEntity(pos.offset(facing));
                }
                transferred = attemptTransfer(frontTe, facing, left);
            } if (transferred ) {
                lastPushed = TileSplitter.this;
                switchSide = true;
            }
        }
    }
}

