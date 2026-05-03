package kriperivi.stareofdoom.common;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import static kriperivi.stareofdoom.StareOfDoom.TAG_NAME;

public class DropListener {
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent evt) {
        if (evt.source != DamageSource.outOfWorld) {
            return;
        }

        if (evt.entityLiving.getEntityData().hasKey(TAG_NAME)) {
            evt.setCanceled(true);
        }
    }
}
