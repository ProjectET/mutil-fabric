package se.mickelus.mutil.network;

import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;


/**
 * AbstractPacket class. Should be the parent of all packets wishing to use the PacketHandler.
 * @author sirgingalot, mickelus
 */
public abstract class AbstractPacket implements C2SPacket, S2CPacket {

    @Override
    public void encode(FriendlyByteBuf buf) {
        toBytes(buf);
    }

    @Override
    public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            handle(client.player);
        });
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        server.execute(() -> {
            handle(player);
        });
    }

    public abstract void handle(Player player);

    /**
     * Encode the packet data into the ByteBuf stream. Complex data sets may need specific data handlers (See @link{cpw.mods.fml.common.network.ByteBuffUtils})
     *
     * @param buffer the buffer to encode into
     */
    public abstract void toBytes(FriendlyByteBuf buffer);

    /**
     * Decode the packet data from the ByteBuf stream. Complex data sets may need specific data handlers (See @link{cpw.mods.fml.common.network.ByteBuffUtils})
     *
     * @param buffer the buffer to decode from
     */
    public abstract void fromBytes(FriendlyByteBuf buffer);

    /**
     * Utility method that reads a string from a buffer object.
     * @param buffer The buffer containing the string to be read.
     * @return A string read from the buffer
     * @throws IOException
     */
    protected static String readString(FriendlyByteBuf buffer) throws IOException {
        String string = "";
        char c = buffer.readChar();

        while(c != '\0') {
            string += c;
            c = buffer.readChar();
        }

        return string;
    }

    protected static void writeString(String string, FriendlyByteBuf buffer) throws IOException {
        for (int i = 0; i < string.length(); i++) {
            buffer.writeChar(string.charAt(i));
        }
        buffer.writeChar('\0');
    }
}