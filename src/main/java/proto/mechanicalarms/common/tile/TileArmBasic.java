package proto.mechanicalarms.common.tile;

import proto.mechanicalarms.common.logic.behavior.Action;
import proto.mechanicalarms.common.logic.behavior.ActionResult;
import proto.mechanicalarms.common.logic.behavior.InteractionType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;


public class TileArmBasic extends TileArmBase {

    AxisAlignedBB renderBB;





    public TileArmBasic() {
        super(1f, InteractionType.ITEM);
    }

    @Override
    public ActionResult interact(Action action, Pair<BlockPos, EnumFacing> blkFace) {
        TileEntity te = world.getTileEntity(blkFace.getKey());
        if (te != null) {
            IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, blkFace.getRight());
            if (itemHandler != null) {
                if (action == Action.RETRIEVE) {
                    if (this.itemHandler.getStackInSlot(0).isEmpty()) {
                        for (int i = 0; i < itemHandler.getSlots(); i++) {
                            if (!itemHandler.extractItem(i, 1, true).isEmpty()) {
                                ItemStack itemStack = itemHandler.extractItem(i, 1, true);
                                if (!itemStack.isEmpty()) {
                                    itemStack = itemHandler.extractItem(i, 1, false);
                                    this.itemHandler.insertItem(0, itemStack, false);
                                    return ActionResult.SUCCESS;
                                }
                            }
                        }
                    }
                } else if (action == Action.DELIVER) {
                    ItemStack itemStack = this.itemHandler.extractItem(0, 1, true);
                    if (!itemStack.isEmpty()) {
                        for (int i = 0; i < itemHandler.getSlots(); i++) {
                            if (itemHandler.insertItem(i, itemStack, true).isEmpty()) {
                                itemStack = this.itemHandler.extractItem(0, 1, false);
                                itemHandler.insertItem(i, itemStack, false);
                                return ActionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
        }
        return ActionResult.CONTINUE;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", itemHandler.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        itemHandler.deserializeNBT(compound.getCompoundTag("inventory"));
        super.readFromNBT(compound);
    }

    public ItemStack getItemStack() {
        return itemHandler.getStackInSlot(0);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (renderBB == null) {
            renderBB = super.getRenderBoundingBox().grow(5, 5, 5);
        }
        return renderBB;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }
}
