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
 * Removes values from a world-property.
 */
public class ModifyRemoveCommand extends MultiverseCommand {
    private final MVWorldManager worldManager;

    public ModifyRemoveCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Modify a World (Remove a value)");
        setCommandUsage("/mv modify" + ChatColor.GREEN + " remove {PROPERTY} {VALUE}" + ChatColor.GOLD + " [WORLD]");
        setArgRange(2, 3);
        addKey("mvm remove");
        addKey("mvmremove");
        addKey("mv modify remove");
        addKey("mvmodify remove");
        addKey("mvm delete");
        addKey("mvmdelete");
        addKey("mv modify delete");
        addKey("mvmodify delete");
        addCommandExample("/mvm " + ChatColor.GOLD + "remove " + ChatColor.GREEN + "sheep " + ChatColor.RED + "animals");
        addCommandExample("/mvm " + ChatColor.GOLD + "remove " + ChatColor.GREEN + "creeper " + ChatColor.RED + "monsters");
        addCommandExample("/mvm " + ChatColor.GOLD + "remove " + ChatColor.GREEN + "MyWorld " + ChatColor.RED + "worldblacklist");
        setPermission("multiverse.core.modify.remove", "Modify various aspects of worlds. See the help wiki for how to use this command properly. "
                + "If you do not include a world, the current world will be used.", PermissionDefault.OP);
        worldManager = this.plugin.getMVWorldManager();
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        // We NEED a world from the command line
        Player p = null;
        if (sender instanceof Player) {
            p = (Player) sender;
        }

        if (args.size() == 2 && p == null) {
            sender.sendMessage(ChatColor.RED + "From the console, WORLD is required.");
            sender.sendMessage(getCommandDesc());
            sender.sendMessage(getCommandUsage());
            sender.sendMessage("Nothing changed.");
            return;
        }

        final MultiverseWorld world;
        final String value = args.get(0);
        final String property = args.get(1);

        if (args.size() == 2) {
            world = worldManager.getMVWorld(p.getWorld().getName());
        }
        else {
            world = worldManager.getMVWorld(args.get(2));
        }

        if (world == null) {
            sender.sendMessage("That world does not exist!");
            return;
        }

        if (!ModifyCommand.validateAction(Action.Remove, property)) {
            sender.sendMessage("Sorry, you can't REMOVE anything from" + property);
            sender.sendMessage("Please visit our Github Wiki for more information: https://goo.gl/OMGwzx");
            return;
        }
        // TODO fix this
        if (world.removeFromVariable(property, value)) {
            sender.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.AQUA + value + ChatColor.WHITE
                    + " was " + ChatColor.RED + "removed from " + ChatColor.GREEN + property);
            if (!plugin.saveWorldConfig()) {
                sender.sendMessage(ChatColor.RED + "There was an issue saving worlds.yml!  Your changes will only be temporary!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "There was an error removing " + ChatColor.GRAY
                    + value + ChatColor.WHITE + " from " + ChatColor.GOLD + property);
        }
    }

}
