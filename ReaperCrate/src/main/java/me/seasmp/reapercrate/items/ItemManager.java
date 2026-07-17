package me.seasmp.reapercrate.items;

import me.seasmp.reapercrate.ReaperCrate;
import me.seasmp.reapercrate.managers.ItemFileManager;
import me.seasmp.reapercrate.util.ColorUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages loading, caching, and retrieval of all ReaperCrate custom items.
 *
 * <p>Items are loaded from {@code items.yml} once on startup and cached in an
 * internal {@link HashMap}. The cache is cleared and rebuilt only on explicit
 * calls to {@link #reload()}. Every public getter returns a <em>clone</em> of
 * the stored stack — internal references are never exposed.</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ItemManager {

    // ─── PDC Key ─────────────────────────────────────────────────────────────

    /**
     * The PDC key name used to stamp every built item with its item ID.
     * Used externally by {@link me.seasmp.reapercrate.util.ItemUtil} to identify items.
     */
    public static final String PDC_KEY = "reaper_item";

    // ─── Dependencies ────────────────────────────────────────────────────────

    private final ReaperCrate     plugin;
    private final ItemFileManager fileManager;

    // ─── Cache ───────────────────────────────────────────────────────────────

    /** Internal item cache: lowercase ID → ReaperItem model. */
    private final Map<String, ReaperItem> items = new LinkedHashMap<>();

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new ItemManager.
     *
     * @param plugin      the plugin instance
     * @param fileManager the item file manager that provides {@code items.yml} access
     */
    public ItemManager(@NotNull final ReaperCrate plugin,
                       @NotNull final ItemFileManager fileManager) {
        this.plugin      = plugin;
        this.fileManager = fileManager;
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Loads (or reloads) all items from {@code items.yml} into the cache.
     * Any previously cached items are discarded before loading.
     */
    public void loadItems() {
        items.clear();

        FileConfiguration config  = fileManager.getConfig();
        ConfigurationSection root = config.getConfigurationSection("items");

        if (root == null) {
            plugin.getLogger().warning("[ItemManager] No 'items' section found in items.yml.");
            return;
        }

        int loaded = 0;
        int failed = 0;

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) continue;

            try {
                ItemStack stack = buildItem(id.toLowerCase(), section);
                items.put(id.toLowerCase(), new ReaperItem(id.toLowerCase(), stack));
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "[ItemManager] Failed to load item '" + id + "': " + e.getMessage(), e);
                failed++;
            }
        }

        plugin.getLogger().info("[ItemManager] Loaded " + loaded + " item(s)."
                + (failed > 0 ? " " + failed + " failed." : ""));
    }

    /**
     * Reloads all items from disk. Equivalent to calling {@link #loadItems()}.
     */
    public void reload() {
        loadItems();
    }

    /**
     * Returns a cloned {@link ItemStack} for the given item ID,
     * or {@code null} if the item is not registered.
     *
     * @param id the item identifier (case-insensitive)
     * @return a cloned ItemStack, or {@code null}
     */
    @Nullable
    public ItemStack getItem(@NotNull final String id) {
        ReaperItem item = items.get(id.toLowerCase());
        return item != null ? item.asItemStack() : null;
    }

    /**
     * Returns {@code true} if an item with the given ID is registered.
     *
     * @param id the item identifier (case-insensitive)
     * @return {@code true} if the item exists
     */
    public boolean exists(@NotNull final String id) {
        return items.containsKey(id.toLowerCase());
    }

    /**
     * Returns an unmodifiable map of all registered item IDs to cloned ItemStacks.
     *
     * @return unmodifiable, insertion-ordered map
     */
    @NotNull
    public Map<String, ItemStack> getItems() {
        Map<String, ItemStack> result = new LinkedHashMap<>();
        items.forEach((k, v) -> result.put(k, v.asItemStack()));
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns an unmodifiable, insertion-ordered set of all registered item IDs.
     *
     * @return set of item identifiers
     */
    @NotNull
    public Set<String> getItemNames() {
        return Collections.unmodifiableSet(items.keySet());
    }

    /**
     * Manually registers a custom {@link ItemStack} under the given ID.
     * If an item with the same ID already exists, it is overwritten.
     *
     * @param id   the item identifier (stored in lowercase)
     * @param item the item to register
     */
    public void register(@NotNull final String id, @NotNull final ItemStack item) {
        items.put(id.toLowerCase(), new ReaperItem(id.toLowerCase(), item));
    }

    /**
     * Removes a registered item from the cache by ID.
     *
     * @param id the item identifier (case-insensitive)
     */
    public void unregister(@NotNull final String id) {
        items.remove(id.toLowerCase());
    }

    // ─── Item Building ───────────────────────────────────────────────────────

    /**
     * Builds a complete {@link ItemStack} from a YAML {@link ConfigurationSection}.
     *
     * @param id      the lowercase item identifier (used for PDC tagging and logs)
     * @param section the YAML section for this item
     * @return the fully configured ItemStack
     * @throws IllegalArgumentException if the material name is invalid
     */
    @NotNull
    private ItemStack buildItem(@NotNull final String id,
                                @NotNull final ConfigurationSection section) {

        // ── Material ──────────────────────────────────────────────────────
        String materialName = section.getString("material", "STONE");
        Material material   = Material.matchMaterial(materialName.toUpperCase());
        if (material == null) {
            throw new IllegalArgumentException("Unknown material: " + materialName);
        }

        ItemBuilder builder = new ItemBuilder(material);

        // ── Display Name ──────────────────────────────────────────────────
        if (section.contains("display-name")) {
            builder.name(section.getString("display-name", ""));
        }

        // ── Lore ──────────────────────────────────────────────────────────
        if (section.contains("lore")) {
            builder.lore(section.getStringList("lore"));
        }

        // ── Custom Model Data ─────────────────────────────────────────────
        if (section.contains("custom-model-data")) {
            builder.customModelData(section.getInt("custom-model-data"));
        }

        // ── Amount ────────────────────────────────────────────────────────
        if (section.contains("amount")) {
            builder.amount(section.getInt("amount", 1));
        }

        // ── Unbreakable ───────────────────────────────────────────────────
        if (section.getBoolean("unbreakable", false)) {
            builder.unbreakable(true);
        }

        // ── Item Flags ────────────────────────────────────────────────────
        if (section.contains("flags")) {
            for (String flagName : section.getStringList("flags")) {
                try {
                    builder.flag(ItemFlag.valueOf(flagName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[ItemManager] Unknown flag '"
                            + flagName + "' on '" + id + "' — skipped.");
                }
            }
        }

        // ── Enchantments ──────────────────────────────────────────────────
        if (section.contains("enchants")) {
            ConfigurationSection enchantSec = section.getConfigurationSection("enchants");
            if (enchantSec != null) {
                for (String enchantKey : enchantSec.getKeys(false)) {
                    Enchantment enchant = Registry.ENCHANTMENT.get(
                            NamespacedKey.minecraft(enchantKey.toLowerCase()));
                    if (enchant != null) {
                        builder.enchant(enchant, enchantSec.getInt(enchantKey));
                    } else {
                        plugin.getLogger().warning("[ItemManager] Unknown enchantment '"
                                + enchantKey + "' on '" + id + "' — skipped.");
                    }
                }
            }
        }

        // ── Attribute Modifiers ───────────────────────────────────────────
        if (section.contains("attributes")) {
            ConfigurationSection attrSec = section.getConfigurationSection("attributes");
            if (attrSec != null) {
                EquipmentSlotGroup slotGroup = resolveSlotGroup(material);
                for (String attrKey : attrSec.getKeys(false)) {
                    Attribute attribute = resolveAttribute(attrKey);
                    if (attribute != null) {
                        builder.attribute(attribute, attrSec.getDouble(attrKey),
                                AttributeModifier.Operation.ADD_NUMBER, slotGroup);
                    } else {
                        plugin.getLogger().warning("[ItemManager] Unknown attribute '"
                                + attrKey + "' on '" + id + "' — skipped.");
                    }
                }
            }
        }

        // ── Leather Colour ────────────────────────────────────────────────
        if (section.contains("leather-color")) {
            try {
                Color color = ColorUtil.parseHex(
                        section.getString("leather-color", "#A06540"));
                builder.leatherColor(color);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[ItemManager] Invalid leather-color on '"
                        + id + "': " + e.getMessage());
            }
        }

        // ── Potion Type ───────────────────────────────────────────────────
        if (section.contains("potion-type")) {
            String potionName = section.getString("potion-type", "");
            try {
                PotionType potionType = PotionType.valueOf(potionName.toUpperCase());
                builder.potionType(potionType);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[ItemManager] Unknown potion-type '"
                        + potionName + "' on '" + id + "' — skipped.");
            }
        }

        // ── Potion Colour ─────────────────────────────────────────────────
        if (section.contains("potion-color")) {
            try {
                Color color = ColorUtil.parseHex(
                        section.getString("potion-color", "#FF00FF"));
                builder.potionColor(color);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[ItemManager] Invalid potion-color on '"
                        + id + "': " + e.getMessage());
            }
        }

        // ── Armour Trim ───────────────────────────────────────────────────
        if (section.contains("trim")) {
            ConfigurationSection trimSec = section.getConfigurationSection("trim");
            if (trimSec != null) {
                String patternName  = trimSec.getString("pattern", "");
                String materialName2 = trimSec.getString("material", "");
                TrimPattern pattern  = Registry.TRIM_PATTERN.get(
                        NamespacedKey.minecraft(patternName.toLowerCase()));
                TrimMaterial trimMat = Registry.TRIM_MATERIAL.get(
                        NamespacedKey.minecraft(materialName2.toLowerCase()));
                if (pattern != null && trimMat != null) {
                    builder.trim(pattern, trimMat);
                } else {
                    plugin.getLogger().warning("[ItemManager] Invalid trim on '"
                            + id + "': pattern=" + patternName
                            + ", material=" + materialName2);
                }
            }
        }

        // ── Food Component ────────────────────────────────────────────────
        if (section.contains("food")) {
            ConfigurationSection foodSec = section.getConfigurationSection("food");
            if (foodSec != null) {
                builder.food(
                        foodSec.getInt("nutrition", 2),
                        (float) foodSec.getDouble("saturation", 0.1),
                        foodSec.getBoolean("can-always-eat", false)
                );
            }
        }

        // ── PDC Stamp — tag every item with its ID ────────────────────────
        NamespacedKey pdcKey = new NamespacedKey(plugin, PDC_KEY);
        builder.pdc(pdcKey, PersistentDataType.STRING, id);

        return builder.build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Maps a YAML attribute key (e.g. {@code "attack_damage"}) to a
     * Bukkit {@link Attribute} enum constant.
     *
     * @param key the YAML key (case-insensitive)
     * @return the matching attribute, or {@code null} if unknown
     */
    @Nullable
    private Attribute resolveAttribute(@NotNull final String key) {
        return switch (key.toLowerCase()) {
            case "max_health"                -> Attribute.GENERIC_MAX_HEALTH;
            case "follow_range"              -> Attribute.GENERIC_FOLLOW_RANGE;
            case "knockback_resistance"      -> Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            case "movement_speed"            -> Attribute.GENERIC_MOVEMENT_SPEED;
            case "flying_speed"              -> Attribute.GENERIC_FLYING_SPEED;
            case "attack_damage"             -> Attribute.GENERIC_ATTACK_DAMAGE;
            case "attack_knockback"          -> Attribute.GENERIC_ATTACK_KNOCKBACK;
            case "attack_speed"              -> Attribute.GENERIC_ATTACK_SPEED;
            case "armor"                     -> Attribute.GENERIC_ARMOR;
            case "armor_toughness"           -> Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "luck"                      -> Attribute.GENERIC_LUCK;
            case "max_absorption"            -> Attribute.GENERIC_MAX_ABSORPTION;
            case "scale"                     -> Attribute.GENERIC_SCALE;
            case "step_height"               -> Attribute.GENERIC_STEP_HEIGHT;
            case "gravity"                   -> Attribute.GENERIC_GRAVITY;
            case "safe_fall_distance"        -> Attribute.GENERIC_SAFE_FALL_DISTANCE;
            case "fall_damage_multiplier"    -> Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER;
            case "jump_strength"             -> Attribute.GENERIC_JUMP_STRENGTH;
            case "block_break_speed"         -> Attribute.PLAYER_BLOCK_BREAK_SPEED;
            case "block_interaction_range"   -> Attribute.PLAYER_BLOCK_INTERACTION_RANGE;
            case "entity_interaction_range"  -> Attribute.PLAYER_ENTITY_INTERACTION_RANGE;
            case "mining_efficiency"         -> Attribute.PLAYER_MINING_EFFICIENCY;
            case "sneaking_speed"            -> Attribute.PLAYER_SNEAKING_SPEED;
            case "submerged_mining_speed"    -> Attribute.PLAYER_SUBMERGED_MINING_SPEED;
            case "sweeping_damage_ratio"     -> Attribute.PLAYER_SWEEPING_DAMAGE_RATIO;
            case "zombie_spawn_reinforcements" -> Attribute.ZOMBIE_SPAWN_REINFORCEMENTS;
            default                          -> null;
        };
    }

    /**
     * Determines the most appropriate {@link EquipmentSlotGroup} for attribute
     * modifiers based on the item's material name suffix.
     *
     * @param material the item material
     * @return the slot group
     */
    @NotNull
    private EquipmentSlotGroup resolveSlotGroup(@NotNull final Material material) {
        String name = material.name();
        if (name.endsWith("_HELMET"))     return EquipmentSlotGroup.HEAD;
        if (name.endsWith("_CHESTPLATE")) return EquipmentSlotGroup.CHEST;
        if (name.endsWith("_LEGGINGS"))   return EquipmentSlotGroup.LEGS;
        if (name.endsWith("_BOOTS"))      return EquipmentSlotGroup.FEET;
        if (name.endsWith("_SWORD")
                || name.endsWith("_AXE")
                || name.endsWith("_PICKAXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE")
                || name.endsWith("_BOW")
                || name.endsWith("_CROSSBOW")
                || name.endsWith("_TRIDENT")) return EquipmentSlotGroup.MAINHAND;
        return EquipmentSlotGroup.ANY;
    }
}