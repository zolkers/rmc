package com.riege.rmc.minecraft.protocol;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class SessionServerAuth {
    private static final String SESSION_SERVER_URL = "https://sessionserver.mojang.com/session/minecraft/join";

    public static void joinServer(String accessToken, String uuid, String serverHash) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        JsonObject payload = new JsonObject();
        payload.addProperty("accessToken", accessToken);
        payload.addProperty("selectedProfile", uuid.replace("-", ""));
        payload.addProperty("serverId", serverHash);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SESSION_SERVER_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new IOException("Session server authentication failed: " + response.statusCode() + " - " + response.body());
        }
    }
}
