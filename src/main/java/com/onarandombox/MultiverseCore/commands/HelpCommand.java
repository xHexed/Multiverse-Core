/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.pneumaticraft.commandhandler.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a nice help menu.
 */
public class HelpCommand extends PaginatedCoreCommand<Command> {

    public HelpCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Get Help with Multiverse");
        setCommandUsage("/mv " + ChatColor.GOLD + "[FILTER] [PAGE #]");
        setArgRange(0, 2);
        addKey("mv");
        addKey("mvh");
        addKey("mvhelp");
        addKey("mv help");
        addKey("mvsearch");
        addKey("mv search");
        addCommandExample("/mv help ?");
        setPermission("multiverse.help", "Displays a nice help menu.", PermissionDefault.TRUE);
        setItemsPerPage(7); // SUPPRESS CHECKSTYLE: MagicNumberCheck
    }

    @Override
    protected List<Command> getFilteredItems(final List<Command> availableItems, final String filter) {
        final List<Command> filtered = new ArrayList<>();

        for (final Command c : availableItems) {
            if (stitchThisString(c.getKeyStrings()).matches("(?i).*" + filter + ".*")) {
                filtered.add(c);
            }
            else if (c.getCommandName().matches("(?i).*" + filter + ".*")) {
                filtered.add(c);
            }
            else if (c.getCommandDesc().matches("(?i).*" + filter + ".*")) {
                filtered.add(c);
            }
            else if (c.getCommandUsage().matches("(?i).*" + filter + ".*")) {
                filtered.add(c);
            }
            else {
                for (final String example : c.getCommandExamples()) {
                    if (example.matches("(?i).*" + filter + ".*")) {
                        filtered.add(c);
                        break;
                    }
                }
            }
        }
        return filtered;
    }

    @Override
    protected String getItemText(final Command item) {
        return ChatColor.AQUA + item.getCommandUsage();
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        sender.sendMessage(ChatColor.AQUA + "====[ Multiverse Help ]====");

        final FilterObject filterObject = getPageAndFilter(args);

        List<Command> availableCommands = new ArrayList<>(plugin.getCommandHandler().getCommands(sender));
        if (filterObject.getFilter().length() > 0) {
            availableCommands = getFilteredItems(availableCommands, filterObject.getFilter());
            if (availableCommands.size() == 0) {
                sender.sendMessage(ChatColor.RED + "Sorry... " + ChatColor.WHITE
                                           + "No commands matched your filter: " + ChatColor.AQUA + filterObject.getFilter());
                return;
            }
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + " Add a '" + ChatColor.DARK_PURPLE + "?" + ChatColor.AQUA + "' after a command to see more about it.");
            for (final Command c : availableCommands) {
                sender.sendMessage(ChatColor.AQUA + c.getCommandUsage());
            }
            return;
        }

        final int totalPages = (int) Math.ceil(availableCommands.size() / (itemsPerPage + 0.0));

        if (filterObject.getPage() > totalPages) {
            filterObject.setPage(totalPages);
        }

        sender.sendMessage(ChatColor.AQUA + " Page " + filterObject.getPage() + " of " + totalPages);
        sender.sendMessage(ChatColor.AQUA + " Add a '" + ChatColor.DARK_PURPLE + "?" + ChatColor.AQUA + "' after a command to see more about it.");

        showPage(filterObject.getPage(), sender, availableCommands);
    }
}
