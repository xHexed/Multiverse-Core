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
 * An exact {@link MVDestination}.
 */
public class ExactDestination implements MVDestination {
    private static final Pattern COORD = Pattern.compile("(-?[\\d]+\\.?[\\d]*|~-?[\\d]+\\.?[\\d]*|~),(-?[\\d]+\\.?[\\d]*|~-?[\\d]+\\.?[\\d]*|~),(-?[\\d]+\\.?[\\d]*|~-?[\\d]+\\.?[\\d]*|~)");
    private boolean isValid;
    private Location location;
    private boolean relativeX, relativeY, relativeZ;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return "e";
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
        final List<String> parsed = Arrays.asList(destination.split(":"));
        // Need at least: e:world:x,y,z
        // OR e:world:x,y,z:pitch:yaw
        // so basically 3 or 5
        if (!(parsed.size() == 3 || parsed.size() == 5)) { // SUPPRESS CHECKSTYLE: MagicNumberCheck
            return false;
        }
        // If it's not an Exact type
        if (!parsed.get(0).equalsIgnoreCase("e")) {
            return false;
        }

        // If it's not a MV world
        if (!((MultiverseCore) plugin).getMVWorldManager().isMVWorld(parsed.get(1))) {
            return false;
        }

        if (!COORD.matcher(parsed.get(2)).matches()) {
            return false;
        }
        // This is 1 now, because we've removed 2
        if (parsed.size() == 3) {
            return true;
        }

        try {
            Float.parseFloat(parsed.get(3));
            Float.parseFloat(parsed.get(4)); // SUPPRESS CHECKSTYLE: MagicNumberCheck
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
        final Location loc = location.clone();
        if (relativeX || relativeY || relativeZ) {
            final Location eLoc = e.getLocation();
            loc.add(relativeX ? eLoc.getX() : 0, relativeY ? eLoc.getY() : 0, relativeZ ? eLoc.getZ() : 0);
            // Since the location is relative, it makes sense to use the entity's pitch and yaw unless those were
            // specified in the destination.
            if (loc.getPitch() == 0) {
                loc.setPitch(eLoc.getPitch());
            }
            if (loc.getYaw() == 0) {
                loc.setYaw(eLoc.getYaw());
            }
        }
        return loc;
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
        // Need at least: e:world:x,y,z
        // OR e:world:x,y,z:pitch:yaw
        // so basically 3 or 5
        if (!(parsed.size() == 3 || parsed.size() == 5)) { // SUPPRESS CHECKSTYLE: MagicNumberCheck
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
            final String[] relSplit = coordString[i].split("~");
            boolean relative = false;
            if (relSplit.length == 0) {
                // coord is "~" form
                relative  = true;
                coords[i] = 0;
            }
            else if (relSplit.length == 1) {
                // coord is "123" form
                try {
                    coords[i] = Double.parseDouble(relSplit[0]);
                }
                catch (final NumberFormatException e) {
                    isValid = false;
                    return;
                }
            } else {
                // coord is "~123" form
                relative = true;
                try {
                    coords[i] = Double.parseDouble(relSplit[1]);
                }
                catch (final NumberFormatException e) {
                    isValid = false;
                    return;
                }
            }
            if (relative) {
                switch (i) {
                    case 0:
                        relativeX = true;
                        break;
                    case 1:
                        relativeY = true;
                        break;
                    case 2:
                        relativeZ = true;
                        break;
                }
            }
        }
        location.setX(coords[0]);
        location.setY(coords[1]);
        location.setZ(coords[2]);

        if (parsed.size() == 3) {
            isValid = true;
            return;
        }

        try {
            location.setPitch(Float.parseFloat(parsed.get(3)));
            location.setYaw(Float.parseFloat(parsed.get(4))); // SUPPRESS CHECKSTYLE: MagicNumberCheck
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
        return "Exact";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Exact (" + location.getX() + ", " + location.getY() + ", " + location.getZ()
                + ":" + location.getPitch() + ":" + location.getYaw() + ")";
    }

    /**
     * Sets this {@link ExactDestination}.
     *
     * @param location The {@link Location}.
     */
    public void setDestination(final Location location) {
        isValid = (this.location = location) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (isValid) {
            return "e:" + location.getWorld().getName() + ":" + location.getX() + "," + location.getY()
                    + "," + location.getZ() + ":" + location.getPitch() + ":" + location.getYaw();
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
        // This is an EXACT destination, don't safely teleport here.
        return false;
    }
}
