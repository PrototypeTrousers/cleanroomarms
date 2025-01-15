package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;


public class TileBeltBasic extends BeltHoldingEntity implements ITickable {

    @Override
    public ModularPanel buildUI(GuiData guiData, GuiSyncManager guiSyncManager) {
        ModularPanel panel = ModularPanel.defaultPanel("tutorial_gui");
        panel.child(new ItemSlot().slot(leftItemHandler, 0));
        panel.child(new ItemSlot().slot(rightItemHandler, 0).left(18));
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
        boolean tickLeft = true;
        if (progressLeft == 0 && insertedTickLeft == world.getTotalWorldTime()) {
            progressLeft = 0;
            previousProgressLeft = -1;
            insertedTickLeft = -1;
            tickLeft = false;
        }

        boolean tickRight = true;
        if (progressRight == 0 && insertedTickRight == world.getTotalWorldTime()) {
            progressRight = 0;
            previousProgressRight = -1;
            insertedTickRight = -1;
            tickRight = false;
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
            if (tickLeft) {
                if (progressLeft < 3 && !leftItemHandler.getStackInSlot(0).isEmpty()) {
                    previousProgressLeft++;
                    progressLeft++;
                }
                if (progressLeft >= 3) {
                    handleItemTransfer(true);
                }
            } if (tickRight) {
                if (progressRight < 3 && !rightItemHandler.getStackInSlot(0).isEmpty()) {
                    previousProgressRight++;
                    progressRight++;
                }
                if (progressRight >= 3) {
                    handleItemTransfer(false);
                }
            }
        } else {
            if (tickLeft) {
                if (progressLeft < 3) {
                    previousProgressLeft = progressLeft;
                    progressLeft++;
                }else {
                    previousProgressLeft = progressLeft;
                }
            }
            if (tickRight) {
                if(progressRight < 3) {
                    previousProgressRight = progressRight;
                    progressRight++;
                } else {
                    previousProgressRight = progressRight;
                }
            }
        }
    }
}
