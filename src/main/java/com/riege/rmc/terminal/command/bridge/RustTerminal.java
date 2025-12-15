package com.riege.rmc.terminal.command.bridge;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Callback;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface RustTerminal extends Library {

    static String getLibraryName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "riege_xterm.dll";
        } else if (os.contains("mac")) {
            return "libriege_xterm.dylib";
        } else {
            return "libriege_xterm.so";
        }
    }

    static RustTerminal loadLibrary() {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path nativesDir = projectRoot.resolve("natives");
        Path libraryPath = nativesDir.resolve(getLibraryName());

        if (libraryPath.toFile().exists()) {
            return Native.load(libraryPath.toAbsolutePath().toString(), RustTerminal.class);
        }

        return Native.load("riege_xterm", RustTerminal.class);
    }

    RustTerminal INSTANCE = loadLibrary();
    void terminal_start();
    void terminal_close();
    void terminal_log_info(String msg);
    void terminal_log_error(String msg);
    void terminal_log_success(String msg);
    void terminal_log_warning(String msg);
    void terminal_log_debug(String msg);
    void terminal_add_candidate(String candidate);
    interface NativeCallback extends Callback {
        void invoke(String data);
    }

    void terminal_register_input_callback(NativeCallback callback);
    void terminal_register_tab_callback(NativeCallback callback);
}