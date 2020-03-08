/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * Teleports a player to the spawn.
 */
public class SpawnCommand extends MultiverseCommand {

    public SpawnCommand(final MultiverseCore plugin) {
        super(plugin);
        final Permission otherPerm = new Permission("multiverse.core.spawn.other",
                                                    "Teleports another player to the spawn of the world they are in.", PermissionDefault.OP);
        setName("Spawn");
        setCommandUsage("/mv spawn" + ChatColor.GOLD + " [PLAYER]");
        setArgRange(0, 1);
        addKey("mvspawn");
        addKey("mv spawn");
        addKey("mvs");
        setPermission("multiverse.core.spawn.self", "Teleports you to the Spawn Point of the world you are in.", PermissionDefault.OP);
        addAdditonalPermission(otherPerm);

    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        // If a persons name was passed in, you must be A. the console, or B have permissions
        if (args.size() == 1) {
            if (player != null && !plugin.getMVPerms().hasPermission(player, "multiverse.core.spawn.other", true)) {
                sender.sendMessage("You don't have permission to teleport another player to spawn. (multiverse.core.spawn.other)");
                return;
            }
            final Player target = plugin.getServer().getPlayer(args.get(0));
            if (target != null) {
                target.sendMessage("Teleporting to this world's spawn...");
                spawnAccurately(target);

                if (player != null) {
                    target.sendMessage("You were teleported by: " + ChatColor.YELLOW + player.getName());
                } else {
                    target.sendMessage("You were teleported by: " + ChatColor.LIGHT_PURPLE + "the console");
                }
            } else {
                sender.sendMessage(args.get(0) + " is not logged on right now!");
            }
        } else {
            if (player != null && !plugin.getMVPerms().hasPermission(player, "multiverse.core.spawn.self", true)) {
                sender.sendMessage("You don't have permission to teleport yourself to spawn. (multiverse.core.spawn.self)");
                return;
            }
            if (player != null) {
                player.sendMessage("Teleporting to this world's spawn...");
                spawnAccurately(player);
            }
            else {
                sender.sendMessage("From the console, you must provide a PLAYER.");
            }
        }
    }

    private void spawnAccurately(final Player player) {
        final MultiverseWorld world = plugin.getMVWorldManager().getMVWorld(player.getWorld().getName());
        final Location spawnLocation;
        if (world != null) {
            spawnLocation = world.getSpawnLocation();
        }
        else {
            spawnLocation = player.getWorld().getSpawnLocation();
        }
        plugin.getSafeTTeleporter().safelyTeleport(player, player, spawnLocation, false);
    }
}
