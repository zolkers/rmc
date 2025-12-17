package com.riege.rmc.minecraft.protocol.handler.play;

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

        // Read sender UUID
        long mostSigBits = data.readLong();
        long leastSigBits = data.readLong();
        UUID sender = new UUID(mostSigBits, leastSigBits);

        // Read message index
        VarInt.readVarInt(data);

        // Read message signature (optional)
        boolean hasSignature = data.readBoolean();
        if (hasSignature) {
            byte[] signature = new byte[256];
            data.readFully(signature);
        }

        // Read message body
        int messageLength = VarInt.readVarInt(data);
        byte[] messageBytes = new byte[messageLength];
        data.readFully(messageBytes);
        String message = new String(messageBytes, StandardCharsets.UTF_8);

        // Read timestamp
        data.readLong();

        // Read salt
        data.readLong();

        // Read previous messages count
        int previousMessagesCount = VarInt.readVarInt(data);
        for (int i = 0; i < previousMessagesCount; i++) {
            VarInt.readVarInt(data); // message ID
            boolean hasPrevSignature = data.readBoolean();
            if (hasPrevSignature) {
                byte[] prevSig = new byte[256];
                data.readFully(prevSig);
            }
        }

        // Read unsigned content (optional - chat component)
        boolean hasUnsignedContent = data.readBoolean();
        String displayMessage = message;
        if (hasUnsignedContent) {
            int jsonLength = VarInt.readVarInt(data);
            byte[] jsonBytes = new byte[jsonLength];
            data.readFully(jsonBytes);
            String jsonMessage = new String(jsonBytes, StandardCharsets.UTF_8);
            displayMessage = extractTextFromJson(jsonMessage);
        }

        // Display the chat message
        connection.getLogger().accept("<Player> " + displayMessage);
    }

    private String extractTextFromJson(String json) {
        try {
            com.google.gson.JsonElement element = com.google.gson.JsonParser.parseString(json);
            return extractTextRecursive(element);
        } catch (Exception e) {
            return json;
        }
    }

    private String extractTextRecursive(com.google.gson.JsonElement element) {
        StringBuilder result = new StringBuilder();

        if (element.isJsonPrimitive()) {
            result.append(element.getAsString());
        } else if (element.isJsonObject()) {
            com.google.gson.JsonObject obj = element.getAsJsonObject();

            if (obj.has("text")) {
                result.append(obj.get("text").getAsString());
            }

            if (obj.has("extra")) {
                com.google.gson.JsonArray extra = obj.getAsJsonArray("extra");
                for (com.google.gson.JsonElement child : extra) {
                    result.append(extractTextRecursive(child));
                }
            }

            if (obj.has("with")) {
                com.google.gson.JsonArray with = obj.getAsJsonArray("with");
                for (com.google.gson.JsonElement child : with) {
                    result.append(extractTextRecursive(child));
                }
            }
        } else if (element.isJsonArray()) {
            com.google.gson.JsonArray array = element.getAsJsonArray();
            for (com.google.gson.JsonElement child : array) {
                result.append(extractTextRecursive(child));
            }
        }

        return result.toString();
    }
}
