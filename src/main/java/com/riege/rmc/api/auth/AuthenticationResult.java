package com.riege.rmc.api.auth;

import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;

/**
 * Result of an authentication attempt.
 */
public sealed interface AuthenticationResult {

    /**
     * Authentication succeeded.
     */
    record Success(AuthenticatedProfile profile) implements AuthenticationResult {}

    /**
     * Authentication failed with an error.
     */
    record Failure(String errorMessage, Throwable cause) implements AuthenticationResult {}

    /**
     * Authentication is in progress.
     */
    record InProgress(String statusMessage) implements AuthenticationResult {}

    /**
     * User is already authenticated.
     */
    record AlreadyAuthenticated(AuthenticatedProfile profile) implements AuthenticationResult {}
}
