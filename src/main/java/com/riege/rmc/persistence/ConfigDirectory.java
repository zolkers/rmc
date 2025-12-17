package com.riege.rmc.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ConfigDirectory {

    public static Path getConfigPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "rmc");
            }
            return Paths.get(home, "AppData", "Roaming", "rmc");
        } else if (os.contains("mac")) {
            return Paths.get(home, "Library", "Application Support", "rmc");
        } else {
            String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfigHome != null) {
                return Paths.get(xdgConfigHome, "rmc");
            }
            return Paths.get(home, ".config", "rmc");
        }
    }

    public static void ensureExists() throws IOException {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
