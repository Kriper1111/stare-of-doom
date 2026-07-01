package kriperivi.stareofdoom.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import kriperivi.stareofdoom.StareOfDoom;
import kriperivi.stareofdoom.common.Config;
import kriperivi.stareofdoom.network.EntityStaredAt;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class StareAtEntity {
    private static Config config = null;
    private final Minecraft theGame;
    private int timer = 0;

    public StareAtEntity() {
        theGame = Minecraft.getMinecraft();
    }

    public static void setConfig(Config newConfig) {
        config = newConfig;
    }

    @SubscribeEvent
    public void postClientTick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END)
            return;

        if (theGame.theWorld == null)
            return;

        if (theGame.isGamePaused())
            return;

        if (config == null)
            throw new AssertionError("Config was unexpectedly null!");

        if (timer != 5) {
            ++timer;
            return;
        }

        EntityLivingBase pointedEntity = pickPointedEntity(theGame.renderViewEntity, theGame.theWorld, config.getMaxDistanceSquared());
        if (pointedEntity == null)
            return;

        StareOfDoom.LOGGER.info("Picked entity {}", pointedEntity);
        StareOfDoom.PACKET_HANDLER.sendToServer(new EntityStaredAt(pointedEntity.getEntityId()));
        timer = 0;
    }

    private static EntityLivingBase pickPointedEntity(EntityLivingBase ref, World world, double maxDist) {
        int workCount = (int) Math.ceil(maxDist / 64) + 1;
        Vec3 position = ref.getPosition(0);
        Vec3 lookVector = ref.getLookVec();

        // TODO: edge case where maxDistance < stepDistance (8)
        // Also maybe more intelligent step heuristic?
        // Also it needs to acquire the config from the server
        // But what if our render distance is too small for that?
        // Well then we can't *see* it, duh, so we can't explode it.

        double diffX, diffY, diffZ;
        double dist, dot;

        EntityLivingBase it;
        Vec3 itsPos;
        for (int work = 1; work < workCount; work++) {
            Vec3 center = position.addVector(lookVector.xCoord * 4 * work,
                                             lookVector.yCoord * 4 * work,
                                             lookVector.zCoord * 4 * work);
            AxisAlignedBB boundingBox =
                    AxisAlignedBB.getBoundingBox(center.xCoord - 4, center.yCoord - 4, center.zCoord - 4,
                                                 center.xCoord + 4, center.yCoord + 4, center.zCoord + 4);
            //noinspection unchecked
            List<Entity> entityList = world.getEntitiesWithinAABBExcludingEntity(ref, boundingBox);
            for (Entity entity : entityList) {
                if (!(entity instanceof EntityLivingBase))
                    continue;

                if (!entity.canBeCollidedWith())
                    continue;

                it = (EntityLivingBase) entity;

                // Check: entity class (commandSenderName)
                // Check: playerInCreative constraint
                // Check: entity distance (+)

                if (!matchConfigFilters(it))
                    continue;

                itsPos = it.getPosition(0);
                diffX = itsPos.xCoord - position.xCoord;
                diffY = itsPos.yCoord - position.yCoord + it.getEyeHeight() + ref.getEyeHeight();
                diffZ = itsPos.zCoord - position.zCoord;

                dist = diffX * diffX + diffY * diffY + diffZ * diffZ;
                if (dist > maxDist)
                    continue;

                dist = Math.sqrt(dist);
                dot = (lookVector.xCoord * diffX + lookVector.yCoord * diffY + lookVector.zCoord * diffZ) / dist;

                if (dot < (1.0 - 0.05 / dist))
                    continue;

                return it;
            }
        }

        return null;
    }

    private static boolean matchConfigFilters(EntityLivingBase entityLiving) {
        // Handle the player case separately
        if (entityLiving instanceof EntityPlayerMP) {
            if (!MinecraftServer.getServer().isPVPEnabled())
                return false;
            if (config.doIgnorePlayers())
                return false;
            if (((EntityPlayerMP) entityLiving).capabilities.isCreativeMode && config.doIgnorePrivileged())
                return false;
            if (((EntityPlayerMP) entityLiving).canCommandSenderUseCommand(2, "") && config.doIgnorePrivileged())
                return false;
            return true;
        }

        String mobName = EntityList.getEntityString(entityLiving);
        boolean includes = false;

        for (String predicate : config.getMobs()) {
            if (mobName.equals(predicate)) {
                includes = true;
                break;
            }
        }

        return includes == config.isMobsWhitelist();
    }
}
