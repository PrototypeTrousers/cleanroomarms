package proto.mechanicalarms.common.block;

import net.minecraft.block.Block;

public class Blocks
{
	public static Block ARM_BASE = null;
	public static Block BELT_BASE = null;

	public static void init() {
		ARM_BASE = new BlockArm();
		BELT_BASE = new BlockBelt();
	}

}
