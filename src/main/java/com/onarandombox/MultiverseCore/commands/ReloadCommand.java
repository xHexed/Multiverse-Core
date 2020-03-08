/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * Reloads worlds.yml and config.yml.
 */
public class ReloadCommand extends MultiverseCommand {

    public ReloadCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Reload Configs");
        setCommandUsage("/mv reload");
        setArgRange(0, 0);
        addKey("mvreload");
        addKey("mv reload");
        addCommandExample("/mv reload");
        setPermission("multiverse.core.reload", "Reloads worlds.yml and config.yml.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        sender.sendMessage(ChatColor.GOLD + "Reloading all Multiverse Plugin configs...");
        plugin.loadConfigs();
        plugin.getAnchorManager().loadAnchors();
        plugin.getMVWorldManager().loadWorlds(true);

        final List<String> configsLoaded = new ArrayList<>();
        configsLoaded.add("Multiverse-Core - config.yml");
        configsLoaded.add("Multiverse-Core - worlds.yml");
        configsLoaded.add("Multiverse-Core - anchors.yml");
        // Create the event
        final MVConfigReloadEvent configReload = new MVConfigReloadEvent(configsLoaded);
        // Fire it off
        plugin.getServer().getPluginManager().callEvent(configReload);
        for (final String s : configReload.getAllConfigsLoaded()) {
            sender.sendMessage(s);
        }

        sender.sendMessage(ChatColor.GREEN + "Reload Complete!");
    }

}
