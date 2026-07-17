package me.seasmp.reapercrate;

import me.seasmp.reapercrate.api.ReaperAPI;
import me.seasmp.reapercrate.commands.ReaperCrateCommand;
import me.seasmp.reapercrate.items.ItemManager;
import me.seasmp.reapercrate.listeners.InventoryListener;
import me.seasmp.reapercrate.listeners.JoinListener;
import me.seasmp.reapercrate.listeners.PlayerListener;
import me.seasmp.reapercrate.managers.ConfigManager;
import me.seasmp.reapercrate.managers.ItemFileManager;
import me.seasmp.reapercrate.managers.ResourcePackManager;
import me.seasmp.reapercrate.util.MessageUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Main entry point for the <b>ReaperCrate</b> plugin.
 *
 * <p>ReaperCrate is a production-ready Paper plugin that replaces Oraxen /
 * ItemsAdder for the Soul Reaper resource pack by generating custom
 * {@link org.bukkit.inventory.ItemStack}s entirely from YAML definitions,
 * without NMS, reflection, or deprecated API.</p>
 *
 * <p>Obtain the plugin instance and its public API via:</p>
 * <pre>{@code
 * ReaperCrate plugin = (ReaperCrate) Bukkit.getPluginManager().getPlugin("ReaperCrate");
 * ReaperAPI api = plugin.getAPI();
 * }</pre>
 *
 * @author SeaSmp
 * @version 1.0.0
 */
public final class ReaperCrate extends JavaPlugin {

    // ─── Singleton ───────────────────────────────────────────────────────────

    /** Singleton instance set on {@link #onEnable()} and cleared on {@link #onDisable()}. */
    private static ReaperCrate instance;

    // ─── Managers ────────────────────────────────────────────────────────────

    private ConfigManager       configManager;
    private ItemFileManager     itemFileManager;
    private ItemManager         itemManager;
    private ResourcePackManager resourcePackManager;
    private MessageUtil         messageUtil;

    // ─── API ─────────────────────────────────────────────────────────────────

    private ReaperAPI api;

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;

        initManagers();
        registerCommands();
        registerListeners();

        api = new ReaperAPI(itemManager);

        logStartup();
    }

    @Override
    public void onDisable() {
        getLogger().info("===========================================");
        getLogger().info("  ReaperCrate v" + getDescription().getVersion() + " disabled.");
        getLogger().info("===========================================");
        instance = null;
    }

    // ─── Initialisation ──────────────────────────────────────────────────────

    /**
     * Constructs and initialises every manager in dependency order.
     */
    private void initManagers() {
        // 1. Config (config.yml)
        configManager = new ConfigManager(this);
        configManager.load();

        // 2. Item file (items.yml)
        itemFileManager = new ItemFileManager(this);
        itemFileManager.setup();

        // 3. Item manager — depends on itemFileManager
        itemManager = new ItemManager(this, itemFileManager);
        itemManager.loadItems();

        // 4. Resource pack — depends on configManager
        resourcePackManager = new ResourcePackManager(this, configManager);

        // 5. Messages (messages.yml)
        messageUtil = new MessageUtil(this);
        messageUtil.load();
    }

    /**
     * Registers all plugin commands and their tab completers.
     */
    private void registerCommands() {
        ReaperCrateCommand executor =
                new ReaperCrateCommand(this, itemManager, messageUtil);

        PluginCommand cmd = getCommand("reapercrate");
        if (cmd != null) {
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        } else {
            getLogger().severe("Command 'reapercrate' is not defined in plugin.yml!");
        }
    }

    /**
     * Registers all event listeners with the Bukkit plugin manager.
     */
    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(this, resourcePackManager, configManager), this);
        pm.registerEvents(new InventoryListener(this, itemManager), this);
        pm.registerEvents(new PlayerListener(this), this);
    }

    // ─── Reload ──────────────────────────────────────────────────────────────

    /**
     * Reloads all configuration files and re-builds the item cache from disk.
     *
     * <p>Called by {@link ReaperCrateCommand} on {@code /rc reload} and by the
     * public {@link ReaperAPI#reload()} method.</p>
     */
    public void reloadPlugin() {
        configManager.reload();
        itemFileManager.reload();
        itemManager.reload();
        messageUtil.reload();

        if (configManager.isDebug()) {
            getLogger().log(Level.INFO, "[DEBUG] Plugin reloaded. "
                    + itemManager.getItemNames().size() + " item(s) loaded.");
        }
    }

    // ─── Startup Logging ─────────────────────────────────────────────────────

    private void logStartup() {
        getLogger().info("===========================================");
        getLogger().info("  ReaperCrate v" + getDescription().getVersion());
        getLogger().info("  Paper " + getServer().getVersion());
        getLogger().info("  Items loaded: " + itemManager.getItemNames().size());
        getLogger().info("  Status: ENABLED");
        getLogger().info("===========================================");
    }

    // ─── Public Accessors ────────────────────────────────────────────────────

    /**
     * Returns the singleton plugin instance.
     *
     * @return the ReaperCrate instance, or {@code null} if not yet enabled
     */
    public static ReaperCrate getInstance() {
        return instance;
    }

    /** @return the public-facing API object */
    @NotNull
    public ReaperAPI getAPI() {
        return api;
    }

    /** @return the config manager */
    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /** @return the item file manager */
    @NotNull
    public ItemFileManager getItemFileManager() {
        return itemFileManager;
    }

    /** @return the item manager */
    @NotNull
    public ItemManager getItemManager() {
        return itemManager;
    }

    /** @return the resource pack manager */
    @NotNull
    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }

    /** @return the message utility */
    @NotNull
    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}