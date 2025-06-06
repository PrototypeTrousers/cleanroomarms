package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;

public class TileSplitterDummy extends BeltHoldingEntity {
    TileSplitter controller;

    @Override
    protected void initLogic() {
        logic = new SplitterDummyUpdatingLogic(this, this);
    }

    @Override
    public void onLoad() {
        if (controller == null) {
            TileEntity te = world.getTileEntity(pos.offset(getFront().rotateYCCW()));
            if (te instanceof TileSplitter ts) {
                if (ts.getFront() == this.getFront()) {
                    controller = ts;
                    controller.setDummy(this);
                }
            }
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

    @Override
    public ModularPanel buildUI(GuiData guiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        return controller.buildUI(guiData, panelSyncManager, uiSettings);
    }

    class SplitterDummyUpdatingLogic extends BeltUpdatingLogic {

        SplitterDummyUpdatingLogic(BeltHoldingEntity beltHoldingEntity, BeltHoldingEntity holder) {
            super(beltHoldingEntity, holder);
        }

        @Override
        protected void handleItemTransfer(boolean left) {
            TileEntity frontTe;
            EnumFacing facing = getFront();

            if (controller.lastOutputSide == Side.L) {
                frontTe = world.getTileEntity(pos.offset(facing));
            } else {
                frontTe = world.getTileEntity(pos.offset(facing.rotateYCCW()).offset(facing));
            }

            boolean transferred = attemptTransfer(frontTe, facing, left);
            if (!transferred) {
                if (controller.lastOutputSide == Side.R) {
                    frontTe = world.getTileEntity(pos.offset(facing));
                } else {
                    frontTe = world.getTileEntity(pos.offset(facing.rotateYCCW()).offset(facing));
                }
                transferred = attemptTransfer(frontTe, facing, left);
            }
            if (transferred) {
                controller.lastPushed = TileSplitterDummy.this;
                controller.switchSide = true;
            }
        }
    }
}
