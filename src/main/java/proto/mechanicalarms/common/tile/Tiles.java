package proto.mechanicalarms.common.tile;

public class Tiles
{
	public static TileArmBasic tileArmBasic;
	public static TileBeltBasic tileBeltBasic;
	public static TileSplitter tileSplitter;

	public static void init() {
		tileArmBasic = new TileArmBasic();
		tileBeltBasic = new TileBeltBasic();
		tileSplitter = new TileSplitter();
	}
}
