package kriperivi.stareofdoom.common;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import kriperivi.stareofdoom.StareOfDoom;
import kriperivi.stareofdoom.event.DetectEntitiesEvent;

public class Config implements IMessage {
    private double maxDistanceSquared;
    private int stareThreshold;
    private int stareFalloff;
    private boolean strikeLightning;

    public double getMaxDistanceSquared() {
        return maxDistanceSquared;
    }

    public int getStareThreshold() {
        return stareThreshold;
    }

    public int getStareFalloff() {
        return stareFalloff;
    }

    public boolean doStrikeLightning() {
        return strikeLightning;
    }

    public Config() {
        maxDistanceSquared = 0;
        stareThreshold = 0;
        stareFalloff = 0;
        strikeLightning = false;
    }

    public Config(double maxDistanceSquared, int stareThreshold, int stareFalloff, boolean strikeLightning) {
        this.maxDistanceSquared = maxDistanceSquared;
        this.stareThreshold = stareThreshold;
        this.stareFalloff = stareFalloff;
        this.strikeLightning = strikeLightning;
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

    public static class Handler implements IMessageHandler<Config, IMessage> {

        @Override
        public IMessage onMessage(Config message, MessageContext ctx) {
            StareOfDoom.LOGGER.info("Received SetupConfig message from server, updating.");
            DetectEntitiesEvent.setConfig(message);
            return null;
        }
    }
}
