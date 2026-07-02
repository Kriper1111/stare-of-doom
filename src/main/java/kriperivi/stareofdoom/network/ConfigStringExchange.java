package kriperivi.stareofdoom.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import kriperivi.stareofdoom.StareOfDoom;
import kriperivi.stareofdoom.common.Config;
import kriperivi.stareofdoom.event.StareAtEntity;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.HashSet;

public class ConfigStringExchange implements IMessage, IMessageHandler<ConfigStringExchange, IMessage> {
    HashSet<String> mobNames;

    public ConfigStringExchange() { mobNames = new HashSet<String>(); }

    public ConfigStringExchange(Config config) {
        mobNames = config.getMobFilterList();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packet = new PacketBuffer(buf);
        int size = packet.readVarIntFromBuffer();
        for (int i = 0; i < size; ++i) {
            try {
                mobNames.add(packet.readStringFromBuffer(512));
            } catch (IOException e) {
                StareOfDoom.LOGGER.error("Invalid packet: long mob name received.");
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packet = new PacketBuffer(buf);
        packet.writeVarIntToBuffer(mobNames.size());
        for (String name : mobNames) {
            try {
                packet.writeStringToBuffer(name);
            } catch (IOException e) {
                StareOfDoom.LOGGER.error("Very long mob name! That shouldn't happen. At all.");
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public IMessage onMessage(ConfigStringExchange message, MessageContext ctx) {
        StareAtEntity.setMobFilterList(message.mobNames);

        return null;
    }
}
