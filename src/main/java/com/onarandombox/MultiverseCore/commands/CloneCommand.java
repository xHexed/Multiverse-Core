/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;

/**
 * Creates a clone of a world.
 */
public class CloneCommand extends MultiverseCommand {
    private final MVWorldManager worldManager;

    public CloneCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Clone World");
        setCommandUsage("/mv clone" + ChatColor.GREEN + " {TARGET} {NAME}" + ChatColor.GOLD + " -g [GENERATOR[:ID]]");
        setArgRange(2, 4); // SUPPRESS CHECKSTYLE: MagicNumberCheck
        addKey("mvclone");
        addKey("mvcl");
        addKey("mv cl");
        addKey("mv clone");
        addCommandExample("/mv clone " + ChatColor.GOLD + "world" + ChatColor.GREEN + " world_backup");
        addCommandExample("/mv clone " + ChatColor.GOLD + "skyblock_pristine" + ChatColor.GREEN + " skyblock");
        setPermission("multiverse.core.clone", "Clones a world.", PermissionDefault.OP);
        worldManager = this.plugin.getMVWorldManager();
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        final String oldName = args.get(0);
        if (!worldManager.hasUnloadedWorld(oldName, true)) {
            // If no world was found, we can't clone.
            sender.sendMessage("Sorry, Multiverse doesn't know about world " + oldName + ", so we can't clone it!");
            sender.sendMessage("Check the " + ChatColor.GREEN + "/mv list" + ChatColor.WHITE + " command to verify it is listed.");
            return;
        }
        if (plugin.getMVWorldManager().cloneWorld(oldName, args.get(1))) {
            sender.sendMessage(ChatColor.GREEN + "World cloned!");
        }
        else {
            sender.sendMessage(ChatColor.RED + "World could NOT be cloned!");
        }
    }
}
