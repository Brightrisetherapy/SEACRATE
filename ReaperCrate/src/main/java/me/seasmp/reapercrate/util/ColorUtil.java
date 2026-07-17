package me.seasmp.reapercrate.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Stateless utility class for colour and text formatting conversions.
 *
 * <p>Provides helpers for MiniMessage, legacy {@code &}-codes, hex colour
 * parsing, and stripping. All methods are thread-safe.</p>
 *
 * @author SeaSmp
 * @since 1.0.0
 */
public final class ColorUtil {

    // ─── Shared Instances ────────────────────────────────────────────────────

    /** Shared MiniMessage instance (stateless / thread-safe). */
    private static final MiniMessage MM = MiniMessage.miniMessage();

    /** Legacy {@code &}-code serializer. */
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    /** Pattern matching a standalone {@code #RRGGBB} hex colour. */
    private static final Pattern HEX_PATTERN =
            Pattern.compile("^#([A-Fa-f0-9]{6})$");

    // Prevent instantiation.
    private ColorUtil() {}

    // ─── MiniMessage ─────────────────────────────────────────────────────────

    /**
     * Parses a MiniMessage-formatted string into a {@link Component}.
     *
     * @param input the MiniMessage string
     * @return the parsed component
     */
    @NotNull
    public static Component miniMessage(@NotNull final String input) {
        return MM.deserialize(input);
    }

    /**
     * Serialises a {@link Component} back to its MiniMessage representation.
     *
     * @param component the component to serialise
     * @return MiniMessage string
     */
    @NotNull
    public static String toMiniMessage(@NotNull final Component component) {
        return MM.serialize(component);
    }

    // ─── Legacy ──────────────────────────────────────────────────────────────

    /**
     * Parses a legacy {@code &}-coded string into a {@link Component}.
     *
     * @param input the legacy-coded string
     * @return the parsed component
     */
    @NotNull
    public static Component legacy(@NotNull final String input) {
        return LEGACY.deserialize(input);
    }

    /**
     * Strips all MiniMessage tags and returns plain text.
     *
     * @param input the input string
     * @return the plain-text string with all tags removed
     */
    @NotNull
    public static String stripColor(@NotNull final String input) {
        return MM.stripTags(input);
    }

    // ─── Hex Colour ──────────────────────────────────────────────────────────

    /**
     * Parses a CSS-style hex colour string into a Bukkit {@link Color}.
     *
     * @param hex the hex colour, with or without a leading {@code #}
     *            (e.g. {@code "#FF0000"} or {@code "FF0000"})
     * @return the Bukkit Color
     * @throws IllegalArgumentException if the string is not a valid 6-digit hex colour
     */
    @NotNull
    public static Color parseHex(@NotNull final String hex) {
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        if (clean.length() != 6) {
            throw new IllegalArgumentException("Invalid hex colour (expected 6 digits): " + hex);
        }
        try {
            int r = Integer.parseInt(clean.substring(0, 2), 16);
            int g = Integer.parseInt(clean.substring(2, 4), 16);
            int b = Integer.parseInt(clean.substring(4, 6), 16);
            return Color.fromRGB(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex colour: " + hex, e);
        }
    }

    /**
     * Converts a Bukkit {@link Color} to a CSS-style hex string ({@code #RRGGBB}).
     *
     * @param color the color to convert
     * @return the uppercase hex string, e.g. {@code "#FF0000"}
     */
    @NotNull
    public static String toHex(@NotNull final Color color) {
        return String.format("#%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Returns {@code true} if the given string is a valid {@code #RRGGBB} hex colour.
     *
     * @param input the string to test (must include the leading {@code #})
     * @return {@code true} if valid
     */
    public static boolean isValidHex(@NotNull final String input) {
        return HEX_PATTERN.matcher(input).matches();
    }
}
