/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * Deletes worlds.
 */
public class DeleteCommand extends MultiverseCommand {

    public DeleteCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Delete World");
        setCommandUsage("/mv delete" + ChatColor.GREEN + " {WORLD}");
        setArgRange(1, 1);
        addKey("mvdelete");
        addKey("mv delete");
        addCommandExample("/mv delete " + ChatColor.GOLD + "MyWorld");
        setPermission("multiverse.core.delete", "Deletes a world on your server. " + ChatColor.RED + "PERMANENTLY.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        final String worldName = args.get(0);

        final Class<?>[] paramTypes = {String.class};
        final List<Object> objectArgs = new ArrayList<>(args);
        plugin.getCommandHandler()
                .queueCommand(sender, "mvdelete", "deleteWorld", objectArgs,
                              paramTypes, ChatColor.GREEN + "World '" + worldName + "' Deleted!",
                              ChatColor.RED + "World '" + worldName + "' could NOT be deleted!");
    }
}
