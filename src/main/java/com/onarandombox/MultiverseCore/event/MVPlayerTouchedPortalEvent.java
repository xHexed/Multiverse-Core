/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is thrown when a portal is touched.
 */
public class MVPlayerTouchedPortalEvent extends Event implements Cancellable {
    private final Player p;
    private final Location l;
    private boolean isCancelled;
    private boolean canUse = true;

    public MVPlayerTouchedPortalEvent(final Player p, final Location l) {
        this.p = p;
        this.l = l;
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
     * Gets the {@link Location} of the portal-block that was touched.
     * @return The {@link Location} of the portal-block that was touched.
     */
    public Location getBlockTouched() {
        return l;
    }

    /**
     * Gets the {@link Player} that's touching the portal.
     * @return The {@link Player} that's touching the portal.
     */
    public Player getPlayer() {
        return p;
    }

    /**
     * Gets whether or not the player in this event can use this portal.
     *
     * @return True if the player can use this portal.
     */
    public boolean canUseThisPortal() {
        return canUse;
    }

    /**
     * Sets whether or not the player in this event can use this portal.
     * <p>
     * Setting this to false will cause the player to bounce back!
     *
     * @param canUse Whether or not the user can go through this portal.
     */
    public void setCanUseThisPortal(final boolean canUse) {
        this.canUse = canUse;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(final boolean b) {
        isCancelled = b;
    }
}
