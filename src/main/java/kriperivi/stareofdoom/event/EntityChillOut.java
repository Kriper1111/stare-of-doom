package kriperivi.stareofdoom.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import kriperivi.stareofdoom.common.Config;
import kriperivi.stareofdoom.common.PardonManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingEvent;

import static kriperivi.stareofdoom.StareOfDoom.*;


public class EntityChillOut {
    private final Config serverConfig;

    public EntityChillOut() {
        serverConfig = CONFIG.getServerConfig();
    }

    @SubscribeEvent
    public void recover(LivingEvent.LivingUpdateEvent event) {
        if (!event.entityLiving.isEntityAlive())
            return;

        if (!PardonManager.INSTANCE.isSpared(event.entityLiving))
            return;

        NBTTagCompound data = event.entityLiving.getEntityData();
        if (!data.hasKey(TAG_NAME))
            return;

        int value = data.getInteger(TAG_NAME);

        if (value > 0)
            data.setInteger(TAG_NAME, value - serverConfig.getStareFalloff());
        else {
            data.removeTag(TAG_NAME);
            PardonManager.INSTANCE.doom(event.entityLiving.getEntityId());
        }
    }
}
