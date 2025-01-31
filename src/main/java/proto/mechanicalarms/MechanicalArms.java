package proto.mechanicalarms;

import proto.mechanicalarms.common.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = MechanicalArms.MODID, dependencies = "required:forge@[14.23.5.2847,);")
public class MechanicalArms {
    public final static String MODID = "mechanicalarms";
    @Mod.Instance(MODID)

    public static MechanicalArms INSTANCE;

    @SidedProxy(modId = MODID, clientSide = "proto.mechanicalarms.common.proxy.ClientProxy", serverSide = "proto.mechanicalarms.common.proxy.ServerProxy")
    public static CommonProxy proxy;
    public static Logger logger;

    public MechanicalArms() {
    }

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit();
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        proxy.init();
    }

    @EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

}
