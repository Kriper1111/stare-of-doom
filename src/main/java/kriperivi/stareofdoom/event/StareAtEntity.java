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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;

public class StareAtEntity {
    private static Config config = null;
    private static HashSet<String> mobFilterList = null;
    private final Minecraft theGame;
    private EntityLivingBase lastMatch = null;
    private int timer = 0;

    public StareAtEntity() {
        theGame = Minecraft.getMinecraft();
    }

    public static void setConfig(Config newConfig) {
        config = newConfig;
    }

    public static void setMobFilterList(HashSet<String> filterList) {
        mobFilterList = filterList;
    }

    @SubscribeEvent
    public void postClientTick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END)
            return;

        if (theGame.theWorld == null)
            return;

        if (theGame.isGamePaused())
            return;

        if (config == null || mobFilterList == null)
            return;

        if (timer != 5) {
            ++timer;
            return;
        }

        timer = 0;
        EntityLivingBase it = checkLastMatch(theGame.renderViewEntity, config.getMaxDistanceSquared());

        if (it == null) {
            it = pickPointedEntity(theGame.renderViewEntity, theGame.theWorld, config.getMaxDistanceSquared());
            lastMatch = it;
            if (it == null) {
                return;
            }
        }

        StareOfDoom.LOGGER.info("Picked entity {}", it);
        StareOfDoom.PACKET_HANDLER.sendToServer(new EntityStaredAt(it.getEntityId()));
    }

    private EntityLivingBase checkLastMatch(EntityLivingBase ref, double maxDist) {
        if (lastMatch == null)
            return null;

        if (lastMatch.isDead)
            return null;

        if (entityIsDying(lastMatch))
            return null;

        if (isEntityNotObserved(ref, lastMatch, maxDist))
            return null;

        return lastMatch;
    }

    private static boolean entityIsDying(EntityLivingBase ent) {
        return !ent.isEntityAlive();
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

        EntityLivingBase it;
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

                if (entityIsDying(it))
                    continue;

                if (!matchConfigFilters(it))
                    continue;

                if (isEntityNotObserved(ref, it, maxDist))
                    continue;

                return it;
            }
        }

        return null;
    }

    private static boolean isEntityNotObserved(EntityLivingBase ref, EntityLivingBase tar, double maxDist) {
        double diffX, diffY, diffZ;
        double dist, dot;

        Vec3 lookVector = ref.getLookVec();
        Vec3 refPos = ref.getPosition(0);
        Vec3 itsPos = tar.getPosition(0);
        diffX = itsPos.xCoord - refPos.xCoord;
        diffY = itsPos.yCoord - refPos.yCoord + tar.getEyeHeight() + ref.getEyeHeight();
        diffZ = itsPos.zCoord - refPos.zCoord;

        dist = diffX * diffX + diffY * diffY + diffZ * diffZ;
        if (dist > maxDist)
            return true;

        dist = Math.sqrt(dist);
        dot = (lookVector.xCoord * diffX + lookVector.yCoord * diffY + lookVector.zCoord * diffZ) / dist;

        if (dot < (1.0 - 0.05 / dist))
            return true;

        return !ref.canEntityBeSeen(tar);
    }

    // TODO: Do all this on server-side.
    // Figure out why MinecraftServer.getServer() is null
    // Perhaps move the player stuff onto the server?
    // Hmm...
    private static boolean matchConfigFilters(EntityLivingBase entityLiving) {
        // TODO: Handle the players somehow.
//        if (entityLiving instanceof EntityOtherPlayerMP) {
//            if (!MinecraftServer.getServer().isPVPEnabled())
//                return false;
//            if (config.doIgnorePlayers())
//                return false;
//            if (((EntityOtherPlayerMP) entityLiving).capabilities.isCreativeMode && config.doIgnorePrivileged())
//                return false;
//            if (((EntityOtherPlayerMP) entityLiving).canCommandSenderUseCommand(2, "") && config.doIgnorePrivileged())
//                return false;
//            return true;
//        }

        boolean includes = mobFilterList.contains(EntityList.getEntityString(entityLiving));

        return includes == config.isMobsWhitelist();
    }
}
