package proto.mechanicalarms.common.logic.behavior;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.tuple.Pair;

public class Targeting implements INBTSerializable<NBTTagCompound> {

    Pair<BlockPos, EnumFacing> source;
    Pair<BlockPos, EnumFacing> target;
    private Vec3d sourceVec;
    private Vec3d targetVec;

    public Targeting() {

    }

    public Vec3d getSourceVec() {
        return sourceVec;
    }

    public Vec3d getTargetVec() {
        return targetVec;
    }

    public Pair<BlockPos, EnumFacing> getSource() {
        return source;
    }

    public Pair<BlockPos, EnumFacing> getTarget() {
        return target;
    }

    public EnumFacing getTargetFacing() {
        return target.getRight();
    }

    public EnumFacing getSourceFacing() {
        return source.getRight();
    }

    public void setSource(BlockPos sourcePos, EnumFacing sourceFacing) {
        this.source = Pair.of(sourcePos, sourceFacing);
        BlockPos vecPos = sourcePos.offset(sourceFacing, 1);
        this.sourceVec = new Vec3d(vecPos.getX() + 0.5, vecPos.getY() + 0.5, vecPos.getZ() + 0.5);
    }

    public void setTarget(BlockPos targetPos, EnumFacing targetFacing) {
        this.target = Pair.of(targetPos, targetFacing);
        BlockPos vecPos = targetPos.offset(targetFacing, 1);
        this.targetVec = new Vec3d(vecPos.getX() + 0.5, vecPos.getY() + 0.5, vecPos.getZ() + 0.5);
    }

    public boolean hasInput() {
        return source != null;
    }

    public boolean hasOutput() {
        return target != null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        if (source != null) {
            compound.setTag("sourcePos", NBTUtil.createPosTag(source.getKey()));
            compound.setInteger("sourceFacing", source.getValue().ordinal());
        }
        if (target != null) {
            compound.setTag("targetPos", NBTUtil.createPosTag(target.getKey()));
            compound.setInteger("targetFacing", target.getValue().ordinal());
        }
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        setSource(NBTUtil.getPosFromTag(compound.getCompoundTag("sourcePos")), EnumFacing.byIndex(compound.getInteger("sourceFacing")));
        setTarget(NBTUtil.getPosFromTag(compound.getCompoundTag("targetPos")), EnumFacing.byIndex(compound.getInteger("targetFacing")));
    }
}
