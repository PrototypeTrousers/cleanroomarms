package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import proto.mechanicalarms.common.logic.belt.BeltNet;


public class TileBeltBasic extends BeltHoldingEntity {

    @Override
    public ModularPanel buildUI(GuiData guiData, GuiSyncManager guiSyncManager) {
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
