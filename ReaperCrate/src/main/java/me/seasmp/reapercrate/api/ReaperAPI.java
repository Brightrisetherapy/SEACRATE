package me.seasmp.reapercrate.api;

import me.seasmp.reapercrate.items.ItemManager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Public API for the ReaperCrate plugin.
 *
 * <p>External plugins should interact with ReaperCrate <em>exclusively</em>
 * through this class to maintain forward-compatibility as the internal
 * implementation evolves.</p>
 *
 * <p>Obtain an instance via:</p>
 * <pre>{@code
 * ReaperCrate rc = (ReaperCrate) Bukkit.getPluginManager().getPlugin("ReaperCrate");
 * if (rc != null && rc.isEnabled()) {
 *     ReaperAPI api = rc.getAPI();
 *     ItemStack sword = api.getItem("soul_sword");
 * }
 * }</pre>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ReaperAPI {

    private final ItemManager itemManager;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new ReaperAPI backed by the given {@link ItemManager}.
     * This constructor is called internally by the plugin — external code
     * should obtain the instance via {@link me.seasmp.reapercrate.ReaperCrate#getAPI()}.
     *
     * @param itemManager the item manager instance
     */
    public ReaperAPI(@NotNull final ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    // ─── Item Access ─────────────────────────────────────────────────────────

    /**
     * Returns a cloned {@link ItemStack} for the given item ID,
     * or {@code null} if the item does not exist.
     *
     * @param id the item identifier (case-insensitive, e.g. {@code "soul_sword"})
     * @return a fresh clone of the item, or {@code null}
     */
    @Nullable
    public ItemStack getItem(@NotNull final String id) {
        return itemManager.getItem(id);
    }

    /**
     * Returns {@code true} if an item with the given ID is currently registered.
     *
     * @param id the item identifier (case-insensitive)
     * @return {@code true} if the item exists
     */
    public boolean exists(@NotNull final String id) {
        return itemManager.exists(id);
    }

    /**
     * Returns an unmodifiable map of all registered item IDs to cloned ItemStacks.
     *
     * @return unmodifiable map of id → ItemStack
     */
    @NotNull
    public Map<String, ItemStack> getAllItems() {
        return itemManager.getItems();
    }

    /**
     * Returns an unmodifiable collection of all registered item IDs.
     *
     * @return collection of item identifiers in registration order
     */
    @NotNull
    public Collection<String> getItemIds() {
        return itemManager.getItemNames();
    }

    // ─── Item Registration ───────────────────────────────────────────────────

    /**
     * Registers a custom {@link ItemStack} under the given ID at runtime.
     *
     * <p>If an item with the same ID is already registered it is overwritten.
     * Runtime-registered items are <em>not</em> persisted to {@code items.yml}
     * and will be lost on the next call to {@link #reload()}.</p>
     *
     * @param id   the item identifier (stored in lowercase)
     * @param item the item to register
     */
    public void registerItem(@NotNull final String id, @NotNull final ItemStack item) {
        itemManager.register(id, item);
    }

    /**
     * Removes a registered item from the in-memory cache.
     *
     * <p>Items removed here are restored on the next call to {@link #reload()}
     * if their definition still exists in {@code items.yml}.</p>
     *
     * @param id the item identifier (case-insensitive)
     */
    public void unregisterItem(@NotNull final String id) {
        itemManager.unregister(id);
    }

    // ─── Reload ──────────────────────────────────────────────────────────────

    /**
     * Triggers a full reload of items from {@code items.yml}.
     * Equivalent to running {@code /rc reload} in-game.
     */
    public void reload() {
        itemManager.reload();
    }
}
