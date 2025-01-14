package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import proto.mechanicalarms.api.capability.IDualSidedHandler;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;

public class TileSplitterDummy extends BeltTileEntity {
    TileSplitter controller;

    protected boolean handleSpliterItemTransfer(boolean left) {
        TileEntity frontTe;
        EnumFacing facing = getFront();

        if (controller.lastOutputSide == Side.L) {
            frontTe = world.getTileEntity(pos.offset(facing));
        } else {
            frontTe = world.getTileEntity(pos.offset(facing.rotateYCCW()).offset(facing));

        }

        boolean shouldSwitchSides = attemptTransfer(frontTe, facing, left);

        if (!shouldSwitchSides) {
            if (controller.lastOutputSide == Side.R) {
                frontTe = world.getTileEntity(pos.offset(facing.rotateYCCW()).offset(facing));
            } else {
                frontTe = world.getTileEntity(pos.offset(facing));
            }
            attemptTransfer(frontTe, facing, left);
        }

        if (frontTe == null) {
            return true;
        }

        return shouldSwitchSides;
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
            boolean transfered = false;
            if (tickLeft) {
                if (progressLeft < 3 && !leftItemHandler.getStackInSlot(0).isEmpty()) {
                    previousProgressLeft++;
                    progressLeft++;
                }
                if (progressLeft >= 3) {
                    transfered = handleSpliterItemTransfer(true);
                }
            }
            if (tickRight) {
                if (progressRight < 3 && !rightItemHandler.getStackInSlot(0).isEmpty()) {
                    previousProgressRight++;
                    progressRight++;
                }
                if (progressRight >= 3) {
                    transfered = handleSpliterItemTransfer(false);
                }
            }
            if (transfered) {
                if (controller.lastOutputSide == Side.L) {
                    controller.lastOutputSide = Side.R;
                } else {
                    controller.lastOutputSide = Side.L;
                }
            }
        } else {
            if (tickLeft) {
                if (progressLeft < 3) {
                    previousProgressLeft = progressLeft;
                    progressLeft++;
                } else {
                    previousProgressLeft = progressLeft;
                }
            }
            if (tickRight) {
                if (progressRight < 3) {
                    previousProgressRight = progressRight;
                    progressRight++;
                } else {
                    previousProgressRight = progressRight;
                }
            }
        }
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
