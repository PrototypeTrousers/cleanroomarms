package proto.mechanicalarms.common.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
import proto.mechanicalarms.common.item.Items;
import proto.mechanicalarms.common.tile.TileArmBasic;
import proto.mechanicalarms.common.tile.TileBeltBasic;

import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final ModelResourceLocation base = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/fullarm.glb"), "");
    public static final ModelResourceLocation belt = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/belt.glb"), "");
    public static final ModelResourceLocation beltSlope = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/beltslope.glb"), "");
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Items.ARM_BASE, 0, new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/completearm.obj"), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Items.BELT_BASE, 0, new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/belt.obj"), "inventory"));
    }

    @Override
    public void preInit() {
        OBJLoader.INSTANCE.addDomain(MechanicalArms.MODID);
        ClientRegistry.bindTileEntitySpecialRenderer(TileArmBasic.class, new TileArmRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileBeltBasic.class, new TileBeltRenderer());
        MinecraftForge.EVENT_BUS.register(Tick.INSTANCE);
        super.preInit();
    }

    @SubscribeEvent
    public static void onDebugOverlay(RenderGameOverlayEvent.Text event)
    {
        BlockPos blockpos = new BlockPos(Minecraft.getMinecraft().getRenderViewEntity().posX,
                Minecraft.getMinecraft().getRenderViewEntity().getEntityBoundingBox().minY,
                Minecraft.getMinecraft().getRenderViewEntity().posZ);

        World w = Minecraft.getMinecraft().getIntegratedServer().getEntityWorld();
        int tickIdx = 0;
        TileEntity te = Minecraft.getMinecraft().getIntegratedServer().getEntityWorld().getTileEntity(blockpos);

        List<TileEntity> tickableTileEntities = w.tickableTileEntities;
        for (int i = 0; i < tickableTileEntities.size(); i++) {
            TileEntity t = tickableTileEntities.get(i);
            if (t == te) {
                tickIdx = i;
            }
        }

        event.getRight().add(String.valueOf(tickIdx));
    }

}
