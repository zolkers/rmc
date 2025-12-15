package com.riege.rmc.terminal.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies permission requirements for a command or command handler.
 * <p>
 * This annotation can be applied at the class level (affecting all handlers)
 * or at the method level (affecting only that handler). Method-level
 * permissions override class-level permissions.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Command(name = "admin")
 * @Permission("server.admin")
 * public class AdminCommand {
 *
 *     @CommandHandler
 *     public void execute(CommandContext ctx) {
 *         // Requires server.admin permission
 *     }
 *
 *     @CommandHandler(subcommand = "kick")
 *     @Permission("server.admin.kick")
 *     public void kick(CommandContext ctx) {
 *         // Requires server.admin.kick permission
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author riege
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Permission {

    /**
     * The permission node required.
     * <p>
     * Uses dot notation for hierarchical permissions.
     * For example: "server.admin.kick"
     * </p>
     *
     * @return the permission node
     */
    String value();

    /**
     * Message to display when permission is denied.
     * <p>
     * If empty, a default permission denied message is used.
     * </p>
     *
     * @return the custom denial message
     */
    String deniedMessage() default "";

    /**
     * Whether to check permissions strictly.
     * <p>
     * If true, only exact permission matches are accepted.
     * If false, wildcard permissions are also checked (e.g., "server.*").
     * </p>
     *
     * @return {@code true} for strict checking; {@code false} for wildcard support
     */
    boolean strict() default false;
}
