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

public final class DisconnectPlayHandler implements PacketHandler {

    @Override
    public void handle(MinecraftConnection.PacketData packet, ServerConnection connection) throws IOException {
        DataInputStream data = packet.getDataStream();

        int reasonLength = VarInt.readVarInt(data);
        byte[] reasonBytes = new byte[reasonLength];
        data.readFully(reasonBytes);
        String reasonJson = new String(reasonBytes, StandardCharsets.UTF_8);

        String reason = extractTextFromJson(reasonJson);

        connection.getLogger().accept("╔════════════════════════════════════════╗");
        connection.getLogger().accept("║   DISCONNECTED FROM SERVER");
        connection.getLogger().accept("║   Reason: " + reason);
        connection.getLogger().accept("╚════════════════════════════════════════╝");
        connection.disconnect();
    }

    private String extractTextFromJson(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            return extractTextRecursive(element);
        } catch (Exception e) {
            return json;
        }
    }

    private String extractTextRecursive(JsonElement element) {
        StringBuilder result = new StringBuilder();

        if (element.isJsonPrimitive()) {
            result.append(element.getAsString());
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            if (obj.has("text")) {
                result.append(obj.get("text").getAsString());
            }

            if (obj.has("extra")) {
                JsonArray extra = obj.getAsJsonArray("extra");
                for (JsonElement child : extra) {
                    result.append(extractTextRecursive(child));
                }
            }

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
