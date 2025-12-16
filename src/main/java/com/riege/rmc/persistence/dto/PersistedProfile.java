package com.riege.rmc.persistence.dto;

import com.google.gson.annotations.SerializedName;
import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;

import java.time.Instant;
import java.util.UUID;

public record PersistedProfile(
    @SerializedName("uuid")
    String uuid,

    @SerializedName("username")
    String username,

    @SerializedName("access_token")
    String accessToken,

    @SerializedName("expires_at")
    String expiresAt,

    @SerializedName("saved_timestamp")
    long savedTimestamp
) {
    public AuthenticatedProfile toAuthenticatedProfile() {
        return new AuthenticatedProfile(
            UUID.fromString(uuid),
            username,
            accessToken,
            Instant.parse(expiresAt)
        );
    }

    public static PersistedProfile from(AuthenticatedProfile profile) {
        return new PersistedProfile(
            profile.uuid().toString(),
            profile.username(),
            profile.accessToken(),
            profile.expiresAt().toString(),
            System.currentTimeMillis()
        );
    }

    public boolean isExpired() {
        try {
            return Instant.parse(expiresAt).isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }
    }
}
