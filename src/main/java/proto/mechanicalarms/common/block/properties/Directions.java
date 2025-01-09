package proto.mechanicalarms.common.block.properties;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public enum Directions implements IStringSerializable {

    UP_NORTH(EnumFacing.NORTH, RelativeHeight.ABOVE),
    UP_EAST(EnumFacing.EAST, RelativeHeight.ABOVE),
    UP_SOUTH(EnumFacing.SOUTH, RelativeHeight.ABOVE),
    UP_WEST(EnumFacing.WEST, RelativeHeight.ABOVE),
    NORTH(EnumFacing.NORTH, RelativeHeight.LEVEL),
    EAST(EnumFacing.EAST, RelativeHeight.LEVEL),
    SOUTH(EnumFacing.SOUTH, RelativeHeight.LEVEL),
    WEST(EnumFacing.WEST, RelativeHeight.LEVEL),
    DOWN_NORTH(EnumFacing.NORTH, RelativeHeight.BELOW),
    DOWN_EAST(EnumFacing.EAST, RelativeHeight.BELOW),
    DOWN_SOUTH(EnumFacing.SOUTH, RelativeHeight.BELOW),
    DOWN_WEST(EnumFacing.WEST, RelativeHeight.BELOW);

    private final RelativeHeight relativeHeight;
    private final EnumFacing horizontalFacing;

    public static final Directions[] VALUES = Directions.values();

    Directions(EnumFacing horizontalFacing, RelativeHeight relativeHeight) {
        this.horizontalFacing = horizontalFacing;
        this.relativeHeight = relativeHeight;
    }

    public RelativeHeight getRelativeHeight() {
        return relativeHeight;
    }

    public EnumFacing getHorizontalFacing() {
        return horizontalFacing;
    }

    public static Directions getFromHorizontalFacing(EnumFacing facing) {
        if (facing == EnumFacing.NORTH) {
            return NORTH;
        }
        if (facing == EnumFacing.EAST) {
            return EAST;
        }
        if (facing == EnumFacing.SOUTH) {
            return SOUTH;
        } else {
            return WEST;
        }
    }

    public static Directions getFromFacingAndLevel(EnumFacing facing, RelativeHeight relativeHeight) {
        int idx = 0;
        if (relativeHeight ==RelativeHeight.LEVEL) {
            idx +=4;
        } else if (relativeHeight == RelativeHeight.BELOW) {
            idx +=8;
        }

        if (facing == EnumFacing.EAST) {
            idx += 1;
        }
        else if (facing == EnumFacing.SOUTH) {
            idx += 2;
        } else if (facing == EnumFacing.WEST){
            idx += 3;
        }
        return VALUES[idx];
    }

    @NotNull
    @Override
    public String getName() {
        return this.toString().toLowerCase();
    }

    public static enum RelativeHeight {
        ABOVE,
        LEVEL,
        BELOW;
    }
}
