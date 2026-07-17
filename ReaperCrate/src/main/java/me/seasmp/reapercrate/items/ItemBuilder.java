package me.seasmp.reapercrate.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for creating fully-featured {@link ItemStack} objects.
 *
 * <p>Supports all major Paper 1.21.1 item-meta features including
 * MiniMessage display names and lore, custom model data, enchantments,
 * attribute modifiers, persistent data container, potion effects,
 * leather armour colours, armour trims, and food / tool components.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * ItemStack sword = new ItemBuilder(Material.NETHERITE_SWORD)
 *     .name("<gradient:#800000:#ff0000>Soul Sword")
 *     .lore("<gray>A legendary weapon.")
 *     .customModelData(40001)
 *     .unbreakable(true)
 *     .flag(ItemFlag.HIDE_ATTRIBUTES)
 *     .enchant(Enchantment.SHARPNESS, 10)
 *     .pdc(key, PersistentDataType.STRING, "soul_sword")
 *     .build();
 * }</pre>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ItemBuilder {

    // ─── Constants ───────────────────────────────────────────────────────────

    /** Shared, thread-safe MiniMessage instance. */
    private static final MiniMessage MM = MiniMessage.miniMessage();

    // ─── State ───────────────────────────────────────────────────────────────

    private final ItemStack stack;
    private final ItemMeta  meta;

    // ─── Constructors ────────────────────────────────────────────────────────

    /**
     * Creates a new ItemBuilder for the given material with a stack size of 1.
     *
     * @param material the item material
     */
    public ItemBuilder(@NotNull final Material material) {
        this.stack = new ItemStack(material);
        this.meta  = stack.getItemMeta();
    }

    /**
     * Creates a new ItemBuilder from an existing {@link ItemStack} (cloned).
     * The original stack is not modified.
     *
     * @param base the base item to clone and build upon
     */
    public ItemBuilder(@NotNull final ItemStack base) {
        this.stack = base.clone();
        this.meta  = stack.getItemMeta();
    }

    // ─── Display Name & Lore ─────────────────────────────────────────────────

    /**
     * Sets the display name using a MiniMessage-formatted string.
     *
     * @param miniMessage the MiniMessage string (e.g. {@code "<red>Name"})
     * @return this builder
     */
    @NotNull
    public ItemBuilder name(@NotNull final String miniMessage) {
        meta.displayName(MM.deserialize(miniMessage));
        return this;
    }

    /**
     * Sets the display name using a pre-parsed {@link Component}.
     *
     * @param component the name component
     * @return this builder
     */
    @NotNull
    public ItemBuilder name(@NotNull final Component component) {
        meta.displayName(component);
        return this;
    }

    /**
     * Sets the item lore from an array of MiniMessage strings.
     * Empty strings produce {@link Component#empty()} lines.
     *
     * @param lines MiniMessage-formatted lore lines
     * @return this builder
     */
    @NotNull
    public ItemBuilder lore(@NotNull final String... lines) {
        List<Component> components = new ArrayList<>(lines.length);
        for (String line : lines) {
            components.add(line.isEmpty() ? Component.empty() : MM.deserialize(line));
        }
        meta.lore(components);
        return this;
    }

    /**
     * Sets the item lore from a list of MiniMessage strings.
     *
     * @param lines MiniMessage-formatted lore lines
     * @return this builder
     */
    @NotNull
    public ItemBuilder lore(@NotNull final List<String> lines) {
        List<Component> components = new ArrayList<>(lines.size());
        for (String line : lines) {
            components.add(line.isEmpty() ? Component.empty() : MM.deserialize(line));
        }
        meta.lore(components);
        return this;
    }

    /**
     * Appends additional lore lines to any existing lore.
     *
     * @param lines MiniMessage-formatted lines to append
     * @return this builder
     */
    @NotNull
    public ItemBuilder appendLore(@NotNull final String... lines) {
        List<Component> existing = meta.lore() != null
                ? new ArrayList<>(meta.lore())
                : new ArrayList<>();
        for (String line : lines) {
            existing.add(line.isEmpty() ? Component.empty() : MM.deserialize(line));
        }
        meta.lore(existing);
        return this;
    }

    // ─── Model Data & Durability ─────────────────────────────────────────────

    /**
     * Sets the custom model data integer used for resource-pack model overrides.
     *
     * @param data the custom model data value
     * @return this builder
     */
    @NotNull
    public ItemBuilder customModelData(final int data) {
        meta.setCustomModelData(data);
        return this;
    }

    /**
     * Sets whether the item is unbreakable (immune to durability damage).
     *
     * @param unbreakable {@code true} to prevent durability loss
     * @return this builder
     */
    @NotNull
    public ItemBuilder unbreakable(final boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    /**
     * Sets the item stack size.
     * Clamped to the valid range {@code [1, maxStackSize]}.
     *
     * @param amount the desired stack size
     * @return this builder
     */
    @NotNull
    public ItemBuilder amount(final int amount) {
        stack.setAmount(Math.clamp(amount, 1, stack.getMaxStackSize()));
        return this;
    }

    // ─── Item Flags ──────────────────────────────────────────────────────────

    /**
     * Adds one or more {@link ItemFlag}s to hide tooltip sections.
     *
     * @param flags the flags to add
     * @return this builder
     */
    @NotNull
    public ItemBuilder flag(@NotNull final ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    /**
     * Removes one or more {@link ItemFlag}s from the item.
     *
     * @param flags the flags to remove
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeFlag(@NotNull final ItemFlag... flags) {
        meta.removeItemFlags(flags);
        return this;
    }

    // ─── Enchantments ────────────────────────────────────────────────────────

    /**
     * Adds an enchantment at the specified level, ignoring vanilla level caps.
     *
     * @param enchantment the enchantment to apply
     * @param level       the enchantment level
     * @return this builder
     */
    @NotNull
    public ItemBuilder enchant(@NotNull final Enchantment enchantment, final int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * Removes an enchantment from the item.
     *
     * @param enchantment the enchantment to remove
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeEnchant(@NotNull final Enchantment enchantment) {
        meta.removeEnchant(enchantment);
        return this;
    }

    // ─── Attribute Modifiers ─────────────────────────────────────────────────

    /**
     * Adds an attribute modifier using {@link AttributeModifier.Operation#ADD_NUMBER}
     * applied to the {@link EquipmentSlotGroup#ANY} slot group.
     *
     * @param attribute the attribute to modify
     * @param value     the modifier value
     * @return this builder
     */
    @NotNull
    public ItemBuilder attribute(@NotNull final Attribute attribute, final double value) {
        return attribute(attribute, value,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY);
    }

    /**
     * Adds an attribute modifier on the {@link EquipmentSlotGroup#ANY} slot group.
     *
     * @param attribute the attribute to modify
     * @param value     the modifier value
     * @param operation the modifier operation
     * @return this builder
     */
    @NotNull
    public ItemBuilder attribute(@NotNull final Attribute attribute,
                                 final double value,
                                 @NotNull final AttributeModifier.Operation operation) {
        return attribute(attribute, value, operation, EquipmentSlotGroup.ANY);
    }

    /**
     * Adds an attribute modifier with full control over operation and slot group.
     *
     * <p>A deterministic {@link NamespacedKey} is generated from the attribute's enum
     * name so that repeated builds produce the same modifier key.</p>
     *
     * @param attribute the attribute to modify
     * @param value     the modifier value
     * @param operation the modifier operation
     * @param slotGroup the equipment slot group the modifier applies to
     * @return this builder
     */
    @NotNull
    public ItemBuilder attribute(@NotNull final Attribute attribute,
                                 final double value,
                                 @NotNull final AttributeModifier.Operation operation,
                                 @NotNull final EquipmentSlotGroup slotGroup) {
        NamespacedKey key = new NamespacedKey("reapercrate", attribute.name().toLowerCase());
        AttributeModifier modifier = new AttributeModifier(key, value, operation, slotGroup);
        meta.addAttributeModifier(attribute, modifier);
        return this;
    }

    // ─── Persistent Data Container ───────────────────────────────────────────

    /**
     * Stores an arbitrary value in the item's {@link org.bukkit.persistence.PersistentDataContainer}.
     *
     * @param key   the namespaced key
     * @param type  the PDC type token
     * @param value the value to store
     * @param <T>   the primary (NBT) type
     * @param <Z>   the retrieve (Java) type
     * @return this builder
     */
    @NotNull
    public <T, Z> ItemBuilder pdc(@NotNull final NamespacedKey key,
                                  @NotNull final PersistentDataType<T, Z> type,
                                  @NotNull final Z value) {
        meta.getPersistentDataContainer().set(key, type, value);
        return this;
    }

    // ─── Potion ──────────────────────────────────────────────────────────────

    /**
     * Sets the base potion type.
     * No-op if the item's meta is not a {@link PotionMeta}.
     *
     * @param type the potion type
     * @return this builder
     */
    @NotNull
    public ItemBuilder potionType(@NotNull final PotionType type) {
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setBasePotionType(type);
        }
        return this;
    }

    /**
     * Adds a custom potion effect.
     * No-op if the item's meta is not a {@link PotionMeta}.
     *
     * @param effect    the potion effect to add
     * @param overwrite whether to overwrite an existing effect of the same type
     * @return this builder
     */
    @NotNull
    public ItemBuilder potionEffect(@NotNull final PotionEffect effect,
                                    final boolean overwrite) {
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.addCustomEffect(effect, overwrite);
        }
        return this;
    }

    /**
     * Sets the potion colour override.
     * No-op if the item's meta is not a {@link PotionMeta}.
     *
     * @param color the colour to apply
     * @return this builder
     */
    @NotNull
    public ItemBuilder potionColor(@NotNull final Color color) {
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setColor(color);
        }
        return this;
    }

    // ─── Leather Armour ──────────────────────────────────────────────────────

    /**
     * Sets the leather armour dye colour.
     * No-op if the item's meta is not a {@link LeatherArmorMeta}.
     *
     * @param color the dye colour
     * @return this builder
     */
    @NotNull
    public ItemBuilder leatherColor(@NotNull final Color color) {
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        }
        return this;
    }

    // ─── Armour Trim ─────────────────────────────────────────────────────────

    /**
     * Applies an armour trim to the item.
     * No-op if the item's meta is not an {@link ArmorMeta}.
     *
     * @param pattern  the trim pattern
     * @param material the trim material
     * @return this builder
     */
    @NotNull
    public ItemBuilder trim(@NotNull final TrimPattern pattern,
                            @NotNull final TrimMaterial material) {
        if (meta instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(new ArmorTrim(material, pattern));
        }
        return this;
    }

    // ─── Food Component ──────────────────────────────────────────────────────

    /**
     * Configures the food component, making this item edible.
     *
     * @param nutrition    saturation points restored
     * @param saturation   saturation multiplier
     * @param canAlwaysEat whether the item can be eaten at full hunger
     * @return this builder
     */
    @NotNull
    public ItemBuilder food(final int nutrition,
                            final float saturation,
                            final boolean canAlwaysEat) {
        try {
            var food = meta.getFood();
            food.setNutrition(nutrition);
            food.setSaturation(saturation);
            food.setCanAlwaysEat(canAlwaysEat);
            meta.setFood(food);
        } catch (UnsupportedOperationException ignored) {
            // Meta type does not support the food component.
        }
        return this;
    }

    // ─── Tool Component ──────────────────────────────────────────────────────

    /**
     * Sets the default mining speed for this tool item.
     *
     * @param speed the mining speed multiplier
     * @return this builder
     */
    @NotNull
    public ItemBuilder toolDefaultSpeed(final float speed) {
        try {
            if (meta.hasTool()) {
                var tool = meta.getTool();
                tool.setDefaultMiningSpeed(speed);
                meta.setTool(tool);
            }
        } catch (UnsupportedOperationException ignored) {
            // Meta type does not support the tool component.
        }
        return this;
    }

    /**
     * Sets the durability damage dealt per block mined.
     *
     * @param damage damage per block broken
     * @return this builder
     */
    @NotNull
    public ItemBuilder toolDamagePerBlock(final int damage) {
        try {
            if (meta.hasTool()) {
                var tool = meta.getTool();
                tool.setDamagePerBlock(damage);
                meta.setTool(tool);
            }
        } catch (UnsupportedOperationException ignored) {
            // Meta type does not support the tool component.
        }
        return this;
    }

    // ─── Build ───────────────────────────────────────────────────────────────

    /**
     * Applies all pending modifications and returns the finished {@link ItemStack}.
     *
     * <p>The returned stack is a clone — calling {@code build()} multiple times
     * yields independent copies.</p>
     *
     * @return a new, fully-configured ItemStack
     */
    @NotNull
    public ItemStack build() {
        stack.setItemMeta(meta);
        return stack.clone();
    }
}
