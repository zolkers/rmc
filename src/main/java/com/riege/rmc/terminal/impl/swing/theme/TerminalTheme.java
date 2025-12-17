package com.riege.rmc.terminal.impl.swing.theme;

import java.awt.*;

/**
 * Modern dark theme for the terminal.
 * Inspired by VS Code Dark+ and JetBrains Darcula.
 */
public final class TerminalTheme {

    // Background colors
    public static final Color BACKGROUND_PRIMARY = new Color(30, 30, 30);      // Main background
    public static final Color BACKGROUND_SECONDARY = new Color(40, 40, 40);    // Input field background
    public static final Color BACKGROUND_HOVER = new Color(50, 50, 50);        // Hover states

    // Foreground colors
    public static final Color TEXT_PRIMARY = new Color(204, 204, 204);         // Main text
    public static final Color TEXT_SECONDARY = new Color(150, 150, 150);       // Secondary text
    public static final Color TEXT_MUTED = new Color(100, 100, 100);           // Muted text

    // Accent colors
    public static final Color ACCENT_PRIMARY = new Color(0, 122, 204);         // Blue accent
    public static final Color ACCENT_SUCCESS = new Color(80, 200, 120);        // Green
    public static final Color ACCENT_WARNING = new Color(255, 193, 7);         // Amber
    public static final Color ACCENT_ERROR = new Color(244, 67, 54);           // Red
    public static final Color ACCENT_INFO = new Color(33, 150, 243);           // Light blue
    public static final Color ACCENT_DEBUG = new Color(156, 39, 176);          // Purple

    // Link colors
    public static final Color LINK_DEFAULT = new Color(79, 192, 255);          // Bright blue
    public static final Color LINK_HOVER = new Color(120, 210, 255);           // Lighter blue

    // Border colors
    public static final Color BORDER_DEFAULT = new Color(60, 60, 60);
    public static final Color BORDER_FOCUS = new Color(0, 122, 204);

    // Selection colors
    public static final Color SELECTION_BACKGROUND = new Color(51, 153, 255, 80);
    public static final Color SELECTION_TEXT = new Color(255, 255, 255);

    // Fonts
    public static final Font FONT_MONO = new Font("JetBrains Mono", Font.PLAIN, 13);
    public static final Font FONT_MONO_BOLD = new Font("JetBrains Mono", Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);

    // Font fallbacks if JetBrains Mono is not available
    public static Font getMonoFont() {
        Font jetBrains = new Font("JetBrains Mono", Font.PLAIN, 13);
        if (!jetBrains.getFamily().equals("JetBrains Mono")) {
            // Fallback to common monospace fonts
            if (new Font("Consolas", Font.PLAIN, 13).getFamily().equals("Consolas")) {
                return new Font("Consolas", Font.PLAIN, 13);
            } else if (new Font("Monaco", Font.PLAIN, 13).getFamily().equals("Monaco")) {
                return new Font("Monaco", Font.PLAIN, 13);
            } else {
                return new Font("Monospaced", Font.PLAIN, 13);
            }
        }
        return jetBrains;
    }

    public static Font getMonoFontBold() {
        Font base = getMonoFont();
        return base.deriveFont(Font.BOLD);
    }

    // Spacing
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 12;
    public static final int PADDING_LARGE = 16;

    // Border radius
    public static final int BORDER_RADIUS = 4;

    private TerminalTheme() {
        // Utility class
    }
}
