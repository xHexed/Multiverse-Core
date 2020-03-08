/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.api.FancyText;

/**
 * A colored text-message.
 */
public class FancyMessage implements FancyText {
    private final String title;
    private final String message;
    private boolean main = true;
    private final FancyColorScheme colors;

    /**
     * Allows easy creation of an alternating colored list.
     * TODO: Documentation! Why does CheckStyle just ignore this?
     *
     * @param title   The title.
     * @param message The body of the message.
     * @param scheme  The color scheme to use for easy styling.
     */
    public FancyMessage(final String title, final String message, final FancyColorScheme scheme) {
        this.title   = title;
        this.message = message;
        colors       = scheme;
    }

    /**
     * Makes this {@link FancyMessage} use the main-color.
     */
    public void setColorMain() {
        main = true;
    }

    /**
     * Makes this {@link FancyMessage} use the alt-color.
     */
    public void setColorAlt() {
        main = false;
    }

    @Override
    public String getFancyText() {
        return colors.getMain(main) + title + colors.getDefault() + message;
    }

    /**
     * Specifies whether this {@link FancyMessage} should use the alt-color.
     *
     * @param altColor Whether this {@link FancyMessage} should use the alt-color.
     */
    public void setAltColor(final boolean altColor) {
        main = !altColor;
    }

    /**
     * Specifies whether this {@link FancyMessage} should use the main-color.
     *
     * @param mainColor Whether this {@link FancyMessage} should use the main-color.
     */
    public void setMainColor(final boolean mainColor) {
        main = mainColor;
    }
}
