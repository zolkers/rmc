package com.riege.rmc.api;

import com.riege.rmc.api.auth.AuthenticationService;
import com.riege.rmc.api.chat.ChatService;
import com.riege.rmc.api.connection.ConnectionService;
import com.riege.rmc.api.session.SessionService;

/**
 * Main API facade for RMC (Riege Minecraft Client).
 * Provides a unified interface to all application services.
 *
 * <p>Usage example:
 * <pre>{@code
 * RMCApi api = RMCApi.getInstance();
 *
 * // Authentication
 * api.auth().authenticateAsync(
 *     status -> System.out.println(status),
 *     result -> {
 *         if (result instanceof AuthenticationResult.Success success) {
 *             System.out.println("Logged in as: " + success.profile().username());
 *         }
 *     }
 * );
 *
 * // Server connection
 * api.connection().connectAsync(
 *     "mc.hypixel.net",
 *     2, // verbosity level
 *     status -> System.out.println(status),
 *     result -> {
 *         if (result instanceof ConnectionResult.Success) {
 *             System.out.println("Connected!");
 *         }
 *     }
 * );
 *
 * // Session info
 * SessionInfo info = api.session().getSessionInfo();
 * System.out.println("User: " + info.username());
 *
 * // Chat
 * api.chat().addListener(message -> {
 *     System.out.println(message.format());
 * });
 * api.chat().sendMessage("Hello, world!");
 * }</pre>
 */
public final class RMCApi {
    private static final RMCApi INSTANCE = new RMCApi();

    private final SessionService sessionService;
    private final AuthenticationService authService;
    private final ConnectionService connectionService;
    private final ChatService chatService;

    private RMCApi() {
        this.sessionService = new SessionService();
        this.chatService = new ChatService();
        this.authService = new AuthenticationService(sessionService);
        this.connectionService = new ConnectionService(sessionService, chatService);
    }

    /**
     * Get the singleton RMC API instance.
     */
    public static RMCApi getInstance() {
        return INSTANCE;
    }

    /**
     * Access authentication operations.
     */
    public AuthenticationService auth() {
        return authService;
    }

    /**
     * Access server connection operations.
     */
    public ConnectionService connection() {
        return connectionService;
    }

    /**
     * Access session management operations.
     */
    public SessionService session() {
        return sessionService;
    }

    /**
     * Access chat operations.
     */
    public ChatService chat() {
        return chatService;
    }

    /**
     * Initialize the API and load persisted session if available.
     */
    public void initialize() {
        sessionService.loadPersistedProfile().ifPresent(profile -> {
            try {
                sessionService.setProfile(profile);
            } catch (Exception e) {
                // Profile couldn't be restored, ignore
            }
        });
    }
}
