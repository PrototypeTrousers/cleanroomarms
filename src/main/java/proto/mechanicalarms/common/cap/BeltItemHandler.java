package proto.mechanicalarms.common.cap;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import proto.mechanicalarms.api.IBeltLogic;
import proto.mechanicalarms.common.tile.Side;

public class BeltItemHandler extends ItemStackHandler {

    private final IBeltLogic dualInventory;
    BeltItemHandler main;
    Side side;

    public BeltItemHandler(IBeltLogic dualInventory, int i, Side side) {
        super(i);
        this.dualInventory = dualInventory;
        this.side = side;
    }

    public BeltItemHandler(IBeltLogic dualInventory, int i, BeltItemHandler mainHandler, Side side) {
        super(i);
        this.dualInventory = dualInventory;
        this.main = mainHandler;
        this.side = side;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return 1;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (main != null) {
            main.setStackInSlot(0, stack);
            return;
        }
        super.setStackInSlot(slot, stack);
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (main != null) {
            return main.getStackInSlot(0);
        }
        return super.getStackInSlot(slot);
    }

    @Override
    public int getSlots() {
        if (main != null) {
            return 1;
        }
        return super.getSlots();
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (main != null) {
            ItemStack returnStack = main.insertItem(0, stack, simulate);
            if (!simulate && returnStack.isEmpty()) {
                if (side == Side.L) {
                    dualInventory.setProgressLeft(1);
                    dualInventory.updateLastTickLeft();
                } else {
                    dualInventory.setProgressRight(1);
                    dualInventory.updateLastTickRight();
                }
            }
            return returnStack;
        }

        ItemStack returnStack = super.insertItem(slot, stack, simulate);
        if (!simulate && returnStack.isEmpty()) {
            if (side == Side.L) {
                dualInventory.setProgressLeft(0);
                dualInventory.updateLastTickLeft();
            } else {
                dualInventory.setProgressRight(0);
                dualInventory.updateLastTickRight();
            }
        }
        return returnStack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (main != null) {
            return main.extractItem(0, amount, simulate);
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        World world = dualInventory.getHolderWorld();
        if (!world.isRemote) {
            dualInventory.markTileDirty();
            ((WorldServer) world).getPlayerChunkMap().markBlockForUpdate(dualInventory.getHolderPosition());
        }
    }
}
