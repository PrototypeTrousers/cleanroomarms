package proto.mechanicalarms.common.tile;

public class Tiles
{
	public static TileArmBasic tileArmBasic;
	public static TileBeltBasic tileBeltBasic;

	public static void init() {
		tileArmBasic = new TileArmBasic();
		tileBeltBasic = new TileBeltBasic();
	}
}
