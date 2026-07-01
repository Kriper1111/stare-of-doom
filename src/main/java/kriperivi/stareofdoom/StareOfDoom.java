package kriperivi.stareofdoom;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import kriperivi.stareofdoom.common.Config;
import kriperivi.stareofdoom.common.DropListener;
import kriperivi.stareofdoom.event.DetectEntitiesEvent;
import kriperivi.stareofdoom.network.EntityStaredAt;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;


@Mod(modid = StareOfDoom.MOD_ID, version = StareOfDoom.VERSION)
public class StareOfDoom {
    public static final String MOD_ID = "stare-of-doom";
    public static final String VERSION = "${version}";
    public static final String TAG_NAME = "sod_doom_timer";
    public static Logger LOGGER;
    public static final SimpleNetworkWrapper PACKET_HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        LOGGER.debug("Hello from Stare of DOOM!!");
        Config.init(event);
        MinecraftForge.EVENT_BUS.register(new DropListener());
        FMLCommonHandler.instance().bus().register(new DetectEntitiesEvent());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.debug("Stare of Doom -> Init");
        PACKET_HANDLER.registerMessage(EntityStaredAt.Handler.class, EntityStaredAt.class, 0, Side.SERVER);
    }
}
