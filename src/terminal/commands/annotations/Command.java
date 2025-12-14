package terminal.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a command container.
 * <p>
 * Classes annotated with {@code @Command} contain methods annotated with
 * {@link CommandHandler} that define the actual command logic. This annotation
 * provides metadata about the command including its name, description, aliases,
 * and permission requirements.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Command(
 *     name = "teleport",
 *     description = "Teleport to a location or player",
 *     aliases = {"tp", "tele"},
 *     permission = "server.teleport"
 * )
 * public class TeleportCommand {
 *     @CommandHandler
 *     public void execute(CommandContext ctx) {
 *         // Command logic here
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    /**
     * The primary name of the command.
     * <p>
     * This is the name users will type to execute the command.
     * Must be lowercase and contain only alphanumeric characters and hyphens.
     * </p>
     *
     * @return the command name
     */
    String name();

    /**
     * A brief description of what the command does.
     * <p>
     * This description is displayed in help messages and command listings.
     * </p>
     *
     * @return the command description
     */
    String description() default "";

    /**
     * Alternative names for the command.
     * <p>
     * Aliases provide convenience shortcuts. For example, "tp" might be
     * an alias for "teleport". All aliases must follow the same naming
     * rules as the primary name.
     * </p>
     *
     * @return array of command aliases
     */
    String[] aliases() default {};

    /**
     * The permission node required to execute this command.
     * <p>
     * If empty, the command is available to all users. Permission checking
     * is delegated to the framework's permission system.
     * </p>
     *
     * @return the permission node, or empty string for no permission requirement
     */
    String permission() default "";

    /**
     * Usage syntax shown in help messages.
     * <p>
     * This should demonstrate the expected argument format.
     * For example: {@code "/teleport <player> [x] [y] [z]"}
     * </p>
     *
     * @return the usage string
     */
    String usage() default "";

    /**
     * Minimum number of arguments required.
     * <p>
     * The framework will automatically validate that at least this many
     * arguments are provided before invoking the command handler.
     * </p>
     *
     * @return minimum argument count
     */
    int minArgs() default 0;

    /**
     * Maximum number of arguments allowed.
     * <p>
     * Set to -1 for unlimited arguments. The framework validates that
     * no more than this many arguments are provided.
     * </p>
     *
     * @return maximum argument count, or -1 for unlimited
     */
    int maxArgs() default -1;

    /**
     * Whether this command is enabled.
     * <p>
     * Disabled commands are not registered and cannot be executed.
     * This is useful for temporarily disabling commands without removing code.
     * </p>
     *
     * @return {@code true} if enabled; {@code false} otherwise
     */
    boolean enabled() default true;

    /**
     * Priority for command execution order.
     * <p>
     * Higher priority commands are checked first when resolving ambiguous
     * command names. Default priority is 0.
     * </p>
     *
     * @return the priority value
     */
    int priority() default 0;
}
