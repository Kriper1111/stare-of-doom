package kriperivi.stareofdoom.common;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import kriperivi.stareofdoom.StareOfDoom;
import kriperivi.stareofdoom.event.StareAtEntity;
import net.minecraft.entity.EntityList;

import java.util.HashSet;

public class Config implements IMessage {
    private double maxDistanceSquared = 0;
    private int stareThreshold = 0;
    private int stareFalloff = 0;
    private boolean strikeLightning = false;
    // TODO: Synchronize the mob list too.
    private String[] mobs = null;
    private HashSet<String> mobFilterList = new HashSet<String>();
    private boolean mobsWhitelist = true;
    private boolean ignorePlayers = true;
    private boolean ignorePrivileged = true;

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

    public HashSet<String> getMobFilterList() {
        return mobFilterList;
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

    public Config() { }

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

    protected void buildEntityHash() {
        for (String mobName : mobs) {
            Object entity = EntityList.stringToClassMapping.get(mobName);
            if (entity == null) {
                StareOfDoom.LOGGER.warn("Config -> entity '{}' is not found. Skipping it.", mobName);
                continue;
            }

            if (mobName.length() > 512) {
                StareOfDoom.LOGGER.warn("Long mob name detected! Are you sure what you're doing is worth it?");
                StareOfDoom.LOGGER.warn("The culprit is '{}'. Skipping.", mobName);
                continue;
            }
            mobFilterList.add(mobName);
        }
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
