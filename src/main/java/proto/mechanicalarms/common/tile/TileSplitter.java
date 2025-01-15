package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;


public class TileSplitter extends TileBeltBasic {
    public Side lastOutputSide = Side.L;
    private TileSplitterDummy dummy;
    boolean worked;
    private BeltHoldingEntity lastUpdated;

    public TileSplitter() {
        logic = new SplitterUpdatingLogic(this, this);
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
    public void update() {
        if (!world.isRemote) {
            if (lastUpdated == this) {
                dummy.logic.update();
                if (worked) {
                    lastOutputSide = lastOutputSide.opposite();
                    worked = false;
                }
                this.logic.update();
                if (worked) {
                    lastOutputSide = lastOutputSide.opposite();
                    worked = false;
                }
            } else {
                this.logic.update();
                if (worked) {
                    lastOutputSide = lastOutputSide.opposite();
                    worked = false;
                }
                dummy.logic.update();
                if (worked) {
                    lastOutputSide = lastOutputSide.opposite();
                    worked = false;
                }
            }
        }
    }


    @Override
    public void onLoad() {
        if (dummy == null) {
            TileEntity te = world.getTileEntity(pos.offset(getFront().rotateY()));
            if (te instanceof TileSplitterDummy ts) {
                if (ts.getFront() == this.getFront()) {
                    dummy = ts;
                }
            }
        }
        if (lastUpdated == null) {
            lastUpdated = this;
        }
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
                attemptTransfer(frontTe, facing, left);
            }
            worked = true;
        }
    }
}

