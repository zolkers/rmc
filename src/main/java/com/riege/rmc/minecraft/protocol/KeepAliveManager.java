package com.riege.rmc.minecraft.protocol;

public final class KeepAliveManager {
    private long lastKeepAliveId;
    private long lastReceivedTime;
    private final int timeoutSeconds;

    public KeepAliveManager(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.lastReceivedTime = System.currentTimeMillis();
    }

    public void recordKeepAlive(long id) {
        this.lastKeepAliveId = id;
        this.lastReceivedTime = System.currentTimeMillis();
    }

    public long getLastKeepAliveId() {
        return lastKeepAliveId;
    }

    public boolean isTimedOut() {
        long elapsed = (System.currentTimeMillis() - lastReceivedTime) / 1000;
        return elapsed > timeoutSeconds;
    }

    public long getSecondsSinceLastKeepAlive() {
        return (System.currentTimeMillis() - lastReceivedTime) / 1000;
    }
}
