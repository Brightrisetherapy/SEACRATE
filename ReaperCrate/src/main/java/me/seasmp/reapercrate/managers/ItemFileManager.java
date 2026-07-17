package me.seasmp.reapercrate.managers;

import me.seasmp.reapercrate.ReaperCrate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Manages the {@code items.yml} file that defines all custom ReaperCrate items.
 *
 * <p>On first run, the bundled default {@code items.yml} is extracted from the
 * plugin jar to the data folder so server operators have a ready-made example.
 * The file is then loaded into a {@link FileConfiguration} accessible via
 * {@link #getConfig()}.</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ItemFileManager {

    private final ReaperCrate plugin;
    private File              file;
    private FileConfiguration config;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates a new ItemFileManager.
     *
     * @param plugin the plugin instance
     */
    public ItemFileManager(@NotNull final ReaperCrate plugin) {
        this.plugin = plugin;
    }

    // ─── Setup / Load ────────────────────────────────────────────────────────

    /**
     * Initialises the {@code items.yml} file, extracting the default from the
     * jar if it does not already exist. Must be called once during startup.
     */
    public void setup() {
        file = new File(plugin.getDataFolder(), "items.yml");

        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("items.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Reloads {@code items.yml} from disk.
     * If {@link #setup()} has not been called yet, it is called implicitly.
     */
    public void reload() {
        if (file == null) {
            setup();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    // ─── Access ──────────────────────────────────────────────────────────────

    /**
     * Returns the loaded {@link FileConfiguration} for {@code items.yml}.
     *
     * @return the items configuration
     */
    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Saves the current in-memory configuration back to {@code items.yml}.
     *
     * <p>Rarely needed in practice — {@code items.yml} is operator-edited,
     * not written to by the plugin. Provided for completeness.</p>
     */
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "[ItemFileManager] Could not save items.yml: " + e.getMessage());
        }
    }
}
