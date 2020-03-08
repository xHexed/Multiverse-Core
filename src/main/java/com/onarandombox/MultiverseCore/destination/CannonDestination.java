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
import java.util.regex.Pattern;

/**
 * A cannon-{@link MVDestination}.
 */
public class CannonDestination implements MVDestination {
    private static final Pattern COORD = Pattern.compile("(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*)");
    private boolean isValid;
    private Location location;
    private double speed;

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector getVelocity() {
        final double pitchRadians = Math.toRadians(location.getPitch());
        final double yawRadians = Math.toRadians(location.getYaw());
        double x = Math.sin(yawRadians) * speed * -1;
        final double y = Math.sin(pitchRadians) * speed * -1;
        double z = Math.cos(yawRadians) * speed;
        // Account for the angle they were pointed, and take away velocity
        x = Math.cos(pitchRadians) * x;
        z = Math.cos(pitchRadians) * z;
        return new Vector(x, y, z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return "ca";
    }

    // NEED ca:world:x,y,z:pitch:yaw:speed
    // so basically 6
    private static final int SPLIT_SIZE = 6;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThisType(final JavaPlugin plugin, final String destination) {
        if (!(plugin instanceof MultiverseCore)) {
            return false;
        }
        final List<String> parsed = Arrays.asList(destination.split(":"));
        if (parsed.size() != SPLIT_SIZE) {
            return false;
        }
        // If it's not an Cannon type
        if (!parsed.get(0).equalsIgnoreCase("ca")) {
            return false;
        }

        // If it's not a MV world
        if (!((MultiverseCore) plugin).getMVWorldManager().isMVWorld(parsed.get(1))) {
            return false;
        }
        // Verify X,Y,Z are numbers
        if (!COORD.matcher(parsed.get(2)).matches()) {
            return false;
        }

        try {
            // BEGIN CHECKSTYLE-SUPPRESSION: MagicNumberCheck
            Float.parseFloat(parsed.get(3));
            Float.parseFloat(parsed.get(4));
            Float.parseFloat(parsed.get(5));
            // END CHECKSTYLE-SUPPRESSION: MagicNumberCheck
        }
        catch (final NumberFormatException e) {
            return false;
        }
        return true;
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
        final List<String> parsed = Arrays.asList(destination.split(":"));

        if (parsed.size() != SPLIT_SIZE) {
            isValid = false;
            return;
        }
        if (!parsed.get(0).equalsIgnoreCase(getIdentifier())) {
            isValid = false;
            return;
        }

        if (!((MultiverseCore) plugin).getMVWorldManager().isMVWorld(parsed.get(1))) {
            isValid = false;
            return;
        }

        location = new Location(((MultiverseCore) plugin).getMVWorldManager().getMVWorld(parsed.get(1)).getCBWorld(), 0, 0, 0);

        if (!COORD.matcher(parsed.get(2)).matches()) {
            isValid = false;
            return;
        }
        final double[] coords = new double[3];
        final String[] coordString = parsed.get(2).split(",");
        for (int i = 0; i < 3; i++) {
            try {
                coords[i] = Double.parseDouble(coordString[i]);
            }
            catch (final NumberFormatException e) {
                isValid = false;
                return;
            }
        }
        location.setX(coords[0]);
        location.setY(coords[1]);
        location.setZ(coords[2]);

        try {
            // BEGIN CHECKSTYLE-SUPPRESSION: MagicNumberCheck
            location.setPitch(Float.parseFloat(parsed.get(3)));
            location.setYaw(Float.parseFloat(parsed.get(4)));
            speed = Math.abs(Float.parseFloat(parsed.get(5)));
            // END CHECKSTYLE-SUPPRESSION: MagicNumberCheck
        }
        catch (final NumberFormatException e) {
            isValid = false;
            return;
        }

        isValid = true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "Cannon!";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Cannon (" + location.getX() + ", " + location.getY() + ", " + location.getZ() + ":"
                + location.getPitch() + ":" + location.getYaw() + ":" + speed + ")";

    }

    /**
     * Sets this {@link CannonDestination}.
     *
     * @param location The {@link Location}.
     * @param speed    The speed.
     */
    public void setDestination(final Location location, final double speed) {
        if (location != null) {
            this.location = location;
            this.speed    = Math.abs(speed);
            isValid       = true;
        }
        isValid = false;
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
        return false;
    }

    @Override
    public String toString() {
        if (isValid) {
            return "ca:" + location.getWorld().getName() + ":" + location.getX() + "," + location.getY()
                    + "," + location.getZ() + ":" + location.getPitch() + ":" + location.getYaw() + ":" + speed;
        }
        return "i:Invalid Destination";
    }
}
