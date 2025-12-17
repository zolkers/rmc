package com.riege.rmc.api.chat;

/**
 * Types of chat messages that can be received from a Minecraft server.
 */
public enum ChatMessageType {
    /**
     * Message from a player.
     */
    PLAYER,

    /**
     * System message (server-generated, not from a player).
     */
    SYSTEM,

    /**
     * Server announcement or broadcast.
     */
    ANNOUNCEMENT
}
