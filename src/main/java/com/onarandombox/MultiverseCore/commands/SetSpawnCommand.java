/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.BlockSafety;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import org.bukkit.Bukkit;

/**
 * Sets the spawn for a world.
 */
public class SetSpawnCommand extends MultiverseCommand {
    public SetSpawnCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Set World Spawn");
        setCommandUsage("/mv setspawn");
        setArgRange(0, 6);
        addKey("mvsetspawn");
        addKey("mvss");
        addKey("mv set spawn");
        addKey("mv setspawn");
        addKey("mvset spawn");
        addCommandExample("/mv set spawn");
        setPermission("multiverse.core.spawn.set", "Sets the spawn for the current world.", PermissionDefault.OP);
    }

    /**
     * Dispatches the user's command depending on the number of parameters
     *
     * @param sender The player who executes the command, may be console as well.
     * @param args   Command line parameters
     */
    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        if (args.isEmpty()) {
            setWorldSpawn(sender);
        }
        else if (args.size() == 4) {
            setWorldSpawn(sender, args.get(0), args.get(1), args.get(2), args.get(3));
        }
        else if (args.size() == 6) {
            setWorldSpawn(sender, args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5));
        }
        else {
            sender.sendMessage("Use no arguments for your current location, or world/x/y/z, or world/x/y/z/yaw/pitch!");
        }
    }

    /**
     * Set the world spawn when no parameters are given
     *
     * @param sender The {@link CommandSender} who executes the command.
     *               Everything not a {@link Player}, e.g. console, gets rejected, as we can't get coordinates from there.
     */
    protected void setWorldSpawn(final CommandSender sender) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            final Location l = p.getLocation();
            final World w = p.getWorld();
            setWorldSpawn(sender, w, l);
        }
        else {
            sender.sendMessage("You need to give coordinates to use this command from the console!");
        }
    }

    /**
     * Set the world spawn when 4 parameters are given
     *
     * @param sender The {@link CommandSender} who executes the command
     * @param world  The world to set the spawn in
     * @param x      X-coordinate to set the spawn to (as a {@link String} as it's from the command line, gets parsed into a double)
     * @param y      Y-coordinate to set the spawn to (as a {@link String} as it's from the command line, gets parsed into a double)
     * @param z      Z-coordinate to set the spawn to (as a {@link String} as it's from the command line, gets parsed into a double)
     */
    protected void setWorldSpawn(final CommandSender sender, final String world, final String x, final String y, final String z) {
        setWorldSpawn(sender, world, x, y, z, "0", "0");
    }

    /**
     * Set the world spawn when 6 parameters are given
     *
     * @param sender The {@link CommandSender} who executes the command
     * @param world  The world to set the spawn in
     * @param x      X-coordinate to set the spawn to (as a {@link String} as it's from the command line, gets parsed into a double)
     * @param y      Y-coordinate to set the spawn to (as a {@link String} as it's from the command line, gets parsed into a double)
     * @param z      Z-coordinate to set the spawn to (as a {@link String} as it's from the command line, gets parsed into a double)
     * @param yaw    Yaw a newly spawned player should look at (as a {@link String} as it's from the command line, gets parsed into a float)
     * @param pitch  Pitch a newly spawned player should look at (as a {@link String} as it's from the command line, gets parsed into a float)
     */
    protected void setWorldSpawn(final CommandSender sender, final String world, final String x, final String y, final String z, final String yaw, final String pitch) {
        final double dx;
        final double dy;
        final double dz;
        final float fpitch;
        final float fyaw;
        final World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            sender.sendMessage("World " + world + " is unknown!");
            return;
        }
        try {
            dx     = Double.parseDouble(x);
            dy     = Double.parseDouble(y);
            dz     = Double.parseDouble(z);
            fpitch = Float.parseFloat(pitch);
            fyaw   = Float.parseFloat(yaw);
        }
        catch (final NumberFormatException ex) {
            sender.sendMessage("All coordinates must be numeric");
            return;
        }
        final Location l = new Location(bukkitWorld, dx, dy, dz, fyaw, fpitch);
        setWorldSpawn(sender, bukkitWorld, l);
    }

    /**
     * Does the actual spawn-setting-work.
     *
     * @param sender The {@link CommandSender} that's setting the spawn.
     * @param w      The {@link World} to set the spawn in
     * @param l      The {@link Location} to set the spawn to
     */
    private void setWorldSpawn(final CommandSender sender, final World w, final Location l) {
        final MultiverseWorld foundWorld = plugin.getMVWorldManager().getMVWorld(w.getName());
        if (foundWorld != null) {
            foundWorld.setSpawnLocation(l);
            final BlockSafety bs = plugin.getBlockSafety();
            if (!bs.playerCanSpawnHereSafely(l) && foundWorld.getAdjustSpawn()) {
                sender.sendMessage("It looks like that location would normally be unsafe. But I trust you.");
                sender.sendMessage("I'm turning off the Safe-T-Teleporter for spawns to this world.");
                sender.sendMessage("If you want this turned back on just do:");
                sender.sendMessage(ChatColor.AQUA + "/mvm set adjustspawn true " + foundWorld.getAlias());
                foundWorld.setAdjustSpawn(false);
            }
            sender.sendMessage("Spawn was set to: " + plugin.getLocationManipulation().strCoords(l));
            if (!plugin.saveWorldConfig()) {
                sender.sendMessage(ChatColor.RED + "There was an issue saving worlds.yml!  Your changes will only be temporary!");
            }
        } else {
            w.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
            sender.sendMessage("Multiverse does not know about this world, only X,Y and Z set. Please import it to set the spawn fully (Pitch/Yaws).");
        }
    }
}
