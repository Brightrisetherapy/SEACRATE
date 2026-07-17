package me.seasmp.reapercrate.util;

import me.seasmp.reapercrate.items.ItemManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * General-purpose utility methods for {@link ItemStack} inspection and manipulation.
 *
 * <p>All methods are stateless and no instances should be created.
 * Instantiation is prevented by a private constructor.</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ItemUtil {

    private ItemUtil() {}

    // ─── PDC Inspection ──────────────────────────────────────────────────────

    /**
     * Returns the ReaperCrate item ID stored in the item's
     * {@link PersistentDataContainer}, or {@code null} if the item does not
     * carry a ReaperCrate PDC tag.
     *
     * @param pdcKey the namespaced key used to tag items ({@link ItemManager#PDC_KEY})
     * @param item   the item to inspect (may be {@code null})
     * @return the item ID string, or {@code null}
     */
    @Nullable
    public static String getReaperItemId(@NotNull final NamespacedKey pdcKey,
                                         @Nullable final ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(pdcKey, PersistentDataType.STRING);
    }

    /**
     * Returns {@code true} if the given item carries a ReaperCrate PDC tag.
     *
     * @param pdcKey the namespaced key used to tag items
     * @param item   the item to inspect (may be {@code null})
     * @return {@code true} if the item is a ReaperCrate item
     */
    public static boolean isReaperItem(@NotNull final NamespacedKey pdcKey,
                                       @Nullable final ItemStack item) {
        return getReaperItemId(pdcKey, item) != null;
    }

    // ─── Presence Checks ─────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the item is non-null and non-air.
     *
     * @param item the item to test (may be {@code null})
     * @return {@code true} if the item is a valid, non-air stack
     */
    public static boolean isPresent(@Nullable final ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    // ─── Inventory Helpers ───────────────────────────────────────────────────

    /**
     * Returns the item currently held in the player's main hand,
     * or {@code null} if the player is holding air.
     *
     * @param player the player to inspect
     * @return the held item stack, or {@code null}
     */
    @Nullable
    public static ItemStack getHeldItem(@NotNull final Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        return isPresent(held) ? held : null;
    }

    /**
     * Gives an item to a player. If the player's inventory is full, any overflow
     * is dropped naturally at the player's location.
     *
     * @param player the recipient
     * @param item   the item to give (cloned before insertion)
     */
    public static void giveOrDrop(@NotNull final Player player,
                                  @NotNull final ItemStack item) {
        var leftover = player.getInventory().addItem(item.clone());
        leftover.values().forEach(drop ->
                player.getWorld().dropItemNaturally(player.getLocation(), drop));
    }
}
