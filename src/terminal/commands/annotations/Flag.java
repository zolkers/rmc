package terminal.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter as a command flag (--flag).
 * <p>
 * Flags are boolean options that don't take a value.
 * Their presence indicates {@code true}, absence indicates {@code false}.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}Command(name = "teleport")
 * public class TeleportCommand {
 *     {@literal @}CommandHandler
 *     public void execute(
 *         CommandContext ctx,
 *         {@literal @}Argument(index = 0) String player,
 *         {@literal @}Argument(index = 1) int x,
 *         {@literal @}Argument(index = 2) int y,
 *         {@literal @}Argument(index = 3) int z,
 *         {@literal @}Flag(name = "silent") boolean silent,
 *         {@literal @}Flag(name = "force") boolean force
 *     ) {
 *         // /teleport riege 100 64 100 --silent --force
 *     }
 * }
 * </pre>
 *
 * <p>Or access via CommandContext:</p>
 * <pre>
 * boolean silent = ctx.hasFlag("silent");
 * boolean verbose = ctx.hasFlag("v") || ctx.hasFlag("verbose");
 * </pre>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Flag {

    /**
     * The flag name (without dashes).
     * <p>
     * Example: "silent" for --silent
     * </p>
     *
     * @return the flag name
     */
    String name();

    /**
     * Alternative short name (single dash).
     * <p>
     * Example: "s" for -s (shorthand for --silent)
     * </p>
     *
     * @return the short name
     */
    String shortName() default "";

    /**
     * Description of this flag.
     *
     * @return the description
     */
    String description() default "";
}
