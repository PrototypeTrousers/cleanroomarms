package proto.mechanicalarms.common.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.ItemStackHandler;


public class TileBeltBasic extends TileEntity {

    AxisAlignedBB renderBB;

    protected ItemStackHandler leftItemHandler = new ItemStackHandler(5);
    protected ItemStackHandler rightItemHandler = new ItemStackHandler(5);

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("leftInventory", leftItemHandler.serializeNBT());
        compound.setTag("rightInventory", rightItemHandler.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        leftItemHandler.deserializeNBT(compound.getCompoundTag("leftInventory"));
        rightItemHandler.deserializeNBT(compound.getCompoundTag("rightInventory"));
        super.readFromNBT(compound);
    }

    public ItemStackHandler getleftItemHandler() {
        return leftItemHandler;
    }

    public ItemStackHandler getRightItemHandler() {
        return rightItemHandler;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (renderBB == null) {
            renderBB = super.getRenderBoundingBox().expand(4, 4, 4);
        }
        return renderBB;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }
}
