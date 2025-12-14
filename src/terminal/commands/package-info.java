/**
 * Annotation-based command framework for the terminal system.
 * <p>
 * This package provides a modern, flexible command system using Java annotations
 * instead of traditional programmatic registration. It simplifies command
 * development and promotes clean, maintainable code through declarative design.
 * </p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link terminal.commands.CommandFramework} - Main framework coordinator</li>
 *   <li>{@link terminal.commands.CommandAPI} - Global API for command interaction</li>
 *   <li>{@link terminal.commands.CommandContext} - Execution context passed to handlers</li>
 *   <li>{@link terminal.commands.CommandRegistry} - Command storage and lookup</li>
 *   <li>{@link terminal.commands.CommandInfo} - Command metadata container</li>
 * </ul>
 *
 * <h2>Annotations</h2>
 * <ul>
 *   <li>{@link terminal.commands.annotations.Command} - Marks a class as a command</li>
 *   <li>{@link terminal.commands.annotations.CommandHandler} - Marks handler methods</li>
 *   <li>{@link terminal.commands.annotations.Argument} - Defines argument metadata</li>
 *   <li>{@link terminal.commands.annotations.Permission} - Specifies permissions</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * // 1. Create a command class
 * @Command(
 *     name = "teleport",
 *     description = "Teleport to a location",
 *     aliases = {"tp"},
 *     usage = "/teleport <x> <y> <z>"
 * )
 * public class TeleportCommand {
 *
 *     @CommandHandler
 *     public void execute(CommandContext ctx) {
 *         double x = ctx.getArgAsDouble(0, 0);
 *         double y = ctx.getArgAsDouble(1, 0);
 *         double z = ctx.getArgAsDouble(2, 0);
 *
 *         ctx.success("Teleported to " + x + ", " + y + ", " + z);
 *     }
 * }
 *
 * // 2. Register the command
 * CommandAPI api = CommandAPI.getInstance();
 * api.initialize();
 * api.registerCommand(new TeleportCommand());
 *
 * // 3. Execute commands
 * api.executeCommand("teleport 100 64 100");
 * }</pre>
 *
 * <h2>Subcommands</h2>
 * <p>
 * Commands can have multiple handlers for different subcommands:
 * </p>
 * <pre>{@code
 * @Command(name = "player")
 * public class PlayerCommand {
 *
 *     @CommandHandler
 *     public void execute(CommandContext ctx) {
 *         // Default handler
 *         ctx.send("Use /player <subcommand>");
 *     }
 *
 *     @CommandHandler(subcommand = "list")
 *     public void list(CommandContext ctx) {
 *         // Handles: /player list
 *         ctx.info("Listing players...");
 *     }
 *
 *     @CommandHandler(subcommand = "kick")
 *     public void kick(CommandContext ctx) {
 *         // Handles: /player kick <name>
 *         String player = ctx.getArg(0);
 *         ctx.warning("Kicking " + player);
 *     }
 * }
 * }</pre>
 *
 * <h2>Asynchronous Execution</h2>
 * <p>
 * Long-running operations can be marked as async:
 * </p>
 * <pre>{@code
 * @Command(name = "search")
 * public class SearchCommand {
 *
 *     @CommandHandler(async = true)
 *     public void execute(CommandContext ctx) {
 *         // This runs in a separate thread
 *         performLongRunningSearch(ctx.getArg(0));
 *         ctx.success("Search complete");
 *     }
 * }
 * }</pre>
 *
 * <h2>Argument Parsing</h2>
 * <p>
 * {@link terminal.commands.CommandContext} provides rich argument parsing:
 * </p>
 * <pre>{@code
 * @CommandHandler
 * public void execute(CommandContext ctx) {
 *     // String arguments
 *     String name = ctx.getArg(0, "default");
 *
 *     // Numeric arguments
 *     int count = ctx.getArgAsInt(1, 1);
 *     double value = ctx.getArgAsDouble(2, 0.0);
 *
 *     // Boolean arguments
 *     boolean flag = ctx.getArgAsBoolean(3, false);
 *
 *     // Join remaining arguments
 *     String message = ctx.joinArgs(4);
 *
 *     // Validate argument count
 *     if (!ctx.requireMinArgs(2)) {
 *         return; // Error message sent automatically
 *     }
 * }
 * }</pre>
 *
 * <h2>Permissions</h2>
 * <p>
 * Commands can require permissions (validation not implemented in base system):
 * </p>
 * <pre>{@code
 * @Command(
 *     name = "admin",
 *     permission = "server.admin"
 * )
 * @Permission("server.admin")
 * public class AdminCommand {
 *
 *     @CommandHandler
 *     public void execute(CommandContext ctx) {
 *         // Only users with server.admin can execute
 *     }
 *
 *     @CommandHandler(subcommand = "kick")
 *     @Permission("server.admin.kick")
 *     public void kick(CommandContext ctx) {
 *         // Requires more specific permission
 *     }
 * }
 * }</pre>
 *
 * <h2>Integration with Terminal</h2>
 * <p>
 * The framework integrates seamlessly with the terminal system:
 * </p>
 * <pre>{@code
 * // Initialize both systems
 * Logger.initialize();
 * CommandAPI cmdApi = CommandAPI.getInstance();
 * cmdApi.initialize();
 * Terminal terminal = new Terminal();
 *
 * // Register framework commands
 * cmdApi.registerCommand(new MyCommand());
 *
 * // Bridge terminal and framework
 * terminal.registerCommand("cmd", args -> {
 *     cmdApi.executeCommand(String.join(" ", args));
 * });
 *
 * terminal.run();
 * }</pre>
 *
 * <h2>Advanced Features</h2>
 * <ul>
 *   <li><b>Command Aliases:</b> Multiple names for the same command</li>
 *   <li><b>Priority System:</b> Control handler selection order</li>
 *   <li><b>Auto-validation:</b> Min/max argument count checks</li>
 *   <li><b>Tab Completion:</b> Partial command matching via {@link terminal.commands.CommandRegistry#findMatchingCommands}</li>
 *   <li><b>Dynamic Registration:</b> Add/remove commands at runtime</li>
 *   <li><b>Thread Safety:</b> All operations are thread-safe</li>
 * </ul>
 *
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Use descriptive command names and clear descriptions</li>
 *   <li>Provide usage strings for complex commands</li>
 *   <li>Validate arguments before processing</li>
 *   <li>Use async handlers for I/O or long-running operations</li>
 *   <li>Organize related commands with subcommands</li>
 *   <li>Include helpful error messages via {@code ctx.error()}</li>
 *   <li>Use aliases sparingly for common abbreviations</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * <p>
 * The framework automatically handles common errors:
 * </p>
 * <ul>
 *   <li>Unknown commands - displays error message</li>
 *   <li>Insufficient arguments - shows usage information</li>
 *   <li>Too many arguments - validates against maxArgs</li>
 *   <li>Handler exceptions - caught and logged gracefully</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <p>
 * See the {@link terminal.commands.examples} package for complete working examples:
 * </p>
 * <ul>
 *   <li>{@link terminal.commands.examples.TeleportCommand} - Basic command with subcommands</li>
 *   <li>{@link terminal.commands.examples.GiveCommand} - Argument validation</li>
 *   <li>{@link terminal.commands.examples.PlayerCommand} - Async handlers and complex logic</li>
 *   <li>{@link terminal.commands.examples.CommandFrameworkExample} - Complete integration demo</li>
 * </ul>
 *
 * @author riege
 * @version 1.0
 * @since 1.21.4
 */
package terminal.commands;
