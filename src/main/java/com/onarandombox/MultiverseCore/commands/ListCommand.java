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
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a listing of all worlds that a player can enter.
 */
public class ListCommand extends PaginatedCoreCommand<String> {

    public ListCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("World Listing");
        setCommandUsage("/mv list");
        setArgRange(0, 2);
        addKey("mvlist");
        addKey("mvl");
        addKey("mv list");
        setPermission("multiverse.core.list.worlds", "Displays a listing of all worlds that you can enter.", PermissionDefault.OP);
        setItemsPerPage(8); // SUPPRESS CHECKSTYLE: MagicNumberCheck
    }

    private List<String> getFancyWorldList(final Player p) {
        final List<String> worldList = new ArrayList<>();
        for (final MultiverseWorld world : plugin.getMVWorldManager().getMVWorlds()) {

            if (p != null && (!plugin.getMVPerms().canEnterWorld(p, world))) {
                continue;
            }

            ChatColor color = ChatColor.GOLD;
            final Environment env = world.getEnvironment();
            if (env == Environment.NETHER) {
                color = ChatColor.RED;
            }
            else if (env == Environment.NORMAL) {
                color = ChatColor.GREEN;
            } else if (env == Environment.THE_END) {
                color = ChatColor.AQUA;
            }
            final StringBuilder builder = new StringBuilder();
            builder.append(world.getColoredWorldString()).append(ChatColor.WHITE);
            builder.append(" - ").append(color).append(world.getEnvironment());
            if (world.isHidden()) {
                if (p == null || plugin.getMVPerms().hasPermission(p, "multiverse.core.modify", true)) {
                    // Prefix hidden worlds with an "[H]"
                    worldList.add(ChatColor.GRAY + "[H]" + builder);
                }
            }
            else {
                worldList.add(builder.toString());
            }
        }
        for (final String name : plugin.getMVWorldManager().getUnloadedWorlds()) {
            if (p == null || plugin.getMVPerms().hasPermission(p, "multiverse.access." + name, true)) {
                worldList.add(ChatColor.GRAY + name + " - UNLOADED");
            }
        }
        return worldList;
    }

    @Override
    protected List<String> getFilteredItems(final List<String> availableItems, final String filter) {
        final List<String> filtered = new ArrayList<>();

        for (final String s : availableItems) {
            if (s.matches("(?i).*" + filter + ".*")) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    @Override
    protected String getItemText(final String item) {
        return item;
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "====[ Multiverse World List ]====");
        Player p = null;
        if (sender instanceof Player) {
            p = (Player) sender;
        }


        final FilterObject filterObject = getPageAndFilter(args);

        List<String> availableWorlds = new ArrayList<>(getFancyWorldList(p));
        if (filterObject.getFilter().length() > 0) {
            availableWorlds = getFilteredItems(availableWorlds, filterObject.getFilter());
            if (availableWorlds.size() == 0) {
                sender.sendMessage(ChatColor.RED + "Sorry... " + ChatColor.WHITE
                        + "No worlds matched your filter: " + ChatColor.AQUA + filterObject.getFilter());
                return;
            }
        }

        if (!(sender instanceof Player)) {
            for (final String c : availableWorlds) {
                sender.sendMessage(c);
            }
            return;
        }

        final int totalPages = (int) Math.ceil(availableWorlds.size() / (itemsPerPage + 0.0));

        if (filterObject.getPage() > totalPages) {
            filterObject.setPage(totalPages);
        }

        sender.sendMessage(ChatColor.AQUA + " Page " + filterObject.getPage() + " of " + totalPages);

        showPage(filterObject.getPage(), sender, availableWorlds);
    }
}
