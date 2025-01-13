package proto.mechanicalarms.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public interface IDualInventory{

    ItemStackHandler getLeftItemHandler();

    ItemStackHandler getRightItemHandler();

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
