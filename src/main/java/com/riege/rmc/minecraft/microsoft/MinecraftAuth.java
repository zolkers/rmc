package com.riege.rmc.minecraft.microsoft;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class MinecraftAuth {

    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private final HttpClient httpClient;
    private final Gson gson;
    private final MicrosoftAuth microsoftAuth;

    public MinecraftAuth() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
        this.microsoftAuth = new MicrosoftAuth();
    }

    public AuthenticatedProfile authenticateWithMicrosoft(MicrosoftToken msToken) throws AuthException {
        XboxToken xboxToken = microsoftAuth.getXboxToken(msToken);
        MinecraftLoginResponse mcToken = loginWithXbox(xboxToken);
        MinecraftProfile profile = getProfile(mcToken.accessToken);

        UUID uuid = parseUuid(profile.id);
        Instant expiresAt = Instant.now().plusSeconds(mcToken.expiresIn);

        return new AuthenticatedProfile(uuid, profile.name, mcToken.accessToken, expiresAt);
    }

    private MinecraftLoginResponse loginWithXbox(XboxToken xboxToken) throws AuthException {
        String identityToken = String.format("XBL3.0 x=%s;%s", xboxToken.userHash(), xboxToken.token());

        MinecraftLoginRequest request = new MinecraftLoginRequest(identityToken);
        String requestBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(MC_LOGIN_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AuthException.HttpException(response.statusCode(),
                        "Minecraft login failed: " + response.statusCode());
            }

            return gson.fromJson(response.body(), MinecraftLoginResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Failed to login with Xbox", e);
        }
    }

    private MinecraftProfile getProfile(String accessToken) throws AuthException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(MC_PROFILE_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AuthException.HttpException(response.statusCode(),
                        "Profile fetch failed: " + response.statusCode());
            }

            return gson.fromJson(response.body(), MinecraftProfile.class);
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Failed to get profile", e);
        }
    }

    private UUID parseUuid(String uuidStr) throws AuthException {
        try {
            if (uuidStr.length() == 32) {
                String formatted = String.format("%s-%s-%s-%s-%s",
                        uuidStr.substring(0, 8),
                        uuidStr.substring(8, 12),
                        uuidStr.substring(12, 16),
                        uuidStr.substring(16, 20),
                        uuidStr.substring(20, 32)
                );
                return UUID.fromString(formatted);
            } else {
                return UUID.fromString(uuidStr);
            }
        } catch (IllegalArgumentException e) {
            throw new AuthException.InvalidResponseException("Invalid UUID: " + uuidStr, e);
        }
    }

    // Internal DTOs
    private static class MinecraftLoginRequest {
        @SerializedName("identityToken")
        String identityToken;

        MinecraftLoginRequest(String identityToken) {
            this.identityToken = identityToken;
        }
    }

    private static class MinecraftLoginResponse {
        @SerializedName("access_token")
        String accessToken;
        @SerializedName("expires_in")
        long expiresIn;
    }

    private static class MinecraftProfile {
        String id;
        String name;
    }
}
