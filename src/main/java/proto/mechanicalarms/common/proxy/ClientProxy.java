package proto.mechanicalarms.common.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.client.events.Tick;
import proto.mechanicalarms.client.renderer.TileArmRenderer;
import proto.mechanicalarms.client.renderer.TileBeltRenderer;
import proto.mechanicalarms.client.renderer.TileSplitterRender;
import proto.mechanicalarms.common.block.BlockBelt;
import proto.mechanicalarms.common.block.Blocks;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.item.Items;
import proto.mechanicalarms.common.tile.TileArmBasic;
import proto.mechanicalarms.common.tile.TileBeltBasic;
import proto.mechanicalarms.common.tile.TileSplitter;

import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final ModelResourceLocation base = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/fullarm.glb"), "");
    public static final ModelResourceLocation belt = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/belt.glb"), "");
    public static final ModelResourceLocation beltSlope = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/beltslope.glb"), "");
    public static final ModelResourceLocation splitter = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/splitter.glb"), "");

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Items.ARM_BASE, 0, new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/completearm.obj"), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Items.BELT_BASE, 0, new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/belt.obj"), "inventory"));
        ModelLoader.setCustomStateMapper(Blocks.BELT_BASE,new StateMap.Builder().ignore(BlockBelt.FACING).build() );
    }

    @SubscribeEvent
    public static void onDebugOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();

        World w = Minecraft.getMinecraft().getIntegratedServer().getEntityWorld();
        int tickIdx = 0;

        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
            BlockPos blockpos1 = mc.objectMouseOver.getBlockPos();
            TileEntity te = Minecraft.getMinecraft().getIntegratedServer().getEntityWorld().getTileEntity(blockpos1);
            List<TileEntity> tickableTileEntities = w.tickableTileEntities;
            for (int i = 0; i < tickableTileEntities.size(); i++) {
                TileEntity t = tickableTileEntities.get(i);
                if (t == te) {
                    tickIdx = i;
                    event.getRight().add("Tile Ticking Index: " + tickIdx);
                    break;
                }
            }
        }
    }

    @Override
    public void preInit() {
        OBJLoader.INSTANCE.addDomain(MechanicalArms.MODID);
        ClientRegistry.bindTileEntitySpecialRenderer(TileArmBasic.class, new TileArmRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileBeltBasic.class, new TileBeltRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileSplitter.class, new TileSplitterRender());
        MinecraftForge.EVENT_BUS.register(Tick.INSTANCE);
        super.preInit();
    }

}
