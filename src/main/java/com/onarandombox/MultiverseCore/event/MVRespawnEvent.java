/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is respawning.
 */
public class MVRespawnEvent extends Event {
    private final Player player;
    private Location location;
    private final String respawnMethod;


    public MVRespawnEvent(final Location spawningAt, final Player p, final String respawnMethod) {
        player             = p;
        location           = spawningAt;
        this.respawnMethod = respawnMethod;
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
     * Gets the {@link Player} that's respawning.
     * @return The {@link Player} that's respawning.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the respawn-method.
     * @return The respawn-method.
     */
    public String getRespawnMethod() {
        return respawnMethod;
    }

    /**
     * Gets the player's respawn-{@link Location}.
     * @return The player's respawn-{@link Location}.
     */
    public Location getPlayersRespawnLocation() {
        return location;
    }

    /**
     * Sets the player's respawn-{@link Location}.
     *
     * @param l The new respawn-{@link Location}.
     */
    public void setRespawnLocation(final Location l) {
        location = l;
    }
}
