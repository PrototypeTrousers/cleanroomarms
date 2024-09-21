package proto.mechanicalarms.common.logic.behavior;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class WorkStatus implements INBTSerializable<NBTTagCompound> {
    private ActionTypes type;
    private Action action;

    public WorkStatus() {
        this.type = ActionTypes.IDLING;
        this.action = Action.IDLING;
    }

    public void idle() {
        this.type = ActionTypes.IDLING;
        this.action = Action.IDLING;
    }

    public ActionTypes getType() {
        return type;
    }

    public Action getAction() {
        return action;
    }

    public Action setAction(Action value) {
        this.action = value;
        return value;
    }

    public ActionTypes setType(ActionTypes value) {
        this.type = value;
        return type;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("type", type.name());
        compound.setString("action", action.name());
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.type = ActionTypes.valueOf(nbt.getString("type"));
        this.action = Action.valueOf(nbt.getString("action"));
    }
}
