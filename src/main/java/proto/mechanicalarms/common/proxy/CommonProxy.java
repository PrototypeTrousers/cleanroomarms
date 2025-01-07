package proto.mechanicalarms.common.proxy;

import net.minecraftforge.common.capabilities.CapabilityManager;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.common.block.Blocks;
import proto.mechanicalarms.common.cap.CapabilityDualSidedHandler;
import proto.mechanicalarms.common.item.Items;
import proto.mechanicalarms.common.tile.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class CommonProxy
{
	@SubscribeEvent
	public static void registerBlocks( RegistryEvent.Register<Block> event )
	{
		event.getRegistry().register( Blocks.ARM_BASE );
		event.getRegistry().register( Blocks.BELT_BASE );
		event.getRegistry().register( Blocks.SPLITTER );
	}

	@SubscribeEvent
	public static void registerItems( RegistryEvent.Register<Item> event )
	{
		event.getRegistry().register( Items.ARM_BASE );
		event.getRegistry().register( Items.BELT_BASE );
		event.getRegistry().register( Items.SPLITTER );
	}

	@SubscribeEvent
	public static void onRegisterEntities(RegistryEvent.Register<EntityEntry> event)
	{
		GameRegistry.registerTileEntity( TileArmBasic.class, new ResourceLocation( MechanicalArms.MODID, "tilearm") );
		GameRegistry.registerTileEntity( TileBeltBasic.class, new ResourceLocation( MechanicalArms.MODID, "tilebelt") );
		GameRegistry.registerTileEntity( TileSplitter.class, new ResourceLocation( MechanicalArms.MODID, "tilesplitter") );
		GameRegistry.registerTileEntity( TileSplitterDummy.class, new ResourceLocation( MechanicalArms.MODID, "tilesplitterdummy") );
	}

	public void preInit()
	{
		Blocks.init();
		Items.init();
		Tiles.init();
		CapabilityDualSidedHandler.register();
	}

	public void init()
	{
	}

	public void postInit()
	{
	}
}

