package proto.mechanicalarms.common.proxy;

import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.client.renderer.TileArmRenderer;
import proto.mechanicalarms.client.renderer.TileBeltRenderer;
import proto.mechanicalarms.common.item.Items;
import proto.mechanicalarms.common.tile.TileArmBasic;
import proto.mechanicalarms.common.tile.TileBeltBasic;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final ModelResourceLocation base = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/fullarm.glb"), "");
    public static final ModelResourceLocation belt = new ModelResourceLocation(new ResourceLocation(MechanicalArms.MODID, "models/block/belt.glb"), "");
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
        super.preInit();
    }

}
