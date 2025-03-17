package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import proto.mechanicalarms.common.logic.belt.BeltNet;


public class TileBeltBasic extends BeltHoldingEntity {

//    @Override
//    public ModularPanel buildUI(GuiData guiData, GuiSyncManager guiSyncManager) {
//        ModularPanel panel = ModularPanel.defaultPanel("tutorial_gui");
//        panel.child(new ItemSlot().slot(leftItemHandler, 0));
//        panel.child(new ItemSlot().slot(rightItemHandler, 0).left(18));
//        panel.bindPlayerInventory();
//        return panel;
//    }

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
