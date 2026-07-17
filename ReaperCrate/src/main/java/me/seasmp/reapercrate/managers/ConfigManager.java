package me.seasmp.reapercrate.managers;

import me.seasmp.reapercrate.ReaperCrate;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Provides type-safe, centralised access to {@code config.yml}.
 *
 * <p>All configuration reads should go through this class rather than calling
 * {@link ReaperCrate#getConfig()} directly, ensuring consistent defaults
 * and easy future refactoring.</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ConfigManager {

    private final ReaperCrate plugin;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new ConfigManager backed by the given plugin.
     *
     * @param plugin the plugin instance
     */
    public ConfigManager(@NotNull final ReaperCrate plugin) {
        this.plugin = plugin;
    }

    // ─── Load / Reload ───────────────────────────────────────────────────────

    /**
     * Saves the default {@code config.yml} from the jar (if absent) and loads it.
     */
    public void load() {
        plugin.saveDefaultConfig();
    }

    /**
     * Reloads {@code config.yml} from disk, discarding in-memory values.
     */
    public void reload() {
        plugin.reloadConfig();
    }

    // ─── Raw Access ──────────────────────────────────────────────────────────

    /**
     * Returns the raw underlying {@link FileConfiguration} for direct access.
     *
     * @return the config.yml FileConfiguration
     */
    @NotNull
    public FileConfiguration getRaw() {
        return plugin.getConfig();
    }

    // ─── Typed Accessors ─────────────────────────────────────────────────────

    /**
     * Returns whether verbose debug logging is enabled.
     *
     * @return {@code true} if {@code debug: true} in config.yml
     */
    public boolean isDebug() {
        return plugin.getConfig().getBoolean("debug", false);
    }

    /**
     * Returns whether the resource-pack auto-send feature is enabled.
     *
     * @return {@code true} if {@code settings.use-resource-pack: true}
     */
    public boolean isResourcePackEnabled() {
        return plugin.getConfig().getBoolean("settings.use-resource-pack", false);
    }

    /**
     * Returns the resource-pack download URL.
     *
     * @return the URL string, or an empty string if not configured
     */
    @NotNull
    public String getResourcePackUrl() {
        return plugin.getConfig().getString("settings.resource-pack-url", "");
    }

    /**
     * Returns the SHA-1 hash of the resource pack zip.
     *
     * @return the 40-character hex hash, or an empty string if not configured
     */
    @NotNull
    public String getResourcePackHash() {
        return plugin.getConfig().getString("settings.resource-pack-hash", "");
    }

    /**
     * Returns whether the automatic update check is enabled.
     *
     * @return {@code true} if {@code settings.auto-update: true}
     */
    public boolean isAutoUpdate() {
        return plugin.getConfig().getBoolean("settings.auto-update", true);
    }
}
