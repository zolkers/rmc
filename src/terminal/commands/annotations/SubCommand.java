package terminal.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class as a subcommand of a parent command.
 * <p>
 * Subcommands can be nested infinitely, allowing for complex command hierarchies
 * like {@code /player inventory clear} or {@code /world region flag set}.
 * </p>
 *
 * <p>Example usage with methods:</p>
 * <pre>
 * {@literal @}Command(name = "player")
 * public class PlayerCommand {
 *
 *     {@literal @}SubCommand(name = "teleport", usage = "/player teleport &lt;x&gt; &lt;y&gt; &lt;z&gt;")
 *     public void teleport(CommandContext ctx) {
 *         // teleport logic
 *     }
 *
 *     {@literal @}SubCommand(name = "heal")
 *     public void heal(CommandContext ctx) {
 *         // heal logic
 *     }
 * }
 * </pre>
 *
 * <p>Example with nested subcommands:</p>
 * <pre>
 * {@literal @}Command(name = "world")
 * public class WorldCommand {
 *
 *     {@literal @}SubCommand(name = "region")
 *     public static class RegionSubCommand {
 *
 *         {@literal @}SubCommand(name = "create")
 *         public void create(CommandContext ctx) {
 *             // /world region create
 *         }
 *
 *         {@literal @}SubCommand(name = "flag")
 *         public static class FlagSubCommand {
 *
 *             {@literal @}SubCommand(name = "set")
 *             public void set(CommandContext ctx) {
 *                 // /world region flag set
 *             }
 *
 *             {@literal @}SubCommand(name = "remove")
 *             public void remove(CommandContext ctx) {
 *                 // /world region flag remove
 *             }
 *         }
 *     }
 * }
 * </pre>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SubCommand {

    /**
     * The subcommand name.
     *
     * @return the subcommand name
     */
    String name();

    /**
     * The subcommand description.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Usage string for this subcommand.
     *
     * @return the usage string
     */
    String usage() default "";

    /**
     * Aliases for this subcommand.
     *
     * @return array of aliases
     */
    String[] aliases() default {};

    /**
     * Whether this subcommand is enabled.
     *
     * @return {@code true} if enabled
     */
    boolean enabled() default true;

    /**
     * Permission required for this subcommand.
     *
     * @return the permission string
     */
    String permission() default "";

    /**
     * Minimum number of arguments required.
     *
     * @return minimum args
     */
    int minArgs() default 0;

    /**
     * Maximum number of arguments allowed (-1 for unlimited).
     *
     * @return maximum args
     */
    int maxArgs() default -1;
}
