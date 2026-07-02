package kriperivi.stareofdoom.common;

import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.IntHashMap;

public class PardonManager {
    public static final PardonManager INSTANCE = new PardonManager();
    private final IntHashMap pardons = new IntHashMap();

    public void spare(int entity) {
        pardons.addKey(entity, null);
    }

    public void doom(int entity) {
        pardons.removeObject(entity);
    }

    public boolean isSpared(Entity entity) {
        return pardons.containsItem(entity.getEntityId());
    }

    public void onInternalServerShutdown(FMLServerStoppedEvent event) {
        pardons.clearMap();
    }
}
