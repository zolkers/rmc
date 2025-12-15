package com.riege.rmc.minecraft.microsoft;

public record XboxToken(String token, String userHash) {

    @Override
    public String toString() {
        return "XboxToken{" +
                "token='" + (token != null ? "***" : "null") + '\'' +
                ", userHash='" + userHash + '\'' +
                '}';
    }
}
