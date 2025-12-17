package com.riege.rmc.api.chat;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a chat message received from a Minecraft server.
 * Immutable data transfer object for chat events.
 */
public record ChatMessage(
    String content,
    ChatMessageType type,
    Instant timestamp,
    Optional<UUID> sender,
    Optional<String> senderName
) {
    /**
     * Create a system chat message (no player sender).
     */
    public static ChatMessage system(String content) {
        return new ChatMessage(
            content,
            ChatMessageType.SYSTEM,
            Instant.now(),
            Optional.empty(),
            Optional.empty()
        );
    }

    /**
     * Create a player chat message.
     */
    public static ChatMessage player(String content, UUID sender, String senderName) {
        return new ChatMessage(
            content,
            ChatMessageType.PLAYER,
            Instant.now(),
            Optional.of(sender),
            Optional.of(senderName)
        );
    }

    /**
     * Create a server announcement message.
     */
    public static ChatMessage announcement(String content) {
        return new ChatMessage(
            content,
            ChatMessageType.ANNOUNCEMENT,
            Instant.now(),
            Optional.empty(),
            Optional.empty()
        );
    }

    /**
     * Check if this message is from a player.
     */
    public boolean isPlayerMessage() {
        return type == ChatMessageType.PLAYER && sender.isPresent();
    }

    /**
     * Check if this message is a system message.
     */
    public boolean isSystemMessage() {
        return type == ChatMessageType.SYSTEM;
    }

    /**
     * Format the message for display.
     */
    public String format() {
        return switch (type) {
            case PLAYER -> senderName.map(name -> "<" + name + "> ").orElse("") + content;
            case SYSTEM -> "[SYSTEM] " + content;
            case ANNOUNCEMENT -> "[SERVER] " + content;
        };
    }
}
