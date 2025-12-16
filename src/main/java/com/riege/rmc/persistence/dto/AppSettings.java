package com.riege.rmc.persistence.dto;

import com.google.gson.annotations.SerializedName;

public record AppSettings(
    @SerializedName("default_verbosity")
    int defaultVerbosity,

    @SerializedName("auto_load_profile")
    boolean autoLoadProfile,

    @SerializedName("keep_alive_timeout")
    int keepAliveTimeout
) {
    public static AppSettings defaults() {
        return new AppSettings(0, true, 30);
    }
}
