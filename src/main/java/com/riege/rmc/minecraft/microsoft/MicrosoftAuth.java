package com.riege.rmc.minecraft.microsoft;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class MicrosoftAuth {

    private static final String CLIENT_ID = "00000000402b5328";
    private static final String DEVICE_CODE_URL = "https://login.live.com/oauth20_connect.srf";
    private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL";

    private final HttpClient httpClient;
    private final Gson gson;

    public MicrosoftAuth() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public MicrosoftToken authenticate(Consumer<String> logger) throws AuthException {
        DeviceCodeResponse deviceCode = requestDeviceCode();

        logger.accept("To sign in, use a web browser to open the page " + deviceCode.verificationUri
                + " and enter the code " + deviceCode.userCode);
        logger.accept("");
        logger.accept("Waiting for authentication...");

        return pollForToken(deviceCode);
    }

    public XboxToken getXboxToken(MicrosoftToken msToken) throws AuthException {
        XblAuthResponse xblToken = authenticateXbl(msToken.accessToken());
        return authenticateXsts(xblToken.token);
    }

    private DeviceCodeResponse requestDeviceCode() throws AuthException {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", CLIENT_ID);
        params.put("scope", SCOPE);
        params.put("response_type", "device_code");

        String body = buildFormBody(params);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEVICE_CODE_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AuthException.HttpException(response.statusCode(),
                        "Device code request failed: " + response.body());
            }

            return gson.fromJson(response.body(), DeviceCodeResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Failed to request device code", e);
        }
    }

    private MicrosoftToken pollForToken(DeviceCodeResponse deviceCode) throws AuthException {
        long intervalMs = deviceCode.interval * 1000;

        while (true) {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                throw new AuthException("Polling interrupted", e);
            }

            Map<String, String> params = new HashMap<>();
            params.put("client_id", CLIENT_ID);
            params.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
            params.put("device_code", deviceCode.deviceCode);

            String body = buildFormBody(params);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    TokenResponse tokenResponse = gson.fromJson(response.body(), TokenResponse.class);
                    return new MicrosoftToken(tokenResponse.accessToken, tokenResponse.refreshToken);
                }

                String errorText = response.body();
                if (!errorText.contains("authorization_pending")) {
                    throw new AuthException.AuthenticationFailedException(errorText);
                }
            } catch (IOException | InterruptedException e) {
                throw new AuthException("Failed to poll for token", e);
            }
        }
    }

    private XblAuthResponse authenticateXbl(String accessToken) throws AuthException {
        XblAuthRequest request = new XblAuthRequest(
                new XblProperties("RPS", "user.auth.xboxlive.com", accessToken),
                "http://auth.xboxlive.com",
                "JWT"
        );

        String requestBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(XBL_AUTH_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AuthException.HttpException(response.statusCode(),
                        "XBL authentication failed: " + response.statusCode());
            }

            return gson.fromJson(response.body(), XblAuthResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Failed to authenticate with XBL", e);
        }
    }

    private XboxToken authenticateXsts(String xblToken) throws AuthException {
        XstsAuthRequest request = new XstsAuthRequest(
                new XstsProperties("RETAIL", List.of(xblToken)),
                "rp://api.minecraftservices.com/",
                "JWT"
        );

        String requestBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(XSTS_AUTH_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AuthException.HttpException(response.statusCode(),
                        "XSTS authentication failed: " + response.statusCode());
            }

            XblAuthResponse xstsResponse = gson.fromJson(response.body(), XblAuthResponse.class);
            String userHash = xstsResponse.displayClaims.xui.getFirst().uhs;

            return new XboxToken(xstsResponse.token, userHash);
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Failed to authenticate with XSTS", e);
        }
    }

    private String buildFormBody(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static class DeviceCodeResponse {
        @SerializedName("device_code")
        String deviceCode;
        @SerializedName("user_code")
        String userCode;
        @SerializedName("verification_uri")
        String verificationUri;
        @SerializedName("expires_in")
        long expiresIn;
        long interval;
    }

    private static class TokenResponse {
        @SerializedName("access_token")
        String accessToken;
        @SerializedName("refresh_token")
        String refreshToken;
    }

    private static class XblAuthRequest {
        @SerializedName("Properties")
        XblProperties properties;
        @SerializedName("RelyingParty")
        String relyingParty;
        @SerializedName("TokenType")
        String tokenType;

        XblAuthRequest(XblProperties properties, String relyingParty, String tokenType) {
            this.properties = properties;
            this.relyingParty = relyingParty;
            this.tokenType = tokenType;
        }
    }

    private static class XblProperties {
        @SerializedName("AuthMethod")
        String authMethod;
        @SerializedName("SiteName")
        String siteName;
        @SerializedName("RpsTicket")
        String rpsTicket;

        XblProperties(String authMethod, String siteName, String rpsTicket) {
            this.authMethod = authMethod;
            this.siteName = siteName;
            this.rpsTicket = rpsTicket;
        }
    }

    private static class XblAuthResponse {
        @SerializedName("Token")
        String token;
        @SerializedName("DisplayClaims")
        DisplayClaims displayClaims;
    }

    private static class DisplayClaims {
        List<XuiClaim> xui;
    }

    private static class XuiClaim {
        String uhs;
    }

    private static class XstsAuthRequest {
        @SerializedName("Properties")
        XstsProperties properties;
        @SerializedName("RelyingParty")
        String relyingParty;
        @SerializedName("TokenType")
        String tokenType;

        XstsAuthRequest(XstsProperties properties, String relyingParty, String tokenType) {
            this.properties = properties;
            this.relyingParty = relyingParty;
            this.tokenType = tokenType;
        }
    }

    private static class XstsProperties {
        @SerializedName("SandboxId")
        String sandboxId;
        @SerializedName("UserTokens")
        List<String> userTokens;

        XstsProperties(String sandboxId, List<String> userTokens) {
            this.sandboxId = sandboxId;
            this.userTokens = userTokens;
        }
    }
}
