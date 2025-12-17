package com.riege.rmc.api.connection;

import com.riege.rmc.api.session.SessionService;
import com.riege.rmc.minecraft.microsoft.AuthenticatedProfile;
import com.riege.rmc.minecraft.protocol.ServerConnection;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service for managing Minecraft server connections.
 * Handles connection lifecycle, encryption, and protocol negotiation.
 */
public final class ConnectionService {
    private final SessionService sessionService;
    private ServerConnection currentConnection;

    public ConnectionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Connect to a Minecraft server.
     *
     * @param serverAddress server address (host:port or just host)
     * @param verbosity verbose level (0=none, 1=basic, 2=detailed, 3=full)
     * @param statusCallback callback for status updates
     * @return connection result
     */
    public ConnectionResult connect(
        String serverAddress,
        int verbosity,
        Consumer<String> statusCallback
    ) {
        Optional<AuthenticatedProfile> profileOpt = sessionService.getCurrentProfile();
        if (profileOpt.isEmpty()) {
            return new ConnectionResult.NotAuthenticated();
        }

        AuthenticatedProfile profile = profileOpt.get();

        try {
            statusCallback.accept("Connecting to " + serverAddress + "...");

            ServerConnection connection = new ServerConnection(
                serverAddress,
                statusCallback,
                verbosity
            );

            connection.connect(profile);

            this.currentConnection = connection;

            return new ConnectionResult.Success(serverAddress, profile.username());

        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new ConnectionResult.Failure(serverAddress, errorMsg, e);
        }
    }

    /**
     * Connect to server asynchronously.
     *
     * @param serverAddress server address
     * @param verbosity verbose level
     * @param statusCallback status updates
     * @param resultCallback completion callback
     */
    public void connectAsync(
        String serverAddress,
        int verbosity,
        Consumer<String> statusCallback,
        Consumer<ConnectionResult> resultCallback
    ) {
        Thread connectionThread = Thread.ofVirtual().name("connection-thread").start(() -> {
            ConnectionResult result = connect(serverAddress, verbosity, statusCallback);
            resultCallback.accept(result);
        });
    }

    /**
     * Disconnect from current server.
     */
    public void disconnect() {
        if (currentConnection != null) {
            currentConnection.disconnect();
            currentConnection = null;
        }
    }

    /**
     * Check if currently connected to a server.
     */
    public boolean isConnected() {
        return currentConnection != null && currentConnection.getConnection().isConnected();
    }

    /**
     * Get current server connection (if any).
     */
    public Optional<ServerConnection> getCurrentConnection() {
        return Optional.ofNullable(currentConnection);
    }
}
