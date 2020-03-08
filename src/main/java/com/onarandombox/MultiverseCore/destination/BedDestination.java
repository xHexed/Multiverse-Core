/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * A bed-{@link MVDestination}.
 */
public class BedDestination implements MVDestination {
    public static final String OLD_BED_STRING = "b:playerbed";
    private String playername = "";
    private boolean isValid;
    private Location knownBedLoc;
    private MultiverseCore plugin;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return "b";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThisType(final JavaPlugin plugin, final String destination) {
        final String[] split = destination.split(":");
        final boolean validFormat = split.length >= 1 && split.length <= 2 && split[0].equals(getIdentifier());

        final OfflinePlayer p = Bukkit.getOfflinePlayer(split[1]);
        final boolean validPlayer = true;

        if (validFormat) playername = p.getName();

        isValid = destination.equals(OLD_BED_STRING) || validFormat;

        return isValid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLocation(final Entity entity) {
        if (entity instanceof Player) {
            if (playername.isEmpty())
                knownBedLoc = plugin.getBlockSafety().getSafeBedSpawn(((Player) entity).getBedSpawnLocation());
            else
                knownBedLoc = plugin.getBlockSafety().getSafeBedSpawn(Bukkit.getOfflinePlayer(playername).getBedSpawnLocation());

            if (knownBedLoc == null) {
                entity.sendMessage("The bed was " + ChatColor.RED + "invalid or blocked" + ChatColor.RESET + ". Sorry.");
            }
            return knownBedLoc;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector getVelocity() {
        return new Vector();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestination(final JavaPlugin plugin, final String destination) {
        this.plugin = (MultiverseCore) plugin;
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
    public String getType() {
        return "Bed";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Bed";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequiredPermission() {
        if (knownBedLoc != null) {
            return "multiverse.access." + knownBedLoc.getWorld().getName();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean useSafeTeleporter() {
        // Bukkit should have already checked this.
        return false;
    }

    @Override
    public String toString() {
        return playername.isEmpty() ? OLD_BED_STRING : ("b:" + playername);
    }
}
