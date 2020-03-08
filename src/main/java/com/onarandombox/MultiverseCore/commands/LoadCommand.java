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
 * Loads a world into Multiverse.
 */
public class LoadCommand extends MultiverseCommand {

    public LoadCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Load World");
        setCommandUsage("/mv load" + ChatColor.GREEN + " {WORLD}");
        setArgRange(1, 1);
        addKey("mvload");
        addKey("mv load");
        addCommandExample("/mv load " + ChatColor.GREEN + "MyUnloadedWorld");
        setPermission("multiverse.core.load", "Loads a world into Multiverse.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        if (plugin.getMVWorldManager().loadWorld(args.get(0))) {
            Command.broadcastCommandMessage(sender, "Loaded world '" + args.get(0) + "'!");
        }
        else {
            sender.sendMessage("Error trying to load world '" + args.get(0) + "'!");
        }
    }
}
