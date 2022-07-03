package se.mickelus.mutil.network;

import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class PacketHandler {
    private static final Logger logger = LogManager.getLogger();

    private final SimpleChannel channel;
    private final ArrayList<Class<? extends AbstractPacket>> packets = new ArrayList<>();
    private static int packetCounter;

    public PacketHandler(String namespace, String channelId) {
        channel = new SimpleChannel(new ResourceLocation(namespace, channelId));
        packetCounter = 0;

        channel.initServerListener();
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            channel.initClientListener();
        }
    }

    /**
     * Register your packet with the pipeline. Discriminators are automatically set.
     *
     * @param packetClass the class to register
     * @param supplier A supplier returning an object instance of packetClass
     *
     * @return whether registration was successful. Failure may occur if 256 packets have been registered or if the registry already contains this packet
     */
    public <T extends AbstractPacket> boolean registerPacket(Class<T> packetClass, Supplier<T> supplier, PacketFlow flow) {
        if (packets.size() > 256) {
            logger.warn("Attempted to register packet but packet list is full: " + packetClass.toString());
            return false;
        }

        if (packets.contains(packetClass)) {
            logger.warn("Attempted to register packet but packet is already in list: " + packetClass.toString());
            return false;
        }

        if(flow == PacketFlow.CLIENTBOUND) {
            channel.registerS2CPacket(packetClass, packetCounter, friendlyByteBuf -> {
                T packet = supplier.get();
                packet.fromBytes(friendlyByteBuf);
                return packet;
            });
        }
        else {
            channel.registerC2SPacket(packetClass, packetCounter, friendlyByteBuf -> {
                T packet = supplier.get();
                packet.fromBytes(friendlyByteBuf);
                return packet;
            });
        }

        packets.add(packetClass);
        packetCounter++;

        return true;
    }

    public SimpleChannel getChannel() {
        return channel;
    }
}