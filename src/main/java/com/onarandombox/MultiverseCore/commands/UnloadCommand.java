/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * Unloads worlds from Multiverse.
 */
public class UnloadCommand extends MultiverseCommand {

    public UnloadCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Unload World");
        setCommandUsage("/mv unload" + ChatColor.GREEN + " {WORLD}");
        setArgRange(1, 1);
        addKey("mvunload");
        addKey("mv unload");
        setPermission("multiverse.core.unload",
                      "Unloads a world from Multiverse. This does NOT remove the world folder. This does NOT remove it from the config file.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        if (plugin.getMVWorldManager().unloadWorld(args.get(0))) {
            Command.broadcastCommandMessage(sender, "Unloaded world '" + args.get(0) + "'!");
        }
        else {
            sender.sendMessage("Error trying to unload world '" + args.get(0) + "'!");
        }
    }
}
