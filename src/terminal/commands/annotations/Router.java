package terminal.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a command router for organizing related commands.
 * <p>
 * Routers provide a way to group commands and apply middleware to entire groups.
 * Commands within a router are automatically registered when the router is scanned.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}Router(
 *     name = "admin",
 *     description = "Admin commands",
 *     permission = "server.admin"
 * )
 * public class AdminRouter {
 *
 *     {@literal @}Command(name = "ban")
 *     public class BanCommand {
 *         {@literal @}CommandHandler
 *         public void execute(CommandContext ctx) {
 *             // ban logic
 *         }
 *     }
 *
 *     {@literal @}Command(name = "kick")
 *     public class KickCommand {
 *         {@literal @}CommandHandler
 *         public void execute(CommandContext ctx) {
 *             // kick logic
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
@Target(ElementType.TYPE)
public @interface Router {

    /**
     * The router name.
     *
     * @return the router name
     */
    String name();

    /**
     * The router description.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Whether this router is enabled.
     *
     * @return {@code true} if enabled
     */
    boolean enabled() default true;

    /**
     * Permission required for all commands in this router.
     *
     * @return the permission string
     */
    String permission() default "";

    /**
     * Priority for router registration (higher = first).
     *
     * @return the priority
     */
    int priority() default 0;
}
