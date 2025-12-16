package com.riege.rmc.persistence.dto;

import com.google.gson.annotations.SerializedName;

public record ServerFavorite(
    @SerializedName("alias")
    String alias,

    @SerializedName("address")
    String address,

    @SerializedName("last_connected")
    long lastConnected,

    @SerializedName("connect_count")
    int connectCount
) implements Comparable<ServerFavorite> {

    @Override
    public int compareTo(ServerFavorite other) {
        return Long.compare(other.lastConnected, this.lastConnected);
    }

    public ServerFavorite incrementConnectCount() {
        return new ServerFavorite(
            alias,
            address,
            System.currentTimeMillis(),
            connectCount + 1
        );
    }
}
