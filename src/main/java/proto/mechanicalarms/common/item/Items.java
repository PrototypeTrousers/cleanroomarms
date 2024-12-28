package proto.mechanicalarms.common.item;

import proto.mechanicalarms.common.block.Blocks;

public class Items {
    public static ItemArm ARM_BASE;
    public static ItemBelt BELT_BASE;
    public static ItemSplitter SPLITTER;

    public static void init() {
        ARM_BASE = new ItemArm(Blocks.ARM_BASE);
        BELT_BASE = new ItemBelt(Blocks.BELT_BASE);
        SPLITTER = new ItemSplitter(Blocks.SPLITTER);
    }
}
