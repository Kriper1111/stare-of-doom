package kriperivi.stareofdoom.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import kriperivi.stareofdoom.StareOfDoom;
import kriperivi.stareofdoom.common.ConfigManager;
import kriperivi.stareofdoom.network.ConfigHello;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class ConfigManagement {
    @SubscribeEvent
    // Server-side. Send welcome messages to the player, stating our connection.
    public void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP))
            throw new AssertionError("Logged in player was not EntityPlayerMP!");

        MinecraftServer server = MinecraftServer.getServer();
        EntityPlayerMP player = (EntityPlayerMP) event.player;

        String playerName = player.getGameProfile().getName();
        String ownerName = server.getServerOwner();

        if (server.isSinglePlayer() && playerName.equals(ownerName))
            DetectEntitiesEvent.setConfig(ConfigManager.getServerConfig());
        else
            StareOfDoom.PACKET_HANDLER.sendTo(new ConfigHello(ConfigManager.getServerConfig()), player);
    }

    @SubscribeEvent
    public void playerLeft(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        DetectEntitiesEvent.setConfig(null);
    }
}
