/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.enums.TeleportResult;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import java.util.logging.Level;

/**
 * The default-implementation of {@link SafeTTeleporter}.
 */
public class SimpleSafeTTeleporter implements SafeTTeleporter {
    private final MultiverseCore plugin;

    public SimpleSafeTTeleporter(final MultiverseCore plugin) {
        this.plugin = plugin;
    }

    private static final int DEFAULT_TOLERANCE = 6;
    private static final int DEFAULT_RADIUS = 9;

    private static Location getCloserBlock(final Location source, final Location blockA, final Location blockB) {
        // If B wasn't given, return a.
        if (blockB == null) {
            return blockA;
        }
        // Center our calculations
        blockA.add(.5, 0, .5);
        blockB.add(.5, 0, .5);

        // Retrieve the distance to the normalized blocks
        final double testA = source.distance(blockA);
        final double testB = source.distance(blockB);

        // Compare and return
        if (testA <= testB) {
            return blockA;
        }
        return blockB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getSafeLocation(final Location l) {
        return getSafeLocation(l, DEFAULT_TOLERANCE, DEFAULT_RADIUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getSafeLocation(final Location l, final int tolerance, final int radius) {
        // Check around the player first in a configurable radius:
        // TODO: Make this configurable
        final Location safe = checkAboveAndBelowLocation(l, tolerance, radius);
        if (safe != null) {
            safe.setX(safe.getBlockX() + .5); // SUPPRESS CHECKSTYLE: MagicNumberCheck
            safe.setZ(safe.getBlockZ() + .5); // SUPPRESS CHECKSTYLE: MagicNumberCheck
            plugin.log(Level.FINE, "Hey! I found one: " + plugin.getLocationManipulation().strCoordsRaw(safe));
        }
        else {
            plugin.log(Level.FINE, "Uh oh! No safe place found!");
        }
        return safe;
    }

    private Location checkAboveAndBelowLocation(final Location l, int tolerance, final int radius) {
        // Tolerance must be an even number:
        if (tolerance % 2 != 0) {
            tolerance += 1;
        }
        // We want half of it, so we can go up and down
        tolerance /= 2;
        plugin.log(Level.FINER, "Given Location of: " + plugin.getLocationManipulation().strCoordsRaw(l));
        plugin.log(Level.FINER, "Checking +-" + tolerance + " with a radius of " + radius);

        // For now this will just do a straight up block.
        Location locToCheck = l.clone();
        // Check the main level
        Location safe = checkAroundLocation(locToCheck, radius);
        if (safe != null) {
            return safe;
        }
        // We've already checked zero right above this.
        int currentLevel = 1;
        while (currentLevel <= tolerance) {
            // Check above
            locToCheck = l.clone();
            locToCheck.add(0, currentLevel, 0);
            safe = checkAroundLocation(locToCheck, radius);
            if (safe != null) {
                return safe;
            }

            // Check below
            locToCheck = l.clone();
            locToCheck.subtract(0, currentLevel, 0);
            safe = checkAroundLocation(locToCheck, radius);
            if (safe != null) {
                return safe;
            }
            currentLevel++;
        }

        return null;
    }

    /*
     * For my crappy algorithm, radius MUST be odd.
     */
    private Location checkAroundLocation(final Location l, int diameter) {
        if (diameter % 2 == 0) {
            diameter += 1;
        }
        Location checkLoc = l.clone();

        // Start at 3, the min diameter around a block
        int loopcounter = 3;
        while (loopcounter <= diameter) {
            final boolean foundSafeArea = checkAroundSpecificDiameter(checkLoc, loopcounter);
            // If a safe area was found:
            if (foundSafeArea) {
                // Return the checkLoc, it is the safe location.
                return checkLoc;
            }
            // Otherwise, let's reset our location
            checkLoc = l.clone();
            // And increment the radius
            loopcounter += 2;
        }
        return null;
    }

    private boolean checkAroundSpecificDiameter(final Location checkLoc, final int circle) {
        // Adjust the circle to get how many blocks to step out.
        // A radius of 3 makes the block step 1
        // A radius of 5 makes the block step 2
        // A radius of 7 makes the block step 3
        // ...
        final int adjustedCircle = ((circle - 1) / 2);
        checkLoc.add(adjustedCircle, 0, 0);
        if (plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
            return true;
        }
        // Now we go to the right that adjustedCircle many
        for (int i = 0; i < adjustedCircle; i++) {
            checkLoc.add(0, 0, 1);
            if (plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
                return true;
            }
        }

        // Then down adjustedCircle *2
        for (int i = 0; i < adjustedCircle * 2; i++) {
            checkLoc.add(-1, 0, 0);
            if (plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
                return true;
            }
        }

        // Then left adjustedCircle *2
        for (int i = 0; i < adjustedCircle * 2; i++) {
            checkLoc.add(0, 0, -1);
            if (plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
                return true;
            }
        }

        // Then up Then left adjustedCircle *2
        for (int i = 0; i < adjustedCircle * 2; i++) {
            checkLoc.add(1, 0, 0);
            if (plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
                return true;
            }
        }

        // Then finish up by doing adjustedCircle - 1
        for (int i = 0; i < adjustedCircle - 1; i++) {
            checkLoc.add(0, 0, 1);
            if (plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeleportResult safelyTeleport(final CommandSender teleporter, final Entity teleportee, final MVDestination d) {
        if (d instanceof InvalidDestination) {
            plugin.log(Level.FINER, "Entity tried to teleport to an invalid destination");
            return TeleportResult.FAIL_INVALID;
        }
        Player teleporteePlayer = null;
        if (teleportee instanceof Player) {
            teleporteePlayer = ((Player) teleportee);
        }
        else if (teleportee.getPassenger() instanceof Player) {
            teleporteePlayer = ((Player) teleportee.getPassenger());
        }

        if (teleporteePlayer == null) {
            return TeleportResult.FAIL_INVALID;
        }
        MultiverseCore.addPlayerToTeleportQueue(teleporter.getName(), teleporteePlayer.getName());

        Location safeLoc = d.getLocation(teleportee);
        if (d.useSafeTeleporter()) {
            safeLoc = getSafeLocation(teleportee, d);
        }

        if (safeLoc != null) {
            if (teleportee.teleport(safeLoc)) {
                if (!d.getVelocity().equals(new Vector(0, 0, 0))) {
                    teleportee.setVelocity(d.getVelocity());
                }
                return TeleportResult.SUCCESS;
            }
            return TeleportResult.FAIL_OTHER;
        }
        return TeleportResult.FAIL_UNSAFE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeleportResult safelyTeleport(final CommandSender teleporter, final Entity teleportee, Location location, final boolean safely) {
        if (safely) {
            location = getSafeLocation(location);
        }

        if (location != null) {
            if (teleportee.teleport(location)) {
                return TeleportResult.SUCCESS;
            }
            return TeleportResult.FAIL_OTHER;
        }
        return TeleportResult.FAIL_UNSAFE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getSafeLocation(final Entity e, final MVDestination d) {
        final Location l = d.getLocation(e);
        if (plugin.getBlockSafety().playerCanSpawnHereSafely(l)) {
            plugin.log(Level.FINE, "The first location you gave me was safe.");
            return l;
        }
        if (e instanceof Minecart) {
            final Minecart m = (Minecart) e;
            if (!plugin.getBlockSafety().canSpawnCartSafely(m)) {
                return null;
            }
        }
        else if (e instanceof Vehicle) {
            final Vehicle v = (Vehicle) e;
            if (!plugin.getBlockSafety().canSpawnVehicleSafely(v)) {
                return null;
            }
        }
        final Location safeLocation = getSafeLocation(l);
        if (safeLocation != null) {
            // Add offset to account for a vehicle on dry land!
            if (e instanceof Minecart && !plugin.getBlockSafety().isEntitiyOnTrack(safeLocation)) {
                safeLocation.setY(safeLocation.getBlockY() + .5);
                plugin.log(Level.FINER, "Player was inside a minecart. Offsetting Y location.");
            }
            plugin.log(Level.FINE, "Had to look for a bit, but I found a safe place for ya!");
            return safeLocation;
        }
        if (e instanceof Player) {
            final Player p = (Player) e;
            plugin.getMessaging().sendMessage(p, "No safe locations found!", false);
            plugin.log(Level.FINER, "No safe location found for " + p.getName());
        } else if (e.getPassenger() instanceof Player) {
            final Player p = (Player) e.getPassenger();
            plugin.getMessaging().sendMessage(p, "No safe locations found!", false);
            plugin.log(Level.FINER, "No safe location found for " + p.getName());
        }
        plugin.log(Level.FINE, "Sorry champ, you're basically trying to teleport into a minefield. I should just kill you now.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location findPortalBlockNextTo(final Location l) {
        final Block b = l.getWorld().getBlockAt(l);
        Location foundLocation = null;
        if (b.getType() == Material.NETHER_PORTAL) {
            return l;
        }
        if (b.getRelative(BlockFace.NORTH).getType() == Material.NETHER_PORTAL) {
            foundLocation = getCloserBlock(l, b.getRelative(BlockFace.NORTH).getLocation(), null);
        }
        if (b.getRelative(BlockFace.SOUTH).getType() == Material.NETHER_PORTAL) {
            foundLocation = getCloserBlock(l, b.getRelative(BlockFace.SOUTH).getLocation(), foundLocation);
        }
        if (b.getRelative(BlockFace.EAST).getType() == Material.NETHER_PORTAL) {
            foundLocation = getCloserBlock(l, b.getRelative(BlockFace.EAST).getLocation(), foundLocation);
        }
        if (b.getRelative(BlockFace.WEST).getType() == Material.NETHER_PORTAL) {
            foundLocation = getCloserBlock(l, b.getRelative(BlockFace.WEST).getLocation(), foundLocation);
        }
        return foundLocation;
    }

    @Override
    public TeleportResult teleport(final CommandSender teleporter, final Player teleportee, final MVDestination destination) {
        return safelyTeleport(teleporter, teleportee, destination);
    }
}
