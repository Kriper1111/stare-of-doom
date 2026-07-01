package kriperivi.stareofdoom.common;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import kriperivi.stareofdoom.StareOfDoom;
import kriperivi.stareofdoom.event.StareAtEntity;

public class Config implements IMessage {
    private double maxDistanceSquared;
    private int stareThreshold;
    private int stareFalloff;
    private boolean strikeLightning;
    // TODO: Synchronize the mob list too.
    private String[] mobs;
    private boolean mobsWhitelist;
    private boolean ignorePlayers;
    private boolean ignorePrivileged;

    public Config(double maxDistanceSquared, int stareThreshold, int stareFalloff, boolean strikeLightning,
                  String[] mobs,
                  boolean mobsWhitelist, boolean ignorePlayers, boolean ignorePrivileged) {
        this.maxDistanceSquared = maxDistanceSquared;
        this.stareThreshold = stareThreshold;
        this.stareFalloff = stareFalloff;
        this.strikeLightning = strikeLightning;
        this.mobs = mobs;
        this.mobsWhitelist = mobsWhitelist;
        this.ignorePlayers = ignorePlayers;
        this.ignorePrivileged = ignorePrivileged;
    }

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

    public String[] getMobs() {
        return mobs;
    }

    public boolean isMobsWhitelist() {
        return mobsWhitelist;
    }

    public boolean doIgnorePlayers() {
        return ignorePlayers;
    }

    public boolean doIgnorePrivileged() {
        return ignorePrivileged;
    }

    public Config() {
        maxDistanceSquared = 0;
        stareThreshold = 0;
        stareFalloff = 0;
        strikeLightning = false;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        maxDistanceSquared = buf.readDouble();
        stareThreshold = buf.readInt();
        stareFalloff = buf.readInt();
        strikeLightning = buf.readBoolean();
        mobsWhitelist = buf.readBoolean();
        ignorePlayers = buf.readBoolean();
        ignorePrivileged = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(maxDistanceSquared);
        buf.writeInt(stareThreshold);
        buf.writeInt(stareFalloff);
        buf.writeBoolean(strikeLightning);
        buf.writeBoolean(mobsWhitelist);
        buf.writeBoolean(ignorePlayers);
        buf.writeBoolean(ignorePrivileged);
    }

    public static class Handler implements IMessageHandler<Config, IMessage> {

        @Override
        public IMessage onMessage(Config message, MessageContext ctx) {
            StareOfDoom.LOGGER.info("Received SetupConfig message from server, updating.");
            StareAtEntity.setConfig(message);
            return null;
        }
    }
}
