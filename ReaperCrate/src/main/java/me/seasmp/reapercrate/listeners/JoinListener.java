package me.seasmp.reapercrate.listeners;

import me.seasmp.reapercrate.ReaperCrate;
import me.seasmp.reapercrate.managers.ConfigManager;
import me.seasmp.reapercrate.managers.ResourcePackManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles player join events for the ReaperCrate plugin.
 *
 * <p>Sends the optional resource pack to joining players when the feature is
 * enabled in {@code config.yml}. Acts as the foundation for future first-join
 * logic (welcome kits, join messages, etc.).</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class JoinListener implements Listener {

    private final ReaperCrate        plugin;
    private final ResourcePackManager resourcePackManager;
    private final ConfigManager       configManager;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new JoinListener.
     *
     * @param plugin              the plugin instance
     * @param resourcePackManager the resource pack manager
     * @param configManager       the config manager
     */
    public JoinListener(@NotNull final ReaperCrate        plugin,
                        @NotNull final ResourcePackManager resourcePackManager,
                        @NotNull final ConfigManager       configManager) {
        this.plugin              = plugin;
        this.resourcePackManager = resourcePackManager;
        this.configManager       = configManager;
    }

    // ─── Events ──────────────────────────────────────────────────────────────

    /**
     * Sends the resource pack to the joining player if the feature is enabled.
     *
     * @param event the join event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(@NotNull final PlayerJoinEvent event) {
        if (configManager.isResourcePackEnabled()) {
            resourcePackManager.sendResourcePack(event.getPlayer());
        }
    }
}
