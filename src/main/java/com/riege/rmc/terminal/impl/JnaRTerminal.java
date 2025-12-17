package com.riege.rmc.terminal.impl;

import com.riege.rmc.terminal.RTerminal;
import com.riege.rmc.terminal.Terminal;

public final class JnaRTerminal implements Terminal {

    private final RTerminal rTerminal;

    public JnaRTerminal() {
        this.rTerminal = RTerminal.INSTANCE;
    }

    @Override
    public void start() {
        rTerminal.terminal_start();
    }

    @Override
    public void close() {
        rTerminal.terminal_close();
    }

    @Override
    public void logInfo(String msg) {
        rTerminal.terminal_log_info(msg);
    }

    @Override
    public void logError(String msg) {
        rTerminal.terminal_log_error(msg);
    }

    @Override
    public void logSuccess(String msg) {
        rTerminal.terminal_log_success(msg);
    }

    @Override
    public void logWarning(String msg) {
        rTerminal.terminal_log_warning(msg);
    }

    @Override
    public void logDebug(String msg) {
        rTerminal.terminal_log_debug(msg);
    }

    @Override
    public void addCandidate(String candidate) {
        rTerminal.terminal_add_candidate(candidate);
    }

    @Override
    public void registerInputCallback(NativeCallback callback) {
        rTerminal.terminal_register_input_callback(callback);
    }

    @Override
    public void registerTabCallback(NativeCallback callback) {
        rTerminal.terminal_register_tab_callback(callback);
    }
}
