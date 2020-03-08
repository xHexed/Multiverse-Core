/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns a list of loaded generator plugins.
 */
public class GeneratorCommand extends MultiverseCommand {

    public GeneratorCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("World Information");
        setCommandUsage("/mv generators");
        setArgRange(0, 0);
        addKey("mv generators");
        addKey("mvgenerators");
        addKey("mv gens");
        addKey("mvgens");
        addCommandExample("/mv generators");
        setPermission("multiverse.core.generator", "Returns a list of Loaded Generator Plugins.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        Logging.info("PLEASE IGNORE the 'Plugin X does not contain any generators' message below!");
        final Plugin[] plugins = plugin.getServer().getPluginManager().getPlugins();
        final List<String> generators = new ArrayList<>();
        for (final Plugin p : plugins) {
            if (p.isEnabled() && p.getDefaultWorldGenerator("world", "") != null) {
                generators.add(p.getDescription().getName());
            }
        }
        sender.sendMessage(ChatColor.AQUA + "--- Loaded Generator Plugins ---");
        StringBuilder loadedGens = new StringBuilder();
        boolean altColor = false;
        for (final String s : generators) {
            loadedGens.append(altColor ? ChatColor.YELLOW : ChatColor.WHITE).append(s).append(" ");
            altColor = !altColor;
        }
        if (loadedGens.length() == 0) {
            loadedGens = new StringBuilder(ChatColor.RED + "No Generator Plugins found.");
        }
        sender.sendMessage(loadedGens.toString());
    }
}
