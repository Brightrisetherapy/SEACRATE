package me.seasmp.reapercrate.util;

import me.seasmp.reapercrate.ReaperCrate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

/**
 * Reads all user-facing messages from {@code messages.yml} and dispatches them
 * to {@link CommandSender}s.
 *
 * <p>Messages are formatted with MiniMessage. Every message returned by
 * {@link #get(String)} is automatically prepended with the configured prefix.
 * Placeholder substitution is performed on the raw string before MiniMessage
 * parsing by replacing all {@code {key}} tokens with their values.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * messageUtil.send(sender, "give-success-sender",
 *     Map.of("{item}", "soul_sword", "{player}", "Steve"));
 * }</pre>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class MessageUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final ReaperCrate plugin;
    private File              file;
    private FileConfiguration config;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new MessageUtil.
     *
     * @param plugin the plugin instance
     */
    public MessageUtil(@NotNull final ReaperCrate plugin) {
        this.plugin = plugin;
    }

    // ─── Load / Reload ───────────────────────────────────────────────────────

    /**
     * Saves the default {@code messages.yml} from the jar (if absent) and loads it.
     * Must be called once during plugin startup.
     */
    public void load() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Reloads {@code messages.yml} from disk.
     * If {@link #load()} has not yet been called, it is called implicitly.
     */
    public void reload() {
        if (file == null) {
            load();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    // ─── Retrieval ───────────────────────────────────────────────────────────

    /**
     * Returns the raw MiniMessage string for a key, or a fallback error string
     * if the key is not found.
     *
     * @param key the messages.yml key
     * @return the raw (unparsed) MiniMessage string
     */
    @NotNull
    public String getRaw(@NotNull final String key) {
        return config.getString(key, "<red>Missing message key: " + key);
    }

    /**
     * Returns the prefix {@link Component} as defined in {@code messages.yml}.
     *
     * @return the parsed prefix component
     */
    @NotNull
    public Component getPrefix() {
        return MM.deserialize(getRaw("prefix"));
    }

    /**
     * Returns a fully-formatted {@link Component} for the given key.
     *
     * <p>Placeholder tokens in the form {@code {key}} are replaced before
     * MiniMessage parsing. The prefix is prepended automatically.</p>
     *
     * @param key          the messages.yml key
     * @param placeholders map of {@code {placeholder}} → replacement string
     * @return the formatted component with prefix prepended
     */
    @NotNull
    public Component get(@NotNull final String key,
                         @NotNull final Map<String, String> placeholders) {
        String raw = getRaw(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace(entry.getKey(), entry.getValue());
        }
        return getPrefix().append(MM.deserialize(raw));
    }

    /**
     * Returns a fully-formatted {@link Component} for the given key with no placeholders.
     *
     * @param key the messages.yml key
     * @return the formatted component with prefix prepended
     */
    @NotNull
    public Component get(@NotNull final String key) {
        return get(key, Map.of());
    }

    // ─── Sending ─────────────────────────────────────────────────────────────

    /**
     * Sends a message to a {@link CommandSender}, resolving it from messages.yml.
     *
     * @param sender       the recipient
     * @param key          the messages.yml key
     * @param placeholders placeholder substitutions
     */
    public void send(@NotNull final CommandSender sender,
                     @NotNull final String key,
                     @NotNull final Map<String, String> placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    /**
     * Sends a message to a {@link CommandSender} with no placeholder substitutions.
     *
     * @param sender the recipient
     * @param key    the messages.yml key
     */
    public void send(@NotNull final CommandSender sender, @NotNull final String key) {
        sender.sendMessage(get(key));
    }

    /**
     * Sends a raw MiniMessage string directly — no prefix, no messages.yml lookup.
     *
     * @param sender the recipient
     * @param raw    the MiniMessage string to parse and send
     */
    public void sendRaw(@NotNull final CommandSender sender, @NotNull final String raw) {
        sender.sendMessage(MM.deserialize(raw));
    }
}
