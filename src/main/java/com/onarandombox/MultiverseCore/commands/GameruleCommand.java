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
public class GameruleCommand extends MultiverseCommand {

    public GameruleCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Set a Minecraft Game Rule for a World.");
        setCommandUsage("/mv gamerule " + ChatColor.GREEN + "{RULE} {VALUE}" + ChatColor.GOLD + " [WORLD]");
        setArgRange(2, 3);
        addKey("mv gamerule");
        addKey("mv rule");
        addKey("mvgamerule");
        addKey("mvrule");
        addCommandExample("/mv gamerule " + ChatColor.GREEN + "doMobLoot false");
        addCommandExample("/mvrule " + ChatColor.GREEN + "keepInventory true " + ChatColor.RED + "world_nether");
        setPermission("multiverse.core.gamerule.set", "Allows a player to set a gamerule.", PermissionDefault.OP);
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

        if (args.size() == 2 && p == null) {
            sender.sendMessage("From the command line, WORLD is required.");
            sender.sendMessage(getCommandDesc());
            sender.sendMessage(getCommandUsage());
            sender.sendMessage("Nothing changed.");
            return;
        }

        final String gameRule = args.get(0);
        final String value = args.get(1);
        final World world;
        if (args.size() == 2) {
            world = p.getWorld();
        } else {
            world = Bukkit.getWorld(args.get(2));
        }

        assert world != null;
        if (world.setGameRuleValue(gameRule, value)) {
            sender.sendMessage(ChatColor.GREEN + "Success!" + ChatColor.WHITE + " Gamerule " + ChatColor.AQUA + gameRule
                    + ChatColor.WHITE + " was set to " + ChatColor.GREEN + value);
        } else {
            sender.sendMessage(ChatColor.RED + "Failure!" + ChatColor.WHITE + " Gamerule " + ChatColor.AQUA + gameRule
                    + ChatColor.WHITE + " cannot be set to " + ChatColor.RED + value);
        }
    }
}
