/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.api.MVDestination;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * A player-{@link MVDestination}.
 */
public class PlayerDestination implements MVDestination {
    private String player;
    private boolean isValid;
    private JavaPlugin plugin;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return "pl";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThisType(final JavaPlugin plugin, final String destination) {
        final String[] items = destination.split(":");
        if (items.length != 2) {
            return false;
        }
        return items[0].equalsIgnoreCase("pl");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLocation(final Entity e) {
        final Player p = plugin.getServer().getPlayer(player);
        Player plLoc = null;
        if (e instanceof Player) {
            plLoc = (Player) e;
        }
        else if (e.getPassenger() instanceof Player) {
            plLoc = (Player) e.getPassenger();
        }

        if (p != null && plLoc != null) {
            return p.getLocation();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return isValid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestination(final JavaPlugin plugin, final String destination) {
        final String[] items = destination.split(":");
        if (items.length != 2) {
            isValid = false;
        }
        if (!items[0].equalsIgnoreCase("pl")) {
            isValid = false;
        }
        isValid     = true;
        player      = items[1];
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "Player";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return player;
    }

    @Override
    public String toString() {
        return "pl:" + player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequiredPermission() {
        return "";
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
        return true;
    }

}
