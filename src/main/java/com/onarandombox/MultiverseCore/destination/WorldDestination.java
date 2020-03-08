/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * A world-{@link MVDestination}.
 */
public class WorldDestination implements MVDestination {
    private boolean isValid;
    private MultiverseWorld world;
    private float yaw = -1;
    private static final String direction = "";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return "w";
    }

    private static Location getAcurateSpawnLocation(final Entity e, final MultiverseWorld world) {
        if (world != null) {
            return world.getSpawnLocation();
        }
        else {
            // add 0.5 to x and z to center people
            // (spawn location is stored as int meaning that you would spawn in the corner of a block)
            return e.getWorld().getSpawnLocation().add(.5, 0, .5);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThisType(final JavaPlugin plugin, final String destination) {
        final String[] items = destination.split(":");
        if (items.length > 3) {
            return false;
        }
        if (items.length == 1 && ((MultiverseCore) plugin).getMVWorldManager().isMVWorld(items[0])) {
            // This case is: world
            return true;
        }
        // This case is: w:world
        // and w:world:ne
        if (items.length == 2 && ((MultiverseCore) plugin).getMVWorldManager().isMVWorld(items[0])) {
            // This case is: world:n
            return true;
        }
        else return items[0].equalsIgnoreCase("w") && ((MultiverseCore) plugin).getMVWorldManager().isMVWorld(items[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLocation(final Entity e) {
        final Location spawnLoc = getAcurateSpawnLocation(e, world);
        if (yaw >= 0) {
            // Only modify the yaw if its set.
            spawnLoc.setYaw(yaw);
        }
        return spawnLoc;
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
        // TODO Taking a JavaPlugin here is rather useless, if we keep casting it up to MultiverseCore.
        // We should change that.
        final Core core = (Core) plugin;
        final String[] items = destination.split(":");
        if (items.length > 3) {
            isValid = false;
            return;
        }
        if (items.length == 1 && ((MultiverseCore) plugin).getMVWorldManager().isMVWorld(items[0])) {
            isValid = true;
            world   = core.getMVWorldManager().getMVWorld(items[0]);
            return;
        }
        if (items.length == 2 && ((MultiverseCore) plugin).getMVWorldManager().isMVWorld(items[0])) {
            world = core.getMVWorldManager().getMVWorld(items[0]);
            yaw   = core.getLocationManipulation().getYaw(items[1]);
            return;
        }
        if (items[0].equalsIgnoreCase("w") && ((MultiverseCore) plugin).getMVWorldManager().isMVWorld(items[1])) {
            world   = ((MultiverseCore) plugin).getMVWorldManager().getMVWorld(items[1]);
            isValid = true;
            if (items.length == 3) {
                yaw = core.getLocationManipulation().getYaw(items[2]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "World";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return world.getColoredWorldString();
    }

    @Override
    public String toString() {
        return world.getCBWorld().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequiredPermission() {
        // TODO: Potenitally replace spaces wiht tabs for friendlier yaml.
        // this.world.getName().replace(" ","_");
        return "multiverse.access." + world.getName();
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
