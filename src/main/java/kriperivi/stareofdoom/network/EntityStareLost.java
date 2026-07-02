package kriperivi.stareofdoom.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import kriperivi.stareofdoom.common.PardonManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

public class EntityStareLost implements IMessage, IMessageHandler<EntityStareLost, IMessage> {
    private int target = 0;

    public EntityStareLost() {}

    public EntityStareLost(Entity entity) {
        target = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        target = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(target);
    }

    @Override
    public IMessage onMessage(EntityStareLost message, MessageContext ctx) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        Entity target = server.getEntityWorld().getEntityByID(message.target);
        if (target == null)
            return null;

        PardonManager.INSTANCE.spare(message.target);

        return null;
    }
}
