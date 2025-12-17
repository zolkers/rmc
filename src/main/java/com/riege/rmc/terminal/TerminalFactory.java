package com.riege.rmc.terminal;

import com.riege.rmc.platform.SystemInfo;
import com.riege.rmc.terminal.impl.JnaRTerminal;
import com.riege.rmc.terminal.impl.SwingTerminal;

public final class TerminalFactory {

    public static Terminal create() {
        if (SystemInfo.isLinux()) {
            return new JnaRTerminal();
        } else {
            return new SwingTerminal();
        }
    }
}
