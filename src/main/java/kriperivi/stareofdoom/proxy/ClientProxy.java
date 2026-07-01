package kriperivi.stareofdoom.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import kriperivi.stareofdoom.event.StareAtEntity;

public class ClientProxy extends Proxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new StareAtEntity());
    }
}
