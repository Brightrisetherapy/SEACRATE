package me.seasmp.reapercrate.listeners;

import me.seasmp.reapercrate.ReaperCrate;
import me.seasmp.reapercrate.items.ItemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles inventory interaction events involving ReaperCrate items.
 *
 * <p>This listener is intentionally minimal in 1.0.0 and serves as a clean
 * hook point for future inventory-related behaviour such as:</p>
 * <ul>
 *   <li>Custom GUI handling for crate-opening screens</li>
 *   <li>Preventing specific items from being moved or dropped</li>
 *   <li>Triggering effects on shift-click or cursor-swap</li>
 * </ul>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class InventoryListener implements Listener {

    private final ReaperCrate plugin;
    private final ItemManager itemManager;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new InventoryListener.
     *
     * @param plugin      the plugin instance
     * @param itemManager the item manager
     */
    public InventoryListener(@NotNull final ReaperCrate plugin,
                             @NotNull final ItemManager itemManager) {
        this.plugin      = plugin;
        this.itemManager = itemManager;
    }

    // ─── Events ──────────────────────────────────────────────────────────────

    /**
     * Reserved hook for inventory click handling.
     *
     * <p>Currently a no-op. Expand this method to implement per-item click
     * logic by inspecting the PDC tag on the clicked item.</p>
     *
     * @param event the inventory click event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(@NotNull final InventoryClickEvent event) {
        // Example future use:
        // if (event.getCurrentItem() == null) return;
        // NamespacedKey pdcKey = new NamespacedKey(plugin, ItemManager.PDC_KEY);
        // if (ItemUtil.isReaperItem(pdcKey, event.getCurrentItem())) { ... }
    }
}
