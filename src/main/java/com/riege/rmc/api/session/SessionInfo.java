package com.riege.rmc.api.session;

import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;

import java.time.Duration;
import java.time.Instant;

/**
 * Immutable session information DTO.
 */
public record SessionInfo(
    String username,
    String uuid,
    boolean authenticated,
    boolean expired,
    Duration remainingTime,
    boolean connected
) {
    public static SessionInfo notAuthenticated() {
        return new SessionInfo(null, null, false, true, Duration.ZERO, false);
    }

    public static SessionInfo fromProfile(AuthenticatedProfile profile) {
        Instant now = Instant.now();
        Instant expiresAt = profile.expiresAt();
        boolean expired = now.isAfter(expiresAt);
        Duration remaining = expired ? Duration.ZERO : Duration.between(now, expiresAt);

        return new SessionInfo(
            profile.username(),
            profile.uuid().toString(),
            true,
            expired,
            remaining,
            false // TODO: Track connection status
        );
    }
}
