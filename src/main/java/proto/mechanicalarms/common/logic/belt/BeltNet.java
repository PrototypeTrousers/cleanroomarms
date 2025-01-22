package proto.mechanicalarms.common.logic.belt;

import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
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
    public List<BeltHoldingEntity> toAddBelt = new ArrayList<>();
    public List<BeltHoldingEntity> toRemove = new ArrayList<>();
    public List<BeltHoldingEntity> addFirst = new ArrayList<>();
    public List<BeltHoldingEntity> split = new ArrayList<>();
    public Map<BeltHoldingEntity, BeltGroup> belt2GroupMap = new HashMap<>();
    public ObjectOpenHashSet<BeltGroup> groups = new ObjectOpenHashSet<>();
    public BeltNet(World world) {
    }

    static public void addToGroup(BeltHoldingEntity entity) {
        if (entity == null) {
            boolean wtf = true;
        }
        BeltNet beltNet = beltNets.computeIfAbsent(entity.getWorld(), w -> new BeltNet(entity.getWorld()));
        if ((entity.getConnected() & (1 << 1)) == 0) {
            beltNet.addFirst.add(entity);
        } else {
            beltNet.toAddBelt.add(entity);
        }
    }

    public static void removeFromGroup(BeltHoldingEntity entity) {
        BeltNet beltNet = beltNets.computeIfAbsent(entity.getWorld(), w -> new BeltNet(entity.getWorld()));
        beltNet.toRemove.add(entity);
    }

    public static void splitFromGroup(BeltHoldingEntity entity) {
        BeltNet beltNet = beltNets.computeIfAbsent(entity.getWorld(), w -> new BeltNet(entity.getWorld()));
        beltNet.split.add(entity);
    }

    public void handleRemovals() {
        for (BeltHoldingEntity entity : toRemove) {
            boolean reached = false;
            BeltGroup group = belt2GroupMap.get(entity);
            List<BeltHoldingEntity> currentBeltsInGroup = new ArrayList<>(belt2GroupMap.get(entity).getBelts());
            for (BeltHoldingEntity belt : currentBeltsInGroup) {
                if (entity == belt) {
                    reached = true;
                    removeFromGroup(belt, group);
                    continue;
                }
                if (reached) {
                    removeFromGroup(belt, group);
                    addToGroup(belt);
                }
            }
            belt2GroupMap.remove(entity);
        }
        toRemove.clear();
    }

    public void handleSplits() {
        for (BeltHoldingEntity entity : split) {
            boolean reached = false;
            BeltGroup group = belt2GroupMap.get(entity);
            BeltGroup newGroup = null;
            List<BeltHoldingEntity> currentBeltsInGroup = new ArrayList<>(belt2GroupMap.get(entity).getBelts());
            for (BeltHoldingEntity belt : currentBeltsInGroup) {
                if (entity == belt) {
                    reached = true;
                    continue;
                }
                if (reached) {
                    if (newGroup == null) {
                        newGroup = new BeltGroup();
                        groups.add(newGroup);
                    }
                    removeFromGroup(belt, group);
                    belt2GroupMap.put(belt, newGroup);
                }
            }
        }
        split.clear();
    }

    public void groupBelts() {
        processEntities(addFirst);
        processEntities(toAddBelt);
    }

    // Handles grouping of entities from a given list
    private void processEntities(List<BeltHoldingEntity> entities) {
        Iterator<BeltHoldingEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            BeltHoldingEntity entity = iterator.next();

            // If already grouped, skip this entity
            if (belt2GroupMap.containsKey(entity)) {
                iterator.remove();
                continue;
            }

            List<BeltHoldingEntity> newBelts = new ArrayList<>();
            ObjectObjectMutablePair<BeltGroup, BeltGroup> actualGroup = new ObjectObjectMutablePair<>(null, null);

            // Recursively group belts connected in both directions
            groupBeltsRecursive(entity, newBelts, actualGroup);
            BeltGroup group = actualGroup.left();
            if (group == null) {
                group = new BeltGroup();
                actualGroup.left(group);
            }
            groups.add(actualGroup.left());
            for (BeltHoldingEntity newBelt : newBelts) {
                addToGroup(newBelt, actualGroup.left());
                belt2GroupMap.put(newBelt, actualGroup.left());
            }
            if (actualGroup.right() != null) {
                mergeGroups(actualGroup.right(), group);
            }
            iterator.remove();
        }
    }

    // Recursively adds connected belts to the group
    private void groupBeltsRecursive(BeltHoldingEntity entity, List<BeltHoldingEntity> newBelts, ObjectObjectMutablePair<BeltGroup, BeltGroup> group) {
        if (entity == null || belt2GroupMap.containsKey(entity)) return;
        belt2GroupMap.put(entity, null);

        // Add entity to the group
        newBelts.add(entity);

        // Handle back connections
        if (entity.isBackConnected()) {
            BeltHoldingEntity backBelt = getBackBelt(entity);
            BeltGroup existingGroup = belt2GroupMap.get(backBelt);
            if (existingGroup != null) {
                // If the front belt is already grouped, merge the current group into the existing one
                group.left(existingGroup);
            }
            if (backBelt != null && !belt2GroupMap.containsKey(backBelt)) {
                groupBeltsRecursive(backBelt, newBelts, group);
            }
        }

        // Handle front connections
        if (entity.isFrontConnected()) {
            BeltHoldingEntity frontBelt = getFrontBelt(entity);
            if (frontBelt != null) {
                if (frontBelt.isOnlyConnectedToSide(entity.getFront())) {
                    BeltGroup existingGroup = belt2GroupMap.get(frontBelt);
                    groupBeltsRecursive(frontBelt, newBelts, group);
                    if (existingGroup != null) {
                        // If the front belt is already grouped, merge the current group into the existing one
                        group.left(existingGroup);
                    }
                } else if (frontBelt.isOnlyConnectedToSide(entity.getFront().getOpposite())) {
                    split.add(frontBelt);
                }
            }
        }

        if (entity.isOnlyRightConnected()) {
            BeltHoldingEntity rightBelt = (BeltHoldingEntity) entity.getWorld().getTileEntity(entity.getPos().offset(entity.getFront().rotateY()));
            if (rightBelt != null) {
                BeltGroup existingGroup = belt2GroupMap.get(rightBelt);
                groupBeltsRecursive(rightBelt, newBelts, group);
                if (existingGroup != null) {
                    // If the front belt is already grouped, merge the current group into the existing one
                    group.right(existingGroup);
                }
            }
        } else if (entity.isOnlyLeftConnected()) {
            BeltHoldingEntity leftBelt = (BeltHoldingEntity) entity.getWorld().getTileEntity(entity.getPos().offset(entity.getFront().rotateYCCW()));
            if (leftBelt != null) {
                BeltGroup existingGroup = belt2GroupMap.get(leftBelt);
                groupBeltsRecursive(leftBelt, newBelts, group);
                if (existingGroup != null) {
                    // If the front belt is already grouped, merge the current group into the existing one
                    group.right(existingGroup);
                }
            }
        }
    }

    // Merges one group into another
    private void mergeGroups(BeltGroup sourceGroup, BeltGroup targetGroup) {
        if (sourceGroup == targetGroup) return;

        for (BeltHoldingEntity entity : sourceGroup.getBelts()) {
            targetGroup.addBeltHoldingEntity(entity);
            belt2GroupMap.put(entity, targetGroup);
        }

        groups.remove(sourceGroup);
    }

    // Adds a belt to a specific group
    public void addToGroup(BeltHoldingEntity entity, BeltGroup group) {
        if (entity == null || group == null) return;

        // Remove entity from its current group if it exists
        BeltGroup currentGroup = belt2GroupMap.get(entity);
        if (currentGroup != null) {
            removeFromGroup(entity, currentGroup);
        }

        group.addBeltHoldingEntity(entity);
        belt2GroupMap.put(entity, group);
    }

    // Removes a belt from a specific group
    private void removeFromGroup(BeltHoldingEntity entity, BeltGroup group) {
        group.removeBeltHoldingEntity(entity);
        belt2GroupMap.remove(entity);
        if (group.isEmpty()) {
            groups.remove(group);
        }
    }

    // Splits a group into smaller connected groups
    private void splitGroup(BeltGroup group) {
        groups.remove(group);

        // Rebuild smaller groups from the disconnected belts
        List<BeltHoldingEntity> disconnectedBelts = new ArrayList<>(group.getBelts());
        for (BeltHoldingEntity entity : disconnectedBelts) {
            if (!belt2GroupMap.containsKey(entity)) {
                BeltGroup newGroup = new BeltGroup();
                groups.add(newGroup);
                //groupBeltsRecursive(entity, newBelts, newGroup);
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

    public class MyObject<B> {
        // Private field
        private BeltGroup value;

        // Constructor
        public MyObject() {
            this.value = value;
        }

        // Getter method
        public BeltGroup getValue() {
            return value;
        }

        // Setter method
        public void setValue(BeltGroup value) {
            this.value = value;
        }
    }
}
