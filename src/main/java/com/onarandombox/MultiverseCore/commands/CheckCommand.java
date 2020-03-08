/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
/**
 * Checks to see if a player can go to a destination.
 */
public class CheckCommand extends MultiverseCommand {

    public CheckCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Help you validate your multiverse settings");
        setCommandUsage("/mv check " + ChatColor.GREEN + "{PLAYER} {DESTINATION}");
        setArgRange(2, 2);
        addKey("mv check");
        addKey("mvcheck");
        addCommandExample("/mv check " + ChatColor.GREEN + "fernferret " + ChatColor.LIGHT_PURPLE + "w:MyWorld");
        addCommandExample("/mv check " + ChatColor.GREEN + "Rigby90 " + ChatColor.LIGHT_PURPLE + "p:MyPortal");
        addCommandExample("/mv check " + ChatColor.GREEN + "lithium3141 " + ChatColor.LIGHT_PURPLE + "ow:WarpName");
        setPermission("multiverse.core.debug", "Checks to see if a player can go to a destination. Prints debug if false.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        final Player p = plugin.getServer().getPlayer(args.get(0));
        if (p == null) {
            sender.sendMessage("Could not find player " + ChatColor.GREEN + args.get(0));
            sender.sendMessage("Are they online?");
            return;
        }
        final MVDestination dest = plugin.getDestFactory().getDestination(args.get(1));
        if (dest instanceof InvalidDestination) {
            sender.sendMessage(String.format("You asked if '%s' could go to %s%s%s,",
                                             args.get(0), ChatColor.GREEN, args.get(0), ChatColor.WHITE));
            sender.sendMessage("but I couldn't find a Destination of that name? Did you type it correctly?");
            return;
        }

        final MVPermissions perms = plugin.getMVPerms();
        perms.tellMeWhyICantDoThis(sender, p, dest);
    }
}
