package com.riege.rmc.terminal.command.middleware;

import com.riege.rmc.terminal.command.core.CommandContext;
import com.riege.rmc.terminal.command.core.CommandMiddleware;
import com.riege.rmc.terminal.command.core.PermissionProvider;
import com.riege.rmc.terminal.command.annotations.Permission;

public record PermissionMiddleware(PermissionProvider provider) implements CommandMiddleware {

    @Override
    public boolean handle(final CommandContext context, final NextHandler next) {
        final Object commandInstance = context.getData("commandInstance");
        if (commandInstance == null) return next.handle();

        final Class<?> clazz = commandInstance.getClass();

        if (!checkPermission(context, clazz.getAnnotation(Permission.class))) return false;


        return next.handle();
    }

    private boolean checkPermission(CommandContext ctx, Permission perm) {
        if (perm == null) return true;

        if (perm.value().isEmpty()) return true;

        if (!provider.hasPermission(ctx.getSender(), perm.value())) {
            String msg = perm.deniedMessage().isEmpty()
                    ? "Permission denied: " + perm.value()
                    : perm.deniedMessage();
            ctx.error(msg);
            return false;
        }
        return true;
    }
}