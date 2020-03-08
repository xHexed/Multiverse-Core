/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.enums.Action;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * Removes all values from a world-property.
 */
public class ModifyClearCommand extends MultiverseCommand {
    private final MVWorldManager worldManager;

    public ModifyClearCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Modify a World (Clear a property)");
        setCommandUsage("/mv modify" + ChatColor.GREEN + " clear {PROPERTY}" + ChatColor.GOLD + " [WORLD]");
        setArgRange(1, 2);
        addKey("mvm clear");
        addKey("mvmclear");
        addKey("mv modify clear");
        addKey("mvmodify clear");
        addCommandExample("/mvm " + ChatColor.GOLD + "clear " + ChatColor.RED + "animals");
        addCommandExample("/mvm " + ChatColor.GOLD + "clear " + ChatColor.RED + "monsters");
        addCommandExample("/mvm " + ChatColor.GOLD + "clear " + ChatColor.RED + "worldblacklist");
        setPermission("multiverse.core.modify.clear",
                      "Removes all values from a property. This will work on properties that contain lists.", PermissionDefault.OP);
        worldManager = this.plugin.getMVWorldManager();
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        // We NEED a world from the command line
        Player p = null;
        if (sender instanceof Player) {
            p = (Player) sender;
        }
        if (args.size() == 1 && p == null) {
            sender.sendMessage(ChatColor.RED + "From the console, WORLD is required.");
            sender.sendMessage(getCommandDesc());
            sender.sendMessage(getCommandUsage());
            sender.sendMessage("Nothing changed.");
            return;
        }

        final MultiverseWorld world;
        final String property = args.get(0);

        if (args.size() == 1) {
            world = worldManager.getMVWorld(p.getWorld().getName());
        }
        else {
            world = worldManager.getMVWorld(args.get(1));
        }

        if (world == null) {
            sender.sendMessage("That world does not exist!");
            return;
        }

        if (!ModifyCommand.validateAction(Action.Clear, property)) {
            sender.sendMessage("Sorry, you can't use CLEAR with " + property);
            sender.sendMessage("Please visit our Github Wiki for more information: https://goo.gl/q1h01S");
            return;
        }
        // TODO fix this
        if (world.clearList(property)) {
            sender.sendMessage(property + " was cleared. It contains 0 values now.");
            sender.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.AQUA + property + ChatColor.WHITE + " was "
                    + ChatColor.GREEN + "CLEARED" + ChatColor.WHITE + ". It contains " + ChatColor.LIGHT_PURPLE + "0" + ChatColor.WHITE + " values now.");
            if (!plugin.saveWorldConfig()) {
                sender.sendMessage(ChatColor.RED + "There was an issue saving worlds.yml!  Your changes will only be temporary!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.GOLD + property
                    + ChatColor.WHITE + " was " + ChatColor.GOLD + "NOT" + ChatColor.WHITE + " cleared.");
        }
    }

}
