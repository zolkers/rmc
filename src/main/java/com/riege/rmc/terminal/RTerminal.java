package com.riege.rmc.terminal;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Callback;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface RTerminal extends Library {

    static String getLibraryName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win") || os.contains("mac")) {
            return "fallback";
        } else {
            return "libriege_xterm.so";
        }
    }

    static RTerminal loadLibrary() {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path nativesDir = projectRoot.resolve("natives");
        Path libraryPath = nativesDir.resolve(getLibraryName());

        if (libraryPath.toFile().exists()) {
            return Native.load(libraryPath.toAbsolutePath().toString(), RTerminal.class);
        }

        return Native.load("riege_xterm", RTerminal.class);
    }

    RTerminal INSTANCE = loadLibrary();
    void terminal_start();
    void terminal_close();
    void terminal_log_info(String msg);
    void terminal_log_error(String msg);
    void terminal_log_success(String msg);
    void terminal_log_warning(String msg);
    void terminal_log_debug(String msg);
    void terminal_add_candidate(String candidate);

    void terminal_register_input_callback(Callback callback);
    void terminal_register_tab_callback(Callback callback);
}