package com.riege.rmc;

import com.riege.rmc.minecraft.SessionManager;
import com.riege.rmc.persistence.PersistenceManager;
import com.riege.rmc.terminal.command.bridge.RustTerminal;
import com.riege.rmc.terminal.command.core.CommandManager;
import com.riege.rmc.terminal.command.impl.AuthCommand;
import com.riege.rmc.terminal.command.impl.ConnectCommand;
import com.riege.rmc.terminal.command.impl.ExitCommand;
import com.riege.rmc.terminal.command.impl.HelpCommand;
import com.riege.rmc.terminal.command.impl.LogoutCommand;
import com.riege.rmc.terminal.command.impl.StatusCommand;
import com.riege.rmc.terminal.logging.Logger;
import com.riege.rmc.terminal.logging.MessageLogger;

import java.util.List;

@SuppressWarnings("unused")
public class Main {

    public static void main(String[] args) {
        Logger.initialize();

        // Load persisted profile
        try {
            PersistenceManager.getInstance().loadProfile()
                .ifPresent(profile -> {
                    SessionManager.setProfile(profile);
                    Logger.success("Profile loaded: " + profile.username());
                });
        } catch (Exception e) {
            Logger.warning("Could not load profile: " + e.getMessage());
        }

        CommandManager manager = new CommandManager();
        manager.register(new HelpCommand(manager.getFramework()));
        manager.register(new ExitCommand());
        manager.register(new AuthCommand());
        manager.register(new LogoutCommand());
        manager.register(new ConnectCommand());
        manager.register(new StatusCommand());

        try {
            System.out.println("[DEBUG] Loading Rust terminal library...");
            RustTerminal terminal = RustTerminal.INSTANCE;
            System.out.println("[DEBUG] Rust terminal loaded successfully");

            MessageLogger.setRustBridge(terminal);
            System.out.println("[DEBUG] Message logger bridge set");

            RustTerminal.NativeCallback onInput = (input) -> {
                if (input.equalsIgnoreCase("exit")) {
                    terminal.terminal_close();
                    System.exit(0);
                } else {
                    manager.execute(input);
                }
            };

            RustTerminal.NativeCallback onTab = (buffer) -> {
                List<String> matches = manager.getFramework().getRegistry().findMatchingCommands(buffer);
                for (String match : matches) {
                    terminal.terminal_add_candidate(match);
                }
            };

            System.out.println("[DEBUG] Registering callbacks...");
            terminal.terminal_register_input_callback(onInput);
            terminal.terminal_register_tab_callback(onTab);
            System.out.println("[DEBUG] Callbacks registered");

            Logger.success("Backend initialised");

            System.out.println("[DEBUG] Starting terminal thread...");
            Thread rustThread = new Thread(terminal::terminal_start, "riege-xterm-frontend");
            rustThread.start();
            System.out.println("[DEBUG] Terminal thread started, waiting for it to complete...");

            rustThread.join();
            System.out.println("[DEBUG] Terminal thread completed");

        } catch (Throwable e) {
            System.err.println("Critical error, cannot load riege-xterm.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}