package proto.mechanicalarms.common.logic.belt;

import proto.mechanicalarms.common.tile.BeltHoldingEntity;

import java.util.*;

public class BeltGroup {
    private final List<BeltHoldingEntity> belts;

    public BeltGroup() {
        belts = new ArrayList<>();
    }

    public void addBeltHoldingEntity(BeltHoldingEntity entity) {
        belts.add(entity);
    }

    public List<BeltHoldingEntity> getBelts() {
        return belts;
    }

    public void removeBeltHoldingEntity(BeltHoldingEntity entity) {
        belts.remove(entity);
    }

    public boolean isEmpty() {
        return belts.isEmpty();
    }
}
