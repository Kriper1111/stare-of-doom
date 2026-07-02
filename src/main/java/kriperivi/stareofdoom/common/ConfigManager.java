package kriperivi.stareofdoom.common;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import kriperivi.stareofdoom.StareOfDoom;
import kriperivi.stareofdoom.event.StareAtEntity;
import kriperivi.stareofdoom.network.ConfigStringExchange;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;


import static kriperivi.stareofdoom.StareOfDoom.LOGGER;

public class ConfigManager {
    private final Config serverConfig;

    public ConfigManager(FMLPreInitializationEvent evt) {
        Configuration configFile = new Configuration(evt.getSuggestedConfigurationFile());
        configFile.load();

        double maxDistanceSquared;
        int stareThreshold;
        int stareFalloff;
        boolean strikeLightning;
        boolean mobsWhitelist;
        boolean ignorePlayers;
        boolean ignorePrivileged;
        String[] mobs;

        float distance = configFile.getFloat("maxDistance", "doom",
                                             32.0f, 0, 64.0f,
                                             "Maximum distance between you and the mob beyond which it's no longer doomed.");
        float stareTime = configFile.getFloat("stareTime", "doom", 5.0f, 0.2f, 60.0f,
                                              "How long do you have to stare at the skeleton until it's eviscerated.");
        float stareCooldown = configFile.getFloat("stareCooldown", "doom", 0.2f, -1.0f, 60.0f,
                                                  "How fast should the doom counter tick down if you're not looking at it.");
        strikeLightning = configFile.getBoolean("castLightning", "doom", true,
                                                "If the mob is eviscerated, should it get struck by lightning or disappear in smoke?");

        mobs = configFile.getStringList("mobs", "doom", new String[]{"Skeleton"}, "A list of mobs to be subjected to the Stare.");
        mobsWhitelist = configFile.getBoolean("mobsWhitelist", "doom", true, "If true, 'mobs' is an inclusive list, exclusive otherwise.");
//        ignorePlayers = configFile.getBoolean("ignorePlayers", "doom", true, "Exclude any player from the effects of the Stare.");
//        ignorePrivileged = configFile.getBoolean("ignorePrivileged", "doom", true, "Exclude OPs and Creative mode players from the Stare.");

        maxDistanceSquared = distance * distance;
        stareThreshold = (int) (stareTime * 20);
        if (stareCooldown < 0.0) {
            stareFalloff = stareThreshold;
        } else {
            stareFalloff = (int) (stareCooldown * 20);
        }

        serverConfig = new Config(
                maxDistanceSquared,
                stareThreshold,
                stareFalloff,
                strikeLightning,
                mobs,
                mobsWhitelist,
                true,
                true);

        configFile.save();
        LOGGER.info("Successfully read and reloaded Stare of DOOM's config.");
    }

    public Config getServerConfig() {
        return serverConfig;
    }

    public static float getSpreadCorrection() {
        return 0.05f;
    }

    public static int getAUXSfxIdentifier() {
        return 2002;
    }

    public static int getAUXSfxValue() {
        return 16428;
    }

    @SubscribeEvent
    // Server-side. Send welcome messages to the player, stating our connection.
    public void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP))
            throw new AssertionError("Logged in player was not EntityPlayerMP!");

        MinecraftServer server = MinecraftServer.getServer();
        EntityPlayerMP player = (EntityPlayerMP) event.player;

        String playerName = player.getGameProfile().getName();
        String ownerName = server.getServerOwner();

        if (server.isSinglePlayer() && playerName.equals(ownerName)) {
            StareAtEntity.setConfig(serverConfig);
            StareAtEntity.setMobFilterList(serverConfig.getMobFilterList());
        }
        else {
            StareOfDoom.PACKET_HANDLER.sendTo(serverConfig, player);
            StareOfDoom.PACKET_HANDLER.sendTo(new ConfigStringExchange(serverConfig), player);
        }
    }

    @SubscribeEvent
    public void playerLeft(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        StareAtEntity.setConfig(null);
        StareAtEntity.setMobFilterList(null);
    }

    public void verifyEntityList() {
        serverConfig.buildEntityHash();
    }
}
