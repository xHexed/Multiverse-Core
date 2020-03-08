/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.api.MVDestination;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * An invalid {@link MVDestination}.
 */
public class InvalidDestination implements MVDestination {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return "i";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThisType(final JavaPlugin plugin, final String destination) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLocation(final Entity e) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestination(final JavaPlugin plugin, final String destination) {
        // Nothing needed, it's invalid.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return ChatColor.RED + "Invalid Destination";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return ChatColor.RED + "Invalid Destination";
    }

    @Override
    public String toString() {
        return "i:Invalid Destination";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequiredPermission() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector getVelocity() {
        return new Vector(0, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean useSafeTeleporter() {
        return false;
    }

}
