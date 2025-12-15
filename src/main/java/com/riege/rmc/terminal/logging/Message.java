package com.riege.rmc.terminal.logging;

import java.time.Instant;

/**
 * Represents a single log message with content, type, and timestamp.
 *
 * @author riege
 * @version 1.0
 */
public final class Message {

    private final String content;
    private final MessageType type;
    private final Instant timestamp;

    public Message(final String content, final MessageType type) {
        this.content = content;
        this.type = type;
        this.timestamp = Instant.now();
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + content;
    }
}