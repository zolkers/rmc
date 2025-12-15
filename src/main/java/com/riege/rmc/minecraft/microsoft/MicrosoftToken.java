package com.riege.rmc.minecraft.microsoft;

public record MicrosoftToken(String accessToken, String refreshToken) {

    @Override
    public String toString() {
        return "MicrosoftToken{" +
                "accessToken='" + (accessToken != null ? "***" : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? "***" : "null") + '\'' +
                '}';
    }
}
