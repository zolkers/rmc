package terminal.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a command execution handler.
 * <p>
 * Methods annotated with {@code @CommandHandler} within a {@link Command}
 * annotated class are invoked when the command is executed. The method
 * must accept a single {@link terminal.commands.CommandContext} parameter.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Command(name = "hello")
 * public class HelloCommand {
 *
 *     @CommandHandler
 *     public void execute(CommandContext ctx) {
 *         ctx.success("Hello, " + ctx.getArg(0, "World") + "!");
 *     }
 *
 *     @CommandHandler(subcommand = "world")
 *     public void helloWorld(CommandContext ctx) {
 *         ctx.success("Hello, World!");
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
@Target(ElementType.METHOD)
public @interface CommandHandler {

    /**
     * Specifies a subcommand that this handler responds to.
     * <p>
     * If specified, this handler will only be invoked when the first argument
     * matches this subcommand name. This allows a single command class to
     * handle multiple related subcommands.
     * </p>
     * <p>
     * For example, with {@code subcommand = "add"}, the handler will be
     * invoked for "/mycommand add arg1 arg2" but not for "/mycommand remove".
     * </p>
     *
     * @return the subcommand name, or empty string for the default handler
     */
    String subcommand() default "";

    /**
     * Priority for handler selection.
     * <p>
     * When multiple handlers could match a command invocation, the one
     * with the highest priority is selected. Default priority is 0.
     * </p>
     *
     * @return the priority value
     */
    int priority() default 0;

    /**
     * Whether this handler is asynchronous.
     * <p>
     * If true, the framework will execute this handler in a separate thread,
     * preventing blocking of the main terminal thread. Use this for
     * long-running operations like database queries or network requests.
     * </p>
     *
     * @return {@code true} for async execution; {@code false} for synchronous
     */
    boolean async() default false;
}
