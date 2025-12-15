package com.riege.rmc.minecraft.microsoft;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record AuthenticatedProfile(UUID uuid, String username, String accessToken, Instant expiresAt) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt.minus(5, ChronoUnit.MINUTES));
    }

    @Override
    public String toString() {
        return "AuthenticatedProfile{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", accessToken='" + (accessToken != null ? "***" : "null") + '\'' +
                ", expiresAt=" + expiresAt +
                ", expired=" + isExpired() +
                '}';
    }
}
