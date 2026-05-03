package kriperivi.stareofdoom;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import kriperivi.stareofdoom.common.Config;
import kriperivi.stareofdoom.common.DropListener;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;


@Mod(modid = StareOfDoom.MOD_ID, version = StareOfDoom.VERSION)
public class StareOfDoom {
    public static final String MOD_ID = "stare-of-doom";
    public static final String VERSION = "${version}";
    public static final String TAG_NAME = "sod_doom_timer";
    public static Logger LOGGER;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        LOGGER.debug("Hello from Stare of DOOM!!");
        Config.init(event);
        MinecraftForge.EVENT_BUS.register(new DropListener());
    }
}
