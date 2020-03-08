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

import java.util.List;
import java.util.Map;

/**
 * Allows you to set Global MV Variables.
 */
public class ConfigCommand extends MultiverseCommand {
    public ConfigCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Configuration");
        setCommandUsage("/mv config " + ChatColor.GREEN + "{PROPERTY} {VALUE}");
        setArgRange(1, 2);
        addKey("mv config");
        addKey("mvconfig");
        addKey("mv conf");
        addKey("mvconf");
        addCommandExample("/mv config show");
        addCommandExample("/mv config " + ChatColor.GREEN + "debug" + ChatColor.AQUA + " 3");
        addCommandExample("/mv config " + ChatColor.GREEN + "enforceaccess" + ChatColor.AQUA + " false");
        setPermission("multiverse.core.config", "Allows you to set Global MV Variables.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        if (args.size() <= 1) {
            final StringBuilder builder = new StringBuilder();
            final Map<String, Object> serializedConfig = plugin.getMVConfig().serialize();
            for (final Map.Entry<String, Object> entry : serializedConfig.entrySet()) {
                builder.append(ChatColor.GREEN);
                builder.append(entry.getKey());
                builder.append(ChatColor.WHITE).append(" = ").append(ChatColor.GOLD);
                builder.append(entry.getValue().toString());
                builder.append(ChatColor.WHITE).append(", ");
            }
            String message = builder.toString();
            message = message.substring(0, message.length() - 2);
            sender.sendMessage(message);
            return;
        }
        if (!plugin.getMVConfig().setConfigProperty(args.get(0).toLowerCase(), args.get(1))) {
            sender.sendMessage(String.format("%sSetting '%s' to '%s' failed!", ChatColor.RED, args.get(0).toLowerCase(), args.get(1)));
            return;
        }

        // special rule
        if (args.get(0).equalsIgnoreCase("firstspawnworld")) {
            // Don't forget to set the world!
            plugin.getMVWorldManager().setFirstSpawnWorld(args.get(1));
        }

        if (plugin.saveMVConfigs()) {
            sender.sendMessage(ChatColor.GREEN + "SUCCESS!" + ChatColor.WHITE + " Values were updated successfully!");
            plugin.loadConfigs();
        }
        else {
            sender.sendMessage(ChatColor.RED + "FAIL!" + ChatColor.WHITE + " Check your console for details!");
        }
    }
}
