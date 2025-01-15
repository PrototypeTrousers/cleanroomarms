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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.api.capability.IDualSidedHandler;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;


public class TileSplitter extends TileBeltBasic {
    public Side lastOutputSide = Side.L;
    private TileSplitterDummy dummy;
    private boolean worked;

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
    protected void handleItemTransfer(boolean left) {
        TileEntity frontTe;
        EnumFacing facing = getFront();

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

    boolean attemptTransfer(TileEntity frontTe, EnumFacing facing, boolean left) {
        if (frontTe != null) {
            if (frontTe.hasCapability(CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY, facing.getOpposite())) {
                IDualSidedHandler cap = frontTe.getCapability(CapabilityDualSidedHandler.DUAL_SIDED_CAPABILITY, facing.getOpposite());
                if (cap != null) {
                    if (left) {
                        if (cap.insertLeft(leftItemHandler.extractItem(0, 1, true), true) != leftItemHandler.getStackInSlot(0)) {
                            cap.insertLeft(leftItemHandler.extractItem(0, 1, false), false);
                            progressLeft = 0;
                            return true;
                        }
                    } else {
                        if (cap.insertRight(rightItemHandler.extractItem(0, 1, true), true) != rightItemHandler.getStackInSlot(0)) {
                            cap.insertRight(rightItemHandler.extractItem(0, 1, false), false);
                            progressRight = 0;
                            return true;
                        }
                    }
                } else {
                    IItemHandler icap = frontTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                    if (icap != null) {
                        if (left) {
                            if (icap.insertItem(0, leftItemHandler.extractItem(0, 1, true), true) != leftItemHandler.getStackInSlot(0)) {
                                icap.insertItem(0, leftItemHandler.extractItem(0, 1, false), false);
                                progressLeft = 0;
                                return true;
                            }
                        } else {
                            if (icap.insertItem(0, rightItemHandler.extractItem(0, 1, true), true) != rightItemHandler.getStackInSlot(0)) {
                                icap.insertItem(0, rightItemHandler.extractItem(0, 1, false), false);
                                progressRight = 0;
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (worked) {
            lastOutputSide = lastOutputSide.opposite();
            worked = false;
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
}

