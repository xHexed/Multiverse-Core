/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import org.bukkit.ChatColor;

/**
 * A color-scheme.
 */
public class FancyColorScheme {
    private final ChatColor headerColor;
    private final ChatColor mainColor;
    private final ChatColor altColor;
    private final ChatColor defContentColor;

    public FancyColorScheme(final ChatColor header, final ChatColor main, final ChatColor alt, final ChatColor defaultColor) {
        headerColor     = header;
        mainColor       = main;
        altColor        = alt;
        defContentColor = defaultColor;
    }

    /**
     * Gets the header's {@link ChatColor}.
     *
     * @return The header's {@link ChatColor}.
     */
    public ChatColor getHeader() {
        return headerColor;
    }

    /**
     * Gets the main {@link ChatColor}.
     * @return The main {@link ChatColor}.
     */
    public ChatColor getMain() {
        return mainColor;
    }

    /**
     * Gets the alt {@link ChatColor}.
     * @return The alt {@link ChatColor}.
     */
    public ChatColor getAlt() {
        return altColor;
    }

    /**
     * Gets the default {@link ChatColor}.
     * @return The default {@link ChatColor}.
     */
    public ChatColor getDefault() {
        return defContentColor;
    }

    /**
     * Gets either the main or the alt {@link ChatColor}.
     *
     * @param main True if the main-color is desired, false to get the alt color.
     *
     * @return The desired {@link ChatColor}.
     */
    public ChatColor getMain(final boolean main) {
        return main ? mainColor : altColor;
    }
}
