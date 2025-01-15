package proto.mechanicalarms.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import proto.mechanicalarms.common.cap.BeltItemHandler;

public interface IDualInventory{

    ItemStackHandler getLeftItemHandler();

    BeltItemHandler getLeftSideItemHandler();

    ItemStackHandler getRightItemHandler();

    BeltItemHandler getRightSideItemHandler();

    void setProgressLeft(int progressLeft);

    void setPreviousProgressLeft(int previousProgressLeft);

    void setProgressRight(int progressRight);

    void setPreviousProgressRight(int previousProgressRight);

    void updateLastTickLeft();

    void updateLastTickRight();

    void markTileDirty();

    World getTileWorld();

    BlockPos getPosition();
}
