package terminal.commands.middleware;

import terminal.commands.CommandContext;
import terminal.commands.CommandMiddleware;
import terminal.commands.annotations.Permission;

/**
 * Middleware that checks command permissions.
 * <p>
 * Validates that the command executor has the required permission
 * based on @Permission annotation.
 * </p>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
public class PermissionMiddleware implements CommandMiddleware {

    @Override
    public boolean handle(final CommandContext context, final NextHandler next) {
        final Object commandInstance = context.getData("commandInstance");
        if (commandInstance == null) {
            return next.handle();
        }

        final Class<?> clazz = commandInstance.getClass();
        final Permission annotation = clazz.getAnnotation(Permission.class);

        if (annotation != null) {
            final String requiredPermission = annotation.value();
            final String sender = context.getSender();

            if (!hasPermission(sender, requiredPermission)) {
                context.error("You don't have permission to use this command.");
                context.error("Required permission: " + requiredPermission);
                return false;
            }
        }

        return next.handle();
    }

    /**
     * Checks if a sender has a specific permission.
     * <p>
     * This is a simplified implementation. In a real system, this would
     * check against a proper permission system.
     * </p>
     *
     * @param sender     the command sender
     * @param permission the required permission
     * @return {@code true} if the sender has permission
     */
    private boolean hasPermission(final String sender, final String permission) {
        // TODO: Implement real permission checking
        // For now, console has all permissions
        return "console".equalsIgnoreCase(sender) || "server".equalsIgnoreCase(sender);
    }
}
