package com.riege.rmc.terminal.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter as a command option (--name value).
 * <p>
 * Options are named arguments that can be provided in any order.
 * They are specified with double dashes: --name value
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}Command(name = "ban")
 * public class BanCommand {
 *     {@literal @}CommandHandler
 *     public void execute(
 *         CommandContext ctx,
 *         {@literal @}Argument(index = 0) String player,
 *         {@literal @}Option(name = "reason", defaultValue = "No reason") String reason,
 *         {@literal @}Option(name = "duration", defaultValue = "permanent") String duration
 *     ) {
 *         // /ban riege --reason "Too toxic" --duration 7d
 *     }
 * }
 * </pre>
 *
 * <p>Or access via CommandContext:</p>
 * <pre>
 * String reason = ctx.getOption("reason", "No reason");
 * int duration = ctx.getOption("duration", Integer.class, 0);
 * </pre>
 *
 * @author riege
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {

    /**
     * The option name (without dashes).
     * <p>
     * Example: "reason" for --reason
     * </p>
     *
     * @return the option name
     */
    String name();

    /**
     * Alternative short name (single dash).
     * <p>
     * Example: "r" for -r (shorthand for --reason)
     * </p>
     *
     * @return the short name
     */
    String shortName() default "";

    /**
     * Default value if option not provided.
     *
     * @return the default value
     */
    String defaultValue() default "";

    /**
     * Whether this option is required.
     *
     * @return {@code true} if required
     */
    boolean required() default false;

    /**
     * Description of this option.
     *
     * @return the description
     */
    String description() default "";
}
