package me.seasmp.reapercrate.commands;

import me.seasmp.reapercrate.ReaperCrate;
import me.seasmp.reapercrate.items.ItemManager;
import me.seasmp.reapercrate.util.ItemUtil;
import me.seasmp.reapercrate.util.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all {@code /reapercrate} (alias {@code /rc}) subcommands.
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /rc help}                  — Show the help menu</li>
 *   <li>{@code /rc give <player> <item>}  — Give a custom item to a player</li>
 *   <li>{@code /rc list}                  — List all loaded items</li>
 *   <li>{@code /rc info <item>}           — Display item details</li>
 *   <li>{@code /rc reload}                — Reload config and items</li>
 * </ul>
 *
 * <p>All output is routed through {@link MessageUtil} so messages are
 * configurable in {@code messages.yml} and formatted with MiniMessage.</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ReaperCrateCommand implements CommandExecutor, TabCompleter {

    // ─── Constants ───────────────────────────────────────────────────────────

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /** Root-level subcommands for tab completion. */
    private static final List<String> ROOT_SUBS =
            List.of("help", "give", "list", "info", "reload");

    // ─── Dependencies ────────────────────────────────────────────────────────

    private final ReaperCrate plugin;
    private final ItemManager  itemManager;
    private final MessageUtil  messageUtil;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new ReaperCrateCommand handler.
     *
     * @param plugin      the plugin instance
     * @param itemManager the item manager
     * @param messageUtil the message utility
     */
    public ReaperCrateCommand(@NotNull final ReaperCrate plugin,
                              @NotNull final ItemManager  itemManager,
                              @NotNull final MessageUtil  messageUtil) {
        this.plugin      = plugin;
        this.itemManager = itemManager;
        this.messageUtil = messageUtil;
    }

    // ─── CommandExecutor ─────────────────────────────────────────────────────

    @Override
    public boolean onCommand(@NotNull final CommandSender sender,
                             @NotNull final Command command,
                             @NotNull final String label,
                             @NotNull final String[] args) {

        if (!sender.hasPermission("reapercrate.admin")) {
            messageUtil.send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help"   -> sendHelp(sender);
            case "give"   -> handleGive(sender, args);
            case "list"   -> handleList(sender);
            case "info"   -> handleInfo(sender, args);
            case "reload" -> handleReload(sender);
            default       -> messageUtil.send(sender, "unknown-command");
        }

        return true;
    }

    // ─── Subcommand Handlers ─────────────────────────────────────────────────

    /**
     * Handles {@code /rc give <player> <item>}.
     * Gives a cloned custom item to the target player.
     */
    private void handleGive(@NotNull final CommandSender sender,
                            @NotNull final String[] args) {
        if (args.length < 3) {
            messageUtil.send(sender, "give-usage");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messageUtil.send(sender, "give-unknown-player",
                    Map.of("{player}", args[1]));
            return;
        }

        String itemId = args[2].toLowerCase();
        ItemStack item = itemManager.getItem(itemId);
        if (item == null) {
            messageUtil.send(sender, "give-unknown-item",
                    Map.of("{item}", itemId));
            return;
        }

        ItemUtil.giveOrDrop(target, item);

        messageUtil.send(sender, "give-success-sender",
                Map.of("{item}", itemId, "{player}", target.getName()));

        // Notify the recipient only if they are not the sender.
        if (!sender.getName().equals(target.getName())) {
            messageUtil.send(target, "give-success-receiver",
                    Map.of("{item}", itemId));
        }
    }

    /**
     * Handles {@code /rc list}.
     * Prints an alphabetically sorted list of all loaded item IDs.
     */
    private void handleList(@NotNull final CommandSender sender) {
        Set<String> names = itemManager.getItemNames();

        sender.sendMessage(MM.deserialize(messageUtil.getRaw("list-header")));

        if (names.isEmpty()) {
            sender.sendMessage(MM.deserialize(messageUtil.getRaw("list-empty")));
        } else {
            List<String> sorted = new ArrayList<>(names);
            Collections.sort(sorted);
            for (String name : sorted) {
                sender.sendMessage(MM.deserialize(
                        messageUtil.getRaw("list-entry")
                                .replace("{item}", name)));
            }
        }

        sender.sendMessage(MM.deserialize(messageUtil.getRaw("list-footer")));
    }

    /**
     * Handles {@code /rc info <item>}.
     * Displays material, custom model data, display name, enchantments, and flags.
     */
    private void handleInfo(@NotNull final CommandSender sender,
                            @NotNull final String[] args) {
        if (args.length < 2) {
            messageUtil.send(sender, "give-usage");
            return;
        }

        String itemId = args[1].toLowerCase();
        ItemStack item = itemManager.getItem(itemId);
        if (item == null) {
            messageUtil.send(sender, "give-unknown-item",
                    Map.of("{item}", itemId));
            return;
        }

        ItemMeta meta = item.getItemMeta();

        sender.sendMessage(MM.deserialize(
                messageUtil.getRaw("info-header")
                        .replace("{item}", itemId)));

        sender.sendMessage(MM.deserialize(
                messageUtil.getRaw("info-material")
                        .replace("{value}", item.getType().name())));

        if (meta.hasCustomModelData()) {
            sender.sendMessage(MM.deserialize(
                    messageUtil.getRaw("info-model-data")
                            .replace("{value}", String.valueOf(meta.getCustomModelData()))));
        }

        if (meta.hasDisplayName() && meta.displayName() != null) {
            String displayName = MM.serialize(Objects.requireNonNull(meta.displayName()));
            sender.sendMessage(MM.deserialize(
                    messageUtil.getRaw("info-display-name")
                            .replace("{value}", displayName)));
        }

        if (meta.hasEnchants()) {
            String enchants = meta.getEnchants().entrySet().stream()
                    .map(e -> e.getKey().getKey().getKey() + ":" + e.getValue())
                    .collect(Collectors.joining(", "));
            sender.sendMessage(MM.deserialize(
                    messageUtil.getRaw("info-enchants")
                            .replace("{value}", enchants)));
        }

        if (!meta.getItemFlags().isEmpty()) {
            String flags = meta.getItemFlags().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            sender.sendMessage(MM.deserialize(
                    messageUtil.getRaw("info-flags")
                            .replace("{value}", flags)));
        }

        sender.sendMessage(MM.deserialize(messageUtil.getRaw("info-footer")));
    }

    /**
     * Handles {@code /rc reload}.
     * Delegates to {@link ReaperCrate#reloadPlugin()}.
     */
    private void handleReload(@NotNull final CommandSender sender) {
        try {
            plugin.reloadPlugin();
            messageUtil.send(sender, "reload-success");
        } catch (Exception e) {
            plugin.getLogger().severe("[ReaperCrate] Reload failed: " + e.getMessage());
            messageUtil.send(sender, "reload-fail");
        }
    }

    /**
     * Sends the help menu listing all available subcommands.
     */
    private void sendHelp(@NotNull final CommandSender sender) {
        sender.sendMessage(MM.deserialize(messageUtil.getRaw("help-header")));
        sender.sendMessage(MM.deserialize(messageUtil.getRaw("help-give")));
        sender.sendMessage(MM.deserialize(messageUtil.getRaw("help-list")));
        sender.sendMessage(MM.deserialize(messageUtil.getRaw("help-info")));
        sender.sendMessage(MM.deserialize(messageUtil.getRaw("help-reload")));
        sender.sendMessage(MM.deserialize(messageUtil.getRaw("help-footer")));
    }

    // ─── TabCompleter ────────────────────────────────────────────────────────

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull final CommandSender sender,
                                      @NotNull final Command command,
                                      @NotNull final String alias,
                                      @NotNull final String[] args) {

        if (!sender.hasPermission("reapercrate.admin")) {
            return Collections.emptyList();
        }

        // /rc <subcommand>
        if (args.length == 1) {
            return filterStartsWith(ROOT_SUBS, args[0]);
        }

        String sub = args[0].toLowerCase();

        // /rc give <player>  or  /rc info <item>
        if (args.length == 2) {
            return switch (sub) {
                case "give" -> getOnlinePlayerNames(args[1]);
                case "info" -> filterStartsWith(
                        new ArrayList<>(itemManager.getItemNames()), args[1]);
                default -> Collections.emptyList();
            };
        }

        // /rc give <player> <item>
        if (args.length == 3 && sub.equals("give")) {
            return filterStartsWith(
                    new ArrayList<>(itemManager.getItemNames()), args[2]);
        }

        return Collections.emptyList();
    }

    // ─── Tab Completion Helpers ───────────────────────────────────────────────

    /**
     * Returns names of all online players whose names start with {@code prefix}.
     */
    @NotNull
    private List<String> getOnlinePlayerNames(@NotNull final String prefix) {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
        }
        return filterStartsWith(names, prefix);
    }

    /**
     * Filters a list to entries that start with the given prefix (case-insensitive).
     */
    @NotNull
    private List<String> filterStartsWith(@NotNull final List<String> list,
                                          @NotNull final String prefix) {
        if (prefix.isEmpty()) return list;
        String lower = prefix.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}