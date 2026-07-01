package kriperivi.stareofdoom.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import kriperivi.stareofdoom.StareOfDoom;
import kriperivi.stareofdoom.common.Config;
import kriperivi.stareofdoom.event.DetectEntitiesEvent;

public class ConfigHello implements IMessage {
    public double maxDistanceSquared;
    public int stareThreshold;
    public int stareFalloff;
    public boolean strikeLightning;

    /// Default method for network protocol
    public ConfigHello() { }

    public ConfigHello(Config config) {
        maxDistanceSquared = config.getMaxDistanceSquared();
        stareThreshold = config.getStareThreshold();
        stareFalloff = config.getStareFalloff();
        strikeLightning = config.doStrikeLightning();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        maxDistanceSquared = buf.readDouble();
        stareThreshold = buf.readInt();
        stareFalloff = buf.readInt();
        strikeLightning = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(maxDistanceSquared);
        buf.writeInt(stareThreshold);
        buf.writeInt(stareFalloff);
        buf.writeBoolean(strikeLightning);
    }

    public static class Handler implements IMessageHandler<ConfigHello, IMessage> {
        @Override
        public IMessage onMessage(ConfigHello message, MessageContext ctx) {
            StareOfDoom.LOGGER.info("Received SetupConfig message from server, updating.");
            DetectEntitiesEvent.setConfig(new Config(
                    message.maxDistanceSquared,
                    message.stareThreshold,
                    message.stareFalloff,
                    message.strikeLightning
            ));
            return null;
        }
    }
}