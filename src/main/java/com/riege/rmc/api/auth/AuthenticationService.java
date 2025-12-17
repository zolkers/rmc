package com.riege.rmc.api.auth;

import com.riege.rmc.api.session.SessionService;
import com.riege.rmc.minecraft.microsoft.AuthException;
import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
import com.riege.rmc.minecraft.microsoft.MinecraftAuth;
import com.riege.rmc.minecraft.microsoft.MicrosoftAuth;
import com.riege.rmc.minecraft.microsoft.MicrosoftToken;

import java.util.function.Consumer;

/**
 * Service for handling Microsoft and Minecraft authentication.
 * Provides high-level authentication operations with proper error handling.
 */
public class AuthenticationService {
    private final SessionService sessionService;
    private final MicrosoftAuth microsoftAuth;
    private final MinecraftAuth minecraftAuth;

    public AuthenticationService(SessionService sessionService) {
        this.sessionService = sessionService;
        this.microsoftAuth = new MicrosoftAuth();
        this.minecraftAuth = new MinecraftAuth();
    }

    /**
     * Perform full authentication flow (Microsoft + Minecraft).
     *
     * @param statusCallback callback for status updates during authentication
     * @return authentication result
     */
    public AuthenticationResult authenticate(Consumer<String> statusCallback) {
        // Check if already authenticated
        if (sessionService.isAuthenticated()) {
            return new AuthenticationResult.AlreadyAuthenticated(
                sessionService.getCurrentProfile().orElseThrow()
            );
        }

        try {
            statusCallback.accept("Starting Microsoft authentication...");
            MicrosoftToken msToken = microsoftAuth.authenticate(statusCallback);

            statusCallback.accept("Authenticating with Minecraft services...");
            AuthenticatedProfile profile = minecraftAuth.authenticateWithMicrosoft(msToken);

            sessionService.setProfile(profile);

            return new AuthenticationResult.Success(profile);

        } catch (AuthException e) {
            return new AuthenticationResult.Failure("Authentication failed: " + e.getMessage(), e);
        } catch (Exception e) {
            return new AuthenticationResult.Failure("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Perform authentication asynchronously.
     *
     * @param statusCallback callback for status updates
     * @param resultCallback callback when authentication completes
     */
    public void authenticateAsync(
        Consumer<String> statusCallback,
        Consumer<AuthenticationResult> resultCallback
    ) {
        Thread authThread = Thread.ofVirtual().name("auth-thread").start(() -> {
            AuthenticationResult result = authenticate(statusCallback);
            resultCallback.accept(result);
        });
    }

    /**
     * Logout from current session.
     *
     * @return username of logged out user, or null if not authenticated
     */
    public String logout() {
        if (!sessionService.isAuthenticated()) {
            return null;
        }

        String username = sessionService.getCurrentProfile().map(p -> p.username()).orElse(null);

        try {
            sessionService.clearSession();
        } catch (Exception e) {
            // Log but don't fail logout
        }

        return username;
    }
}
