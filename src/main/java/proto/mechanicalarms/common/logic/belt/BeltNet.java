package proto.mechanicalarms.common.logic.belt;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.tile.BeltHoldingEntity;
import proto.mechanicalarms.common.tile.TileBeltBasic;

import java.util.*;

public class BeltNet {
    public static HashMap<World, BeltNet> beltNets = new HashMap<>();
    public List<BeltHoldingEntity> pendingEntities = new ArrayList<>();
    public List<BeltHoldingEntity> headCandidates = new ArrayList<>();
    public Map<BeltHoldingEntity, BeltGroup> beltGroups = new HashMap<>();
    public ObjectOpenHashSet<BeltGroup> groups = new ObjectOpenHashSet<>();
    public BeltNet(World world) {
    }

    static public void addToGroup(BeltHoldingEntity entity) {
        if (entity == null) {
            boolean wtf = true;
        }
        BeltNet beltNet = beltNets.computeIfAbsent(entity.getWorld(), w -> new BeltNet(entity.getWorld()));
        if ((entity.getConnected() & (1 << 1)) == 0) {
            beltNet.headCandidates.add(entity);
        } else {
            beltNet.pendingEntities.add(entity);
        }
    }

    public void groupBelts() {
        Iterator<BeltHoldingEntity> iterator = headCandidates.iterator();
        while (iterator.hasNext()) {
            BeltHoldingEntity entity = iterator.next();
            if (beltGroups.containsKey(entity)) {
                iterator.remove();
                continue;
            }
            BeltGroup group = new BeltGroup();
            if (entity.isBackConnected()) {
                groupBelts(entity, group);
                beltGroups.put(entity, group);
                groups.add(group);
            } else {
                group.addBeltHoldingEntity(entity);
            }
            iterator.remove();
        }
        Iterator<BeltHoldingEntity> iter = pendingEntities.iterator();
        while (iter.hasNext()) {
            BeltHoldingEntity entity = iter.next();
            if (beltGroups.containsKey(entity)) {
                iter.remove();
                continue;
            }
            BeltGroup group = new BeltGroup();
            beltGroups.put(entity, group);
            groups.add(group);
            if (entity.isBackConnected()) {
                groupBelts(entity, group);
            } else {
                group.addBeltHoldingEntity(entity);
            }
            iter.remove();
        }
    }

    void groupBelts(BeltHoldingEntity entity, BeltGroup group) {
        group.addBeltHoldingEntity(entity);

        if (entity.isOnlyOppositeOrVerticalConnected()) {
            BeltHoldingEntity backBelt = getBackBelt(entity);
            if (backBelt != null && !beltGroups.containsKey(entity)){
                groupBelts(backBelt, group);
            }
        }
    }

    // Assuming you have a method to get the BeltHoldingEntity by direction
    private BeltHoldingEntity getBackBelt(BeltHoldingEntity entity) {
        // Implement this method to get the BeltHoldingEntity by direction
        // For example, you can use the world and pos to get the adjacent tile entity
        World world = entity.getWorld();
        BlockPos pos = entity.getPos();
        Directions direction = entity.getDirection();
        EnumFacing front = entity.getFront();

        if (entity.getDirection().getRelativeHeight() == Directions.RelativeHeight.LEVEL) {
            // Check the opposite direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    return backBelt;
                }
            }

            // Check the opposite below direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    return backBelt;
                }
            }
        } else if (entity.getDirection().getRelativeHeight() == Directions.RelativeHeight.ABOVE) {
            // Check the opposite direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    return backBelt;
                }
            }

            // Check the opposite below direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    return backBelt;
                }
            }
        } else if (entity.getDirection().getRelativeHeight() == Directions.RelativeHeight.BELOW) {
            // Check the opposite above direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite()).up()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    return backBelt;
                }
            }
        }
        return null;
    }

    public void updateConnected(BeltHoldingEntity beltHoldingEntity) {
        int mask = 0; // Initialize the bitmask as an integer
        Directions direction = beltHoldingEntity.getDirection();
        BlockPos pos = beltHoldingEntity.getPos();
        World world = beltHoldingEntity.getWorld();
        EnumFacing front = beltHoldingEntity.getFront();

        if (direction.getRelativeHeight() == Directions.RelativeHeight.LEVEL) {
            // Check the left connection and set bit 0 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().rotateYCCW())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front.rotateY()) {
                    mask |= (1 << 0); // Set bit 0
                }
            }

            // Check the front connection and set bit 1 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() != front.getOpposite()) {
                    mask |= (1 << 1); // Set bit 1
                }
            }

            // Check the right connection and set bit 2 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().rotateY())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front.rotateYCCW()) {
                    mask |= (1 << 2); // Set bit 2
                }
            }

            // Check the opposite direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    mask |= (1 << 3); // Set bit 3
                }
            }

            // Check the opposite below direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    mask |= (1 << 3); // Set bit 3
                }
            }
        } else if (direction.getRelativeHeight() == Directions.RelativeHeight.ABOVE) {
            // Check the front above connection and set bit 1 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing()).up()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() != front.getOpposite()) {
                    mask |= (1 << 1); // Set bit 1
                }
            }

            // Check the opposite direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    mask |= (1 << 3); // Set bit 3
                }
            }

            // Check the opposite below direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite()).down()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    mask |= (1 << 3); // Set bit 3
                }
            }
        } else if (direction.getRelativeHeight() == Directions.RelativeHeight.BELOW) {
            // Check the opposite above direction and set bit 3 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing().getOpposite()).up()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() == front) {
                    mask |= (1 << 3); // Set bit 3
                }
            }

            // Check the front connection and set bit 1 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing())) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() != front.getOpposite()) {
                    mask |= (1 << 1); // Set bit 1
                }
            }

            // Check the front above connection and set bit 1 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing()).up()) instanceof TileBeltBasic backBelt) {
                if (backBelt.getFront() != front.getOpposite()) {
                    mask |= (1 << 1); // Set bit 1
                }
            }
        }

        // Use the mask for further operations as needed, for example:
        //connected = mask; // Store the bitmask in 'connected' (now an integer)
    }
}
