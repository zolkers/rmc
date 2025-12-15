package com.riege.rmc.terminal.command.impl;

import com.riege.rmc.terminal.command.core.BaseCommand;
import com.riege.rmc.terminal.command.core.CommandContext;
import com.riege.rmc.terminal.command.core.CommandFramework;
import com.riege.rmc.terminal.command.core.CommandInfo;
import com.riege.rmc.terminal.command.annotations.Command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Command(
    name = "help",
    description = "Affiche la liste dynamique des commandes",
    aliases = {"?", "h"},
    usage = "help [page]"
)
public class HelpCommand extends BaseCommand {

    private final CommandFramework framework;
    public HelpCommand(CommandFramework framework) {
        this.framework = framework;
    }

    @Override
    public void execute(CommandContext ctx) {
        List<CommandInfo> commands = new ArrayList<>(framework.getRegistry().getAllCommands());
        
        commands.sort(Comparator.comparing(CommandInfo::getName));

        int page = ctx.getArgAsInt(0, 1);
        int commandsPerPage = 6;
        int maxPages = (int) Math.ceil((double) commands.size() / commandsPerPage);

        if (page < 1) page = 1;
        if (page > maxPages && maxPages > 0) page = maxPages;

        msg(ctx, " ");
        msg(ctx, "=== Help (Page " + page + "/" + maxPages + ") ===");
        msg(ctx, " ");

        if (commands.isEmpty()) {
            msg(ctx, "No registered commands.");
            return;
        }

        int start = (page - 1) * commandsPerPage;
        int end = Math.min(start + commandsPerPage, commands.size());

        for (int i = start; i < end; i++) {
            CommandInfo cmd = commands.get(i);
            
            StringBuilder line = new StringBuilder(" • ");
            line.append(cmd.getName());

            if (cmd.getAliases().length > 0) {
                line.append(" [")
                    .append(String.join(", ", cmd.getAliases()))
                    .append("]");
            }

            line.append(" : ").append(cmd.getDescription());
            
            msg(ctx, line.toString());
        }

        msg(ctx, " ");
        if (page < maxPages) {
            msg(ctx, "Type 'help " + (page + 1) + "' to see the next pages.");
        }
    }
}