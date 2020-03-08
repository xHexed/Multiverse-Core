/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

/**
 * An anchor-{@link MVDestination}.
 */
public class AnchorDestination implements MVDestination {
    private boolean isValid;
    private Location location;
    private MultiverseCore plugin;
    private String name;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return "a";
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
    public boolean isThisType(final JavaPlugin plugin, final String destination) {
        if (!(plugin instanceof MultiverseCore)) {
            return false;
        }
        this.plugin = (MultiverseCore) plugin;
        final List<String> parsed = Arrays.asList(destination.split(":"));
        // Need at least: a:name
        if (!(parsed.size() == 2)) {
            return false;
        }
        // If it's not an Anchor type
        return parsed.get(0).equalsIgnoreCase("a");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLocation(final Entity e) {
        return location;
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
        if (!(plugin instanceof MultiverseCore)) {
            return;
        }
        this.plugin = (MultiverseCore) plugin;
        final List<String> parsed = Arrays.asList(destination.split(":"));
        // Need at least: e:world:x,y,z
        // OR e:world:x,y,z:pitch:yaw
        // so basically 3 or 5
        if (!(parsed.size() == 2)) {
            isValid = false;
            return;
        }
        name     = parsed.get(1);
        location = this.plugin.getAnchorManager().getAnchorLocation(parsed.get(1));
        if (location == null) {
            isValid = false;
            return;
        }
        if (!parsed.get(0).equalsIgnoreCase(getIdentifier())) {
            isValid = false;
        }
        isValid = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "Anchor";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Anchor: " + name;
    }

    @Override
    public String toString() {
        if (isValid) {
            return "a:" + name;
        }
        return "i:Invalid Destination";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequiredPermission() {
        return "multiverse.access." + location.getWorld().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean useSafeTeleporter() {
        // This is an ANCHOR destination, don't safely teleport here.
        return false;
    }
}
