package proto.mechanicalarms.common.cap;

import net.minecraft.item.ItemStack;
import proto.mechanicalarms.api.IDualInventory;
import proto.mechanicalarms.api.capability.IDualSidedHandler;
import proto.mechanicalarms.common.tile.Side;

public class DualSidedHandler implements IDualSidedHandler {
    private final IDualInventory dualInventory;
    Side side;

    public DualSidedHandler(IDualInventory dualInventory, Side side) {
        this.dualInventory = dualInventory;
        this.side = side;
    }

    public DualSidedHandler(IDualInventory dualInventory) {
        this.dualInventory = dualInventory;
    }

    @Override
    public ItemStack insertLeft(ItemStack insert, boolean simulate) {
        if (side == Side.L) {
            return dualInventory.getLeftSideItemHandler().insertItem(0, insert, simulate);
        }
        return dualInventory.getLeftItemHandler().insertItem(0, insert, simulate);
    }

    @Override
    public ItemStack insertRight(ItemStack insert, boolean simulate) {
        if (side == Side.R) {
            return dualInventory.getRightSideItemHandler().insertItem(0, insert, simulate);
        }
        return dualInventory.getRightItemHandler().insertItem(0, insert, simulate);
    }
}
