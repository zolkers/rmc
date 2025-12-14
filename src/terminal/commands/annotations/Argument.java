package terminal.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines metadata for a command argument.
 * <p>
 * This annotation can be applied to method parameters to provide additional
 * information about expected arguments, including descriptions, default values,
 * and validation rules.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @CommandHandler
 * public void execute(
 *     CommandContext ctx,
 *     @Argument(name = "player", description = "Target player name")
 *     String playerName,
 *     @Argument(name = "amount", description = "Amount to give", defaultValue = "1")
 *     int amount
 * ) {
 *     // Command logic
 * }
 * }</pre>
 * </p>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

    /**
     * The name of this argument.
     * <p>
     * Used in error messages and help text to identify the argument.
     * </p>
     *
     * @return the argument name
     */
    String name();

    /**
     * A description of what this argument represents.
     * <p>
     * Displayed in help messages and usage information.
     * </p>
     *
     * @return the argument description
     */
    String description() default "";

    /**
     * The default value if the argument is not provided.
     * <p>
     * This string will be converted to the parameter type. If empty,
     * the argument is considered required.
     * </p>
     *
     * @return the default value as a string
     */
    String defaultValue() default "";

    /**
     * Whether this argument is required.
     * <p>
     * If true and the argument is not provided (and no default value exists),
     * the command execution fails with an error message.
     * </p>
     *
     * @return {@code true} if required; {@code false} if optional
     */
    boolean required() default true;

    /**
     * Suggested values for tab completion.
     * <p>
     * These values are offered as autocomplete suggestions when users
     * press tab while typing this argument.
     * </p>
     *
     * @return array of suggested values
     */
    String[] suggestions() default {};

    /**
     * Regular expression pattern for validation.
     * <p>
     * If specified, the argument value must match this pattern.
     * Use this for format validation (e.g., email addresses, UUIDs).
     * </p>
     *
     * @return the validation pattern, or empty string for no validation
     */
    String pattern() default "";

    /**
     * Minimum value for numeric arguments.
     * <p>
     * Only applicable to numeric types (int, long, double, etc.).
     * The argument value must be greater than or equal to this value.
     * </p>
     *
     * @return the minimum value
     */
    double min() default Double.NEGATIVE_INFINITY;

    /**
     * Maximum value for numeric arguments.
     * <p>
     * Only applicable to numeric types (int, long, double, etc.).
     * The argument value must be less than or equal to this value.
     * </p>
     *
     * @return the maximum value
     */
    double max() default Double.POSITIVE_INFINITY;
}
