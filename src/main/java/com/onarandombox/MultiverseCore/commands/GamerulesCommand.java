/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * Allows management of Anchor Destinations.
 */
public class GamerulesCommand extends MultiverseCommand {

    public GamerulesCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("List the Minecraft Game Rules for a World.");
        setCommandUsage("/mv gamerules" + ChatColor.GOLD + " [WORLD]");
        setArgRange(0, 1);
        addKey("mv gamerules");
        addKey("mv rules");
        addKey("mvgamerules");
        addKey("mvrules");
        addCommandExample("/mv gamerules");
        addCommandExample("/mvrules " + ChatColor.RED + "world_nether");
        setPermission("multiverse.core.gamerule.list", "Allows a player to list gamerules.", PermissionDefault.OP);
    }


    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        // We NEED a world from the command line
        final Player p;
        if (sender instanceof Player) {
            p = (Player) sender;
        }
        else {
            p = null;
        }

        if (args.size() == 0 && p == null) {
            sender.sendMessage("From the command line, WORLD is required.");
            sender.sendMessage(getCommandDesc());
            sender.sendMessage(getCommandUsage());
            sender.sendMessage("Nothing changed.");
            return;
        }

        final World world;
        if (args.size() == 0) {
            world = p.getWorld();
        } else {
            world = Bukkit.getWorld(args.get(0));
        }

        final StringBuilder gameRules = new StringBuilder();
        assert world != null;
        for (final String gameRule : world.getGameRules()) {
            if (gameRules.length() != 0) {
                gameRules.append(ChatColor.WHITE).append(", ");
            }
            gameRules.append(ChatColor.AQUA).append(gameRule).append(ChatColor.WHITE).append(": ");
            gameRules.append(ChatColor.GREEN).append(world.getGameRuleValue(gameRule));
        }
        sender.sendMessage("=== Gamerules for " + ChatColor.AQUA + world.getName() + ChatColor.WHITE + " ===");
        sender.sendMessage(gameRules.toString());
    }
}
