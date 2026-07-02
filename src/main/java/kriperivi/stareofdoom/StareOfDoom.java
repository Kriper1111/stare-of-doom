package kriperivi.stareofdoom;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import kriperivi.stareofdoom.common.Config;
import kriperivi.stareofdoom.common.ConfigManager;
import kriperivi.stareofdoom.common.DropListener;
import kriperivi.stareofdoom.common.PardonManager;
import kriperivi.stareofdoom.event.EntityChillOut;
import kriperivi.stareofdoom.network.ConfigStringExchange;
import kriperivi.stareofdoom.network.EntityStareLost;
import kriperivi.stareofdoom.network.EntityStaredAt;
import kriperivi.stareofdoom.proxy.Proxy;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;


@Mod(modid = StareOfDoom.MOD_ID, version = StareOfDoom.VERSION)
public class StareOfDoom {
    public static final String MOD_ID = "stare-of-doom";
    public static final String VERSION = "${version}";
    public static final String TAG_NAME = "sod_doom_timer";
    public static final SimpleNetworkWrapper PACKET_HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

    public static Logger LOGGER;
    public static ConfigManager CONFIG;

    @SidedProxy(clientSide = "kriperivi.stareofdoom.proxy.ClientProxy", serverSide = "kriperivi.stareofdoom.proxy.Proxy")
    public static Proxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        LOGGER.debug("Hello from Stare of DOOM!!");
        CONFIG = new ConfigManager(event);
        proxy.preInit(event);
        MinecraftForge.EVENT_BUS.register(new DropListener());
        MinecraftForge.EVENT_BUS.register(new EntityChillOut());
        FMLCommonHandler.instance().bus().register(CONFIG);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PACKET_HANDLER.registerMessage(EntityStaredAt.class, EntityStaredAt.class, 0x00, Side.SERVER);
        PACKET_HANDLER.registerMessage(EntityStareLost.class, EntityStareLost.class, 0x01, Side.SERVER);

        PACKET_HANDLER.registerMessage(Config.class, Config.class, 0x10, Side.CLIENT);
        PACKET_HANDLER.registerMessage(ConfigStringExchange.class, ConfigStringExchange.class, 0x11, Side.CLIENT);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        CONFIG.verifyEntityList();
    }

    @Mod.EventHandler
    public void serverShutdown(FMLServerStoppedEvent event) {
        PardonManager.INSTANCE.onInternalServerShutdown(event);
    }
}
