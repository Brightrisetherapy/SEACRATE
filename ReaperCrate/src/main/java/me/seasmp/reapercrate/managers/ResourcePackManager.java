package me.seasmp.reapercrate.managers;

import me.seasmp.reapercrate.ReaperCrate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Handles sending the optional resource pack to players on join.
 *
 * <p>Resource-pack delivery is gated behind the
 * {@code settings.use-resource-pack} option in {@code config.yml}.
 * When disabled this manager is a no-op. The resource pack itself is
 * <em>not</em> hosted by this plugin — it must be served externally
 * (e.g. via a CDN or a self-hosted HTTP server).</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ResourcePackManager {

    private final ReaperCrate  plugin;
    private final ConfigManager configManager;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new ResourcePackManager.
     *
     * @param plugin        the plugin instance
     * @param configManager the config manager used to read pack settings
     */
    public ResourcePackManager(@NotNull final ReaperCrate  plugin,
                               @NotNull final ConfigManager configManager) {
        this.plugin        = plugin;
        this.configManager = configManager;
    }

    // ─── Pack Sending ────────────────────────────────────────────────────────

    /**
     * Sends the configured resource pack to the given player.
     *
     * <p>If the feature is disabled in {@code config.yml}, or the URL is blank,
     * this method does nothing. An invalid URL format is logged as a warning.</p>
     *
     * @param player the player to send the pack to
     */
    public void sendResourcePack(@NotNull final Player player) {
        if (!configManager.isResourcePackEnabled()) {
            return;
        }

        String url = configManager.getResourcePackUrl();
        if (url.isBlank()) {
            plugin.getLogger().warning(
                    "[ResourcePackManager] 'settings.use-resource-pack' is true but"
                    + " 'settings.resource-pack-url' is not set in config.yml.");
            return;
        }

        // Validate URL syntax before sending to prevent a client kick.
        try {
            URI.create(url);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(
                    "[ResourcePackManager] Invalid resource-pack URL: " + url);
            return;
        }

        String hash = configManager.getResourcePackHash();
        if (!hash.isBlank()) {
            player.setResourcePack(url, hash);
        } else {
            player.setResourcePack(url);
        }
    }
}
