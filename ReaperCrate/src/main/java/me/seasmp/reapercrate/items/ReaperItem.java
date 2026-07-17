package me.seasmp.reapercrate.items;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable model object representing a fully-built ReaperCrate item.
 *
 * <p>Wraps an {@link ItemStack} and its unique string identifier.
 * The internal stack is cloned on construction and again on every
 * call to {@link #asItemStack()}, ensuring the cache is never exposed.</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ReaperItem {

    /** The unique, lowercase identifier for this item (e.g. {@code "soul_sword"}). */
    private final String id;

    /** The cached, fully-configured item stack. Never returned by reference. */
    private final ItemStack itemStack;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new ReaperItem.
     *
     * @param id        the unique string identifier (stored as lowercase)
     * @param itemStack the built {@link ItemStack} (cloned internally)
     */
    public ReaperItem(@NotNull final String id, @NotNull final ItemStack itemStack) {
        this.id        = id.toLowerCase();
        this.itemStack = itemStack.clone();
    }

    // ─── Accessors ───────────────────────────────────────────────────────────

    /**
     * Returns the item's unique identifier.
     *
     * @return the item ID in lowercase
     */
    @NotNull
    public String getId() {
        return id;
    }

    /**
     * Returns a fresh clone of this item's {@link ItemStack}.
     *
     * <p>A new clone is created on every invocation — the internal reference
     * is never exposed directly.</p>
     *
     * @return a cloned ItemStack, safe to mutate
     */
    @NotNull
    public ItemStack asItemStack() {
        return itemStack.clone();
    }

    // ─── Object ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "ReaperItem{id='" + id + "', material=" + itemStack.getType() + '}';
    }
}
