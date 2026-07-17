package me.seasmp.reapercrate.listeners;

import me.seasmp.reapercrate.ReaperCrate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles general player interaction events for ReaperCrate items.
 *
 * <p>This listener is intentionally minimal in 1.0.0 and serves as a clean
 * hook point for future per-item interaction logic such as:</p>
 * <ul>
 *   <li>Right-click abilities on custom weapons or tools</li>
 *   <li>Consumable item effects triggered on use</li>
 *   <li>Sound and particle emission on interact</li>
 * </ul>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class PlayerListener implements Listener {

    private final ReaperCrate plugin;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new PlayerListener.
     *
     * @param plugin the plugin instance
     */
    public PlayerListener(@NotNull final ReaperCrate plugin) {
        this.plugin = plugin;
    }

    // ─── Events ──────────────────────────────────────────────────────────────

    /**
     * Reserved hook for item interaction handling.
     *
     * <p>Currently a no-op. Expand this method to read the PDC tag of the
     * held item and trigger custom actions accordingly.</p>
     *
     * @param event the player interact event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(@NotNull final PlayerInteractEvent event) {
        // Example future use:
        // ItemStack held = ItemUtil.getHeldItem(event.getPlayer());
        // NamespacedKey pdcKey = new NamespacedKey(plugin, ItemManager.PDC_KEY);
        // String itemId = ItemUtil.getReaperItemId(pdcKey, held);
        // if (itemId != null) { ... trigger ability ... }
    }
}
