package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.api.chat.ChatMessage;
import com.riege.rmc.api.chat.ChatService;
import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class PlayerChatHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();

        long mostSigBits = data.readLong();
        long leastSigBits = data.readLong();
        UUID sender = new UUID(mostSigBits, leastSigBits);

        VarInt.readVarInt(data);

        boolean hasSignature = data.readBoolean();
        if (hasSignature) {
            byte[] signature = new byte[256];
            data.readFully(signature);
        }

        int messageLength = VarInt.readVarInt(data);
        byte[] messageBytes = new byte[messageLength];
        data.readFully(messageBytes);
        String message = new String(messageBytes, StandardCharsets.UTF_8);

        data.readLong();

        data.readLong();

        int previousMessagesCount = VarInt.readVarInt(data);
        for (int i = 0; i < previousMessagesCount; i++) {
            VarInt.readVarInt(data);
            boolean hasPrevSignature = data.readBoolean();
            if (hasPrevSignature) {
                byte[] prevSig = new byte[256];
                data.readFully(prevSig);
            }
        }

        boolean hasUnsignedContent = data.readBoolean();
        String displayMessage = message;
        if (hasUnsignedContent) {
            int nbtLength = VarInt.readVarInt(data);
            byte[] nbtBytes = new byte[nbtLength];
            data.readFully(nbtBytes);
            displayMessage = com.riege.rmc.minecraft.nbt.NBT.extractChatText(nbtBytes);
        }

        // Log to terminal
        connection.getLogger().accept("<Player> " + displayMessage);

        // Dispatch to ChatService if available
        ChatService chatService = connection.getChatService();
        if (chatService != null) {
            // For player chat, we use "Player" as the sender name since we don't have the actual username
            ChatMessage chatMessage = ChatMessage.player(displayMessage, sender, "Player");
            chatService.dispatchMessage(chatMessage);
        }
    }
}
