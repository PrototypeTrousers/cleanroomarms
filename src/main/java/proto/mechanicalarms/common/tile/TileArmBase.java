package proto.mechanicalarms.common.tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;
import proto.mechanicalarms.common.logic.behavior.*;
import proto.mechanicalarms.common.logic.movement.MotorCortex;

import static net.minecraftforge.common.util.Constants.NBT.TAG_FLOAT;
import static proto.mechanicalarms.common.logic.behavior.Action.DELIVER;
import static proto.mechanicalarms.common.logic.behavior.Action.RETRIEVE;

public abstract class TileArmBase extends TileEntity implements ITickable, IGuiHolder {
    private final Targeting targeting = new Targeting();
    private final MotorCortex motorCortex;
    private final WorkStatus workStatus = new WorkStatus();
    protected ItemStackHandler itemHandler = new ArmItemHandler(1);
    private Vec3d armPoint;

    private int progress = 0;


    public TileArmBase(float armSize, InteractionType interactionType) {
        super();
        motorCortex = new MotorCortex(this, armSize, interactionType);
    }

    public float[] getAnimationRotation(int idx) {
        return motorCortex.getAnimationRotation(idx);
    }

    public float[] getRotation(int idx) {
        return motorCortex.getRotation(idx);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        // getUpdateTag() is called whenever the chunkdata is sent to the
        // client. In contrast getUpdatePacket() is called when the tile entity
        // itself wants to sync to the client. In many cases you want to send
        // over the same information in getUpdateTag() as in getUpdatePacket().
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        // Prepare a packet for syncing our TE to the client. Since we only have to sync the stack
        // and that's all we have we just write our entire NBT here. If you have a complex
        // tile entity that doesn't need to have all information on the client you can write
        // a more optimal NBT here.
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        // Here we get the packet from the server and read it into our client side tile entity
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        armPoint = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        motorCortex.deserializeNBT(compound.getTagList("rotation", TAG_FLOAT));
        targeting.deserializeNBT(compound.getCompoundTag("targeting"));
        workStatus.deserializeNBT(compound.getCompoundTag("workStatus"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("rotation", motorCortex.serializeNBT());
        compound.setTag("targeting", targeting.serializeNBT());
        compound.setTag("workStatus", workStatus.serializeNBT());
        return compound;
    }

    @Override
    public ModularPanel buildUI(GuiData guiData, PanelSyncManager panelSyncManager) {
        ModularPanel panel = ModularPanel.defaultPanel("tutorial_gui");
        panel.child(new ProgressWidget()
                .size(20)
                .leftRel(0.5f).topRelAnchor(0.25f, 0.5f)
                .texture(GuiTextures.PROGRESS_ARROW, 20)
                .value(new DoubleSyncValue(() -> this.progress / 100.0, val -> this.progress = (int) (val * 100))));
        panel.child(new ItemSlot().slot(itemHandler, 0));
        panel.bindPlayerInventory();
        return panel;
    }

    public abstract ActionResult interact(Action retrieve, Pair<BlockPos, EnumFacing> blkFacePair);

    @Override
    public void update() {
        if (workStatus.getType() == ActionTypes.IDLING) {
            if (hasInput() && hasOutput()) {
                updateWorkStatus(ActionTypes.MOVEMENT, RETRIEVE);
            }
        } else if (workStatus.getType() == ActionTypes.MOVEMENT) {
            if (workStatus.getAction() == Action.RETRIEVE) {
                ActionResult result = motorCortex.move(armPoint, targeting.getSourceVec(), targeting.getSourceFacing());
                if (result == ActionResult.SUCCESS) {
                    updateWorkStatus(ActionTypes.INTERACTION, RETRIEVE);
                }
            } else if (workStatus.getAction() == DELIVER) {
                if (itemHandler.getStackInSlot(0).isEmpty()){
                    updateWorkStatus(ActionTypes.MOVEMENT, RETRIEVE);
                } else {
                    ActionResult result = motorCortex.move(armPoint, targeting.getTargetVec(), targeting.getTargetFacing());
                    if (result == ActionResult.SUCCESS) {
                        updateWorkStatus(ActionTypes.INTERACTION, DELIVER);
                    }
                }
            }
        } else if (workStatus.getType() == ActionTypes.INTERACTION) {
            if (workStatus.getAction() == Action.RETRIEVE) {
                ActionResult result = interact(RETRIEVE, targeting.getSource());
                if (result == ActionResult.SUCCESS) {
                    updateWorkStatus(ActionTypes.MOVEMENT, DELIVER);
                }
            } else if (workStatus.getAction() == DELIVER) {
                ActionResult result = interact(DELIVER, targeting.getTarget());
                if (result == ActionResult.SUCCESS) {
                    updateWorkStatus(ActionTypes.MOVEMENT, RETRIEVE);
                }
            }
        }
        if (!getWorld().isRemote && this.progress++ == 100) {
            this.progress = 0;
        }
    }

    private void updateWorkStatus(ActionTypes type, Action action) {
        workStatus.setType(type);
        workStatus.setAction(action);
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        markDirty();
    }

    public WorkStatus getWorkStatus() {
        return workStatus;
    }

    public void setSource(BlockPos sourcePos, EnumFacing sourceFacing) {
        targeting.setSource(sourcePos, sourceFacing);
        markDirty();
    }

    public void setTarget(BlockPos targetPos, EnumFacing targetFacing) {
        targeting.setTarget(targetPos, targetFacing);
        markDirty();
    }

    public boolean hasInput() {
        return targeting.hasInput();
    }

    public boolean hasOutput() {
        return targeting.hasOutput();
    }

    public class ArmItemHandler extends ItemStackHandler {
        public ArmItemHandler(int i) {
            super(i);
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }
}
