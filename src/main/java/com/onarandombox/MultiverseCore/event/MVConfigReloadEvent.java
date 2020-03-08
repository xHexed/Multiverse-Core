/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called when the Multiverse-config should be reloaded.
 */
public class MVConfigReloadEvent extends Event {
    private final List<String> configsLoaded;

    public MVConfigReloadEvent(final List<String> configsLoaded) {
        this.configsLoaded = configsLoaded;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the handler list. This is required by the event system.
     * @return A list of HANDLERS.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Adds a config to this event.
     *
     * @param config The config to add.
     */
    public void addConfig(final String config) {
        configsLoaded.add(config);
    }

    /**
     * Gets all loaded configs.
     * @return A list of all loaded configs.
     */
    public List<String> getAllConfigsLoaded() {
        return configsLoaded;
    }
}
