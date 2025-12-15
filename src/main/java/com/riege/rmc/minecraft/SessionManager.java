package com.riege.rmc.minecraft;

import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;

public class SessionManager {
    private static AuthenticatedProfile currentProfile;

    public static void setProfile(AuthenticatedProfile profile) {
        currentProfile = profile;
    }

    public static AuthenticatedProfile getProfile() {
        return currentProfile;
    }

    public static boolean isAuthenticated() {
        return currentProfile != null && !currentProfile.isExpired();
    }

    public static void clear() {
        currentProfile = null;
    }
}
