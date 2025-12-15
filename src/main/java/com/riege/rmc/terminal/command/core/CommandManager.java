package com.riege.rmc.terminal.command.core;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final CommandFramework framework;
    private final List<BaseCommand> registeredCommands;

    public CommandManager() {
        this.framework = new CommandFramework();
        this.registeredCommands = new ArrayList<>();

        if (!CommandAPI.getInstance().isInitialized()) {
            CommandAPI.getInstance().initialize(this.framework);
        }
    }

    public void register(BaseCommand command) {
        if (framework.registerCommand(command)) {
            registeredCommands.add(command);
        }
    }

    public void execute(String input) {
        framework.executeCommand(input);
    }
    public CommandFramework getFramework() {
        return framework;
    }
}