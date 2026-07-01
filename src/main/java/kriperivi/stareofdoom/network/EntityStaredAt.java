package kriperivi.stareofdoom.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Random;

import static kriperivi.stareofdoom.StareOfDoom.TAG_NAME;
import static kriperivi.stareofdoom.StareOfDoom.CONFIG;

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
            if (target == null || target.isDead)
                return null;

            NBTTagCompound tag = target.getEntityData();

            int stareDuration;
            if (!tag.hasKey(TAG_NAME))
                stareDuration = 0;
            else
                stareDuration = tag.getInteger(TAG_NAME);

            tag.setInteger(TAG_NAME, ++stareDuration);
            if (stareDuration < 2)
                return null;
            else if (stareDuration == 2)
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
