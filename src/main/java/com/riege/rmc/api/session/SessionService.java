package com.riege.rmc.api.session;

import com.riege.rmc.minecraft.SessionManager;
import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
import com.riege.rmc.persistence.PersistenceManager;

import java.util.Optional;

/**
 * Service for managing user sessions.
 * Handles authentication state, profile persistence, and session validation.
 */
public class SessionService {
    private final PersistenceManager persistenceManager;

    public SessionService() {
        this.persistenceManager = PersistenceManager.getInstance();
    }

    /**
     * Get current session information.
     */
    public SessionInfo getSessionInfo() {
        if (!SessionManager.isAuthenticated()) {
            return SessionInfo.notAuthenticated();
        }
        return SessionInfo.fromProfile(SessionManager.getProfile());
    }

    /**
     * Check if user is authenticated.
     */
    public boolean isAuthenticated() {
        return SessionManager.isAuthenticated();
    }

    /**
     * Get current authenticated profile.
     */
    public Optional<AuthenticatedProfile> getCurrentProfile() {
        return SessionManager.isAuthenticated()
            ? Optional.of(SessionManager.getProfile())
            : Optional.empty();
    }

    /**
     * Set authenticated profile and save to disk.
     */
    public void setProfile(AuthenticatedProfile profile) throws Exception {
        SessionManager.setProfile(profile);
        persistenceManager.saveProfile(profile);
    }

    /**
     * Load profile from disk.
     */
    public Optional<AuthenticatedProfile> loadPersistedProfile() {
        try {
            return persistenceManager.loadProfile();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Clear current session and remove from disk.
     */
    public void clearSession() throws Exception {
        SessionManager.clear();
        persistenceManager.clearProfile();
    }

    /**
     * Check if current session is expired.
     */
    public boolean isExpired() {
        return SessionManager.isAuthenticated() && SessionManager.getProfile().isExpired();
    }
}
