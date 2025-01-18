package proto.mechanicalarms.common.logic.belt;

import proto.mechanicalarms.common.tile.BeltHoldingEntity;

import java.util.ArrayList;
import java.util.List;

public class BeltGroup {
    private final List<BeltHoldingEntity> beltHoldingEntities;

    public BeltGroup() {
        beltHoldingEntities = new ArrayList<>();
    }

    public void addBeltHoldingEntity(BeltHoldingEntity entity) {
        beltHoldingEntities.add(entity);
    }

    public List<BeltHoldingEntity> getBeltHoldingEntities() {
        return beltHoldingEntities;
    }
}
