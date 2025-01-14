package proto.mechanicalarms.common.tile;

public enum Side {
    L, R;

    Side opposite() {
        return this == L ? R : L;
    }
}
