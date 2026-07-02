package kriperivi.stareofdoom.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import kriperivi.stareofdoom.common.Config;
import kriperivi.stareofdoom.common.PardonManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Random;

import static kriperivi.stareofdoom.StareOfDoom.*;

public class EntityStaredAt implements IMessage {
    protected int target;

    public EntityStaredAt() {}

    public EntityStaredAt(int networkId) {
        target = networkId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        target = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(target);
    }

    public static class Handler implements IMessageHandler<EntityStaredAt, IMessage> {
        private static final Random RANDOM = new Random();

        @Override
        public IMessage onMessage(EntityStaredAt message, MessageContext ctx) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getEntityWorld();
            Entity target = world.getEntityByID(message.target);
            if (target == null || !target.isEntityAlive())
                return null;

            Config serverConfig = CONFIG.getServerConfig();
            NBTTagCompound tag = target.getEntityData();

            int stareDuration;
            if (!tag.hasKey(TAG_NAME))
                stareDuration = 0;
            else {
                stareDuration = tag.getInteger(TAG_NAME);
                PardonManager.INSTANCE.doom(message.target);
            }

            tag.setInteger(TAG_NAME, stareDuration += 5);
            LOGGER.info("{}'s counter is at {}", target, stareDuration);
            if (stareDuration < serverConfig.getStareThreshold())
                return null;
            else if (stareDuration >= serverConfig.getStareThreshold())
                eviscerate(world, target);

            return null;
        }

        private static void eviscerate(World worldObj, Entity entity) {
            if (CONFIG.getServerConfig().doStrikeLightning()) {
                worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, entity.posX, entity.posY, entity.posZ));
            } else {
                // Spawn instant damage particle cloud, and still play the thunder sounds.
                worldObj.playAuxSFX(2002, (int)Math.round(entity.posX), (int)Math.round(entity.posY), (int)Math.round(entity.posZ), 16428);
                worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "ambient.weather.thunder", 10000.0F, 0.8F + RANDOM.nextFloat() * 0.2F);
                worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "random.explode", 1.0F, 0.5F + RANDOM.nextFloat() * 0.2F);
            }
            entity.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
        }
    }
}
