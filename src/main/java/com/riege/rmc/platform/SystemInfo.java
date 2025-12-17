package com.riege.rmc.platform;

public final class SystemInfo {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OS_NAME.contains("win");
    }

    public static boolean isMac() {
        return OS_NAME.contains("mac");
    }

    public static boolean isLinux() {
        return OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix");
    }

    public static OS getOS() {
        if (isWindows()) {
            return OS.WINDOWS;
        } else if (isMac()) {
            return OS.MAC;
        } else if (isLinux()) {
            return OS.LINUX;
        } else {
            return OS.UNKNOWN;
        }
    }

    public enum OS {
        WINDOWS,
        MAC,
        LINUX,
        UNKNOWN
    }
}
