package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import net.minecraft.util.math.AxisAlignedBB;
import proto.mechanicalarms.common.logic.belt.BeltNet;


public class TileBeltBasic extends BeltHoldingEntity implements IGuiHolder {

    @Override
    public ModularPanel buildUI(GuiData guiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        ModularPanel panel = ModularPanel.defaultPanel("tutorial_gui");
        panel.child(new ItemSlot().slot(leftItemHandler, 0));
        panel.child(new ItemSlot().slot(rightItemHandler, 0).left(18));
        panel.bindPlayerInventory();
        return panel;

    }

    @Override
    protected void initLogic() {
        logic = new BeltUpdatingLogic(this, this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        BeltNet.addToGroup(this);
    }

    @Override
    public void updateConnected() {
        boolean onlySideConnected = false;
        boolean bothSideConnected = false;
        if (connected != -1) {
            if (isOnlyLeftConnected() || isOnlyRightConnected()) {
                onlySideConnected = true;
            } else if (isLeftConnected() && isRightConnected()) {
                bothSideConnected = true;
            }
        }
        super.updateConnected();

        if (onlySideConnected) {
            if (!isOnlyLeftConnected() && !isOnlyRightConnected()) {
                BeltNet.splitFromGroup(this);
            }
        } else if (bothSideConnected) {
            if (!isLeftConnected()) {
                BeltNet.mergeConnectedGroups(this, (BeltHoldingEntity) world.getTileEntity(getPos().offset(getDirection().getHorizontalFacing().rotateY())));
            } else if (!isRightConnected()) {
                BeltNet.mergeConnectedGroups(this, (BeltHoldingEntity) world.getTileEntity(getPos().offset(getDirection().getHorizontalFacing().rotateYCCW())));
            }
        }
    }

    @Override
    public void invalidate() {
        BeltNet.removeFromGroup(this);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (renderBB == null) {
            renderBB = super.getRenderBoundingBox();
        }
        return renderBB;
    }

    public void update() {
        logic.update();
    }
}
