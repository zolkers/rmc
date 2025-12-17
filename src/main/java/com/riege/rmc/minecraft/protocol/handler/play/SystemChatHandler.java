package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.api.chat.ChatMessage;
import com.riege.rmc.api.chat.ChatService;
import com.riege.rmc.minecraft.nbt.NBT;
import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.DataInputStream;
import java.io.IOException;

public final class SystemChatHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();

        // Read the NBT chat component
        int nbtLength = VarInt.readVarInt(data);
        byte[] nbtBytes = new byte[nbtLength];
        data.readFully(nbtBytes);

        // Read overlay flag
        boolean overlay = data.readBoolean();

        // Parse NBT and extract text
        String displayText = NBT.extractChatText(nbtBytes);

        if (!overlay && !displayText.isEmpty()) {
            // Log to terminal
            connection.getLogger().accept("[CHAT] " + displayText);

            // Dispatch to ChatService if available
            ChatService chatService = connection.getChatService();
            if (chatService != null) {
                ChatMessage message = ChatMessage.system(displayText);
                chatService.dispatchMessage(message);
            }
        }
    }
}
