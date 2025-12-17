package com.riege.rmc.minecraft.protocol.handler.play;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
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

        // Parse and display the message
        String displayText = extractTextFromJson(jsonMessage);

        if (!overlay) {
            connection.getLogger().accept("[CHAT] " + displayText);
        }
    }

    private String extractTextFromJson(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            return extractTextRecursive(element);
        } catch (Exception e) {
            // Fallback to raw JSON if parsing fails
            return json;
        }
    }

    private String extractTextRecursive(JsonElement element) {
        StringBuilder result = new StringBuilder();

        if (element.isJsonPrimitive()) {
            // Plain string
            result.append(element.getAsString());
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            // Extract "text" field if present
            if (obj.has("text")) {
                result.append(obj.get("text").getAsString());
            }

            // Process "extra" array if present
            if (obj.has("extra")) {
                JsonArray extra = obj.getAsJsonArray("extra");
                for (JsonElement child : extra) {
                    result.append(extractTextRecursive(child));
                }
            }

            // Process "with" array if present (for translate components)
            if (obj.has("with")) {
                JsonArray with = obj.getAsJsonArray("with");
                for (JsonElement child : with) {
                    result.append(extractTextRecursive(child));
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                result.append(extractTextRecursive(child));
            }
        }

        return result.toString();
    }
}
