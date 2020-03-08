/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.event;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that gets called when a player use the /mvtp command.
 */
public class MVTeleportEvent extends Event implements Cancellable {
    private final Player teleportee;
    private final CommandSender teleporter;
    private final MVDestination dest;
    private final boolean useSafeTeleport;
    private boolean isCancelled;

    public MVTeleportEvent(final MVDestination dest, final Player teleportee, final CommandSender teleporter, final boolean safeTeleport) {
        this.teleportee = teleportee;
        this.teleporter = teleporter;
        this.dest       = dest;
        useSafeTeleport = safeTeleport;
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
     * Returns the player who will be teleported by this event.
     *
     * @return The player who will be teleported by this event.
     */
    public Player getTeleportee() {
        return teleportee;
    }

    /**
     * Returns the location the player was before the teleport.
     *
     * @return The location the player was before the teleport.
     */
    public Location getFrom() {
        return teleportee.getLocation();
    }

    /**
     * Gets the {@link CommandSender} who requested the Teleport.
     *
     * @return The {@link CommandSender} who requested the Teleport
     */
    public CommandSender getTeleporter() {
        return teleporter;
    }

    /**
     * Returns the destination that the player will spawn at.
     *
     * @return The destination the player will spawn at.
     */
    public MVDestination getDestination() {
        return dest;
    }

    /**
     * Looks if this {@link MVTeleportEvent} is using the {@link SafeTTeleporter}.
     * @return True if this {@link MVTeleportEvent} is using the {@link SafeTTeleporter}.
     */
    public boolean isUsingSafeTTeleporter() {
        return useSafeTeleport;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        isCancelled = cancel;
    }
}
