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
        processEntities(headCandidates);
        processEntities(pendingEntities);
    }

    // Handles grouping of entities from a given list
    private void processEntities(List<BeltHoldingEntity> entities) {
        Iterator<BeltHoldingEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            BeltHoldingEntity entity = iterator.next();
            if (beltGroups.containsKey(entity)) {
                iterator.remove();
                continue;
            }

            BeltHoldingEntity front = getFrontBelt(entity);
            if (front != null && front.isOnlyConnectedToSide(entity.getFront())) {
                addToGroup(entity, groups.get(front));
                iterator.remove();
                continue;
            }
            BeltGroup group = new BeltGroup();
            beltGroups.put(entity, group);
            groups.add(group);

            if (entity.isBackConnected()) {
                groupBeltsRecursive(entity, group);
            } else {
                group.addBeltHoldingEntity(entity);
            }

            iterator.remove();
        }
    }

    // Recursively adds connected belts to the group
    private void groupBeltsRecursive(BeltHoldingEntity entity, BeltGroup group) {
        group.addBeltHoldingEntity(entity);
        beltGroups.put(entity, group);

        if (entity.isOnlyBackConnected()) {
            BeltHoldingEntity backBelt = getBackBelt(entity);
            if (backBelt != null && !beltGroups.containsKey(backBelt)) {
                groupBeltsRecursive(backBelt, group);
            }
        }
    }

    // Adds a belt to a specific group
    public void addToGroup(BeltHoldingEntity entity, BeltGroup group) {
        if (entity == null || group == null) return;

        // Remove entity from its current group if it exists
        BeltGroup currentGroup = beltGroups.get(entity);
        if (currentGroup != null) {
            removeFromGroup(entity, currentGroup);
        }

        group.addBeltHoldingEntity(entity);
        beltGroups.put(entity, group);
    }

    // Removes a belt from its group and handles splitting groups if necessary
    public void removeFromGroup(BeltHoldingEntity entity, BeltGroup group) {
        if (entity == null || group == null) return;

        group.removeBeltHoldingEntity(entity);
        beltGroups.remove(entity);

        // If the group becomes disconnected, split it into smaller groups
        if (group.isEmpty()) {
            groups.remove(group);
        }
        if (group == groups.get(getBackBelt(entity)) && group == groups.get(getFrontBelt(entity))) {
            splitGroup(group);
        }
    }

    // Splits a group into smaller connected groups
    private void splitGroup(BeltGroup group) {
        groups.remove(group);

        // Rebuild smaller groups from the disconnected belts
        List<BeltHoldingEntity> disconnectedBelts = new ArrayList<>(group.getBelts());
        for (BeltHoldingEntity entity : disconnectedBelts) {
            if (!beltGroups.containsKey(entity)) {
                BeltGroup newGroup = new BeltGroup();
                groups.add(newGroup);
                groupBeltsRecursive(entity, newGroup);
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

    private BeltHoldingEntity getFrontBelt(BeltHoldingEntity entity) {
        World world = entity.getWorld();
        BlockPos pos = entity.getPos();
        Directions direction = entity.getDirection();
        EnumFacing front = entity.getFront();

        if (entity.getDirection().getRelativeHeight() == Directions.RelativeHeight.LEVEL) {
            // Check the front direction and set bit 1 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing())) instanceof TileBeltBasic frontBelt) {
                return frontBelt;
            }
        } else if (entity.getDirection().getRelativeHeight() == Directions.RelativeHeight.ABOVE) {
            // Check the front direction and set bit 1 if true
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing()).up()) instanceof TileBeltBasic frontBelt) {
                return frontBelt;
            }
        } else if (entity.getDirection().getRelativeHeight() == Directions.RelativeHeight.BELOW) {
            if (world.getTileEntity(pos.offset(direction.getHorizontalFacing())) instanceof TileBeltBasic frontBelt) {
                return frontBelt;
            } else if (world.getTileEntity(pos.offset(direction.getHorizontalFacing()).down()) instanceof TileBeltBasic frontBelt) {
                return frontBelt;
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
