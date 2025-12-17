package com.riege.rmc.minecraft.protocol.handler.play;

import com.riege.rmc.minecraft.protocol.MinecraftConnection;
import com.riege.rmc.minecraft.protocol.ServerConnection;
import com.riege.rmc.minecraft.protocol.VarInt;
import com.riege.rmc.minecraft.protocol.handler.PacketHandler;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class SystemChatHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();

        // Read the JSON chat component as a string
        int jsonLength = VarInt.readVarInt(data);
        byte[] jsonBytes = new byte[jsonLength];
        data.readFully(jsonBytes);
        String jsonMessage = new String(jsonBytes, StandardCharsets.UTF_8);

        // Read overlay flag
        boolean overlay = data.readBoolean();

        // Parse and display the message (simple text extraction from JSON)
        String displayText = extractTextFromJson(jsonMessage);

        if (!overlay) {
            connection.getLogger().accept("[CHAT] " + displayText);
        }
    }

    private String extractTextFromJson(String json) {
        // Simple text extraction - handles basic {"text":"message"} format
        // For full support, would need proper JSON parser
        try {
            if (json.contains("\"text\"")) {
                int start = json.indexOf("\"text\":\"") + 8;
                int end = json.indexOf("\"", start);
                if (start > 7 && end > start) {
                    return json.substring(start, end);
                }
            }
            // If it's just a plain string
            if (json.startsWith("\"") && json.endsWith("\"")) {
                return json.substring(1, json.length() - 1);
            }
            return json;
        } catch (Exception e) {
            return json;
        }
    }
}
