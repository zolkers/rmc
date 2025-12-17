package com.riege.rmc.api.connection;

/**
 * Result of a server connection attempt.
 */
public sealed interface ConnectionResult {

    /**
     * Connection succeeded and is active.
     */
    record Success(String serverAddress, String username) implements ConnectionResult {}

    /**
     * Connection failed with an error.
     */
    record Failure(String serverAddress, String errorMessage, Throwable cause) implements ConnectionResult {}

    /**
     * Connection is in progress.
     */
    record InProgress(String serverAddress, String statusMessage) implements ConnectionResult {}

    /**
     * Cannot connect - not authenticated.
     */
    record NotAuthenticated() implements ConnectionResult {}

    /**
     * Connection disconnected.
     */
    record Disconnected(String serverAddress) implements ConnectionResult {}
}
