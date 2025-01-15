package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;

public class TileSplitterDummy extends BeltHoldingEntity {
    TileSplitter controller;

    @Override
    public void onLoad() {
        if (controller == null) {
            TileEntity te = world.getTileEntity(pos.offset(getFront().rotateYCCW()));
            if (te instanceof TileSplitter ts) {
                if (ts.getFront() == this.getFront()) {
                    controller = ts;
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
    public ModularPanel buildUI(GuiData data, GuiSyncManager syncManager) {
        return controller.buildUI(data, syncManager);
    }
}
