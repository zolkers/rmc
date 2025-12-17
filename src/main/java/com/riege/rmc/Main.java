package com.riege.rmc;

import com.riege.rmc.api.RMCApi;
import com.riege.rmc.terminal.Terminal;
import com.riege.rmc.terminal.TerminalFactory;
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

        // Initialize RMC API (loads persisted profile)
        RMCApi api = RMCApi.getInstance();
        api.initialize();

        api.session().getCurrentProfile().ifPresent(profile ->
            Logger.success("Profile loaded: " + profile.username())
        );

        CommandManager manager = new CommandManager();
        manager.register(new HelpCommand(manager.getFramework()));
        manager.register(new ExitCommand());
        manager.register(new AuthCommand());
        manager.register(new LogoutCommand());
        manager.register(new ConnectCommand());
        manager.register(new StatusCommand());

        try {
            System.out.println("[DEBUG] Creating terminal...");
            Terminal terminal = TerminalFactory.create();
            System.out.println("[DEBUG] Terminal created successfully");

            MessageLogger.setTerminal(terminal);
            System.out.println("[DEBUG] Message logger terminal set");

            Terminal.NativeCallback onInput = (input) -> {
                if (input.equalsIgnoreCase("exit")) {
                    terminal.close();
                    System.exit(0);
                } else {
                    manager.execute(input);
                }
            };

            Terminal.NativeCallback onTab = (buffer) -> {
                List<String> matches = manager.getFramework().getRegistry().findMatchingCommands(buffer);
                for (String match : matches) {
                    terminal.addCandidate(match);
                }
            };

            System.out.println("[DEBUG] Registering callbacks...");
            terminal.registerInputCallback(onInput);
            terminal.registerTabCallback(onTab);
            System.out.println("[DEBUG] Callbacks registered");

            Logger.success("Backend initialised");

            System.out.println("[DEBUG] Starting terminal thread...");
            Thread terminalThread = new Thread(terminal::start, "riege-xterm-frontend");
            terminalThread.start();
            System.out.println("[DEBUG] Terminal thread started, waiting for it to complete...");

            terminalThread.join();
            System.out.println("[DEBUG] Terminal thread completed");

        } catch (Throwable e) {
            System.err.println("Critical error, cannot load terminal.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}