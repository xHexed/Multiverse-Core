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
import com.onarandombox.MultiverseCore.api.WorldPurger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Removes a type of mob from a world.
 */
public class PurgeCommand extends MultiverseCommand {
    private final MVWorldManager worldManager;

    public PurgeCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Purge World of Creatures");
        setCommandUsage("/mv purge" + ChatColor.GOLD + " [WORLD|all] " + ChatColor.GREEN + "{all|animals|monsters|MOBNAME}");
        setArgRange(1, 2);
        addKey("mvpurge");
        addKey("mv purge");
        addCommandExample("/mv purge " + ChatColor.GREEN + "all");
        addCommandExample("/mv purge " + ChatColor.GOLD + "all " + ChatColor.GREEN + "all");
        addCommandExample("/mv purge " + ChatColor.GREEN + "monsters");
        addCommandExample("/mv purge " + ChatColor.GOLD + "all " + ChatColor.GREEN + "animals");
        addCommandExample("/mv purge " + ChatColor.GOLD + "MyWorld " + ChatColor.GREEN + "squid");
        addCommandExample("/mv purge " + ChatColor.GOLD + "MyWorld_nether " + ChatColor.GREEN + "ghast");
        setPermission("multiverse.core.purge", "Removed the specified type of mob from the specified world.", PermissionDefault.OP);
        worldManager = this.plugin.getMVWorldManager();
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        Player p = null;
        if (sender instanceof Player) {
            p = (Player) sender;
        }
        if (args.size() == 1 && p == null) {
            sender.sendMessage("This command requires a WORLD when being run from the console!");
            sender.sendMessage(getCommandUsage());
            return;
        }
        final String worldName;
        final String deathName;
        if (args.size() == 1) {
            worldName = p.getWorld().getName();
            deathName = args.get(0);
        }
        else {
            worldName = args.get(0);
            deathName = args.get(1);
        }

        if (!worldName.equalsIgnoreCase("all") && !worldManager.isMVWorld(worldName)) {
            plugin.showNotMVWorldMessage(sender, worldName);
            sender.sendMessage("It cannot be purged.");
            return;
        }

        final List<MultiverseWorld> worldsToRemoveEntitiesFrom = new ArrayList<>();
        // Handle all case any user who names a world "all" should know better...
        if (worldName.equalsIgnoreCase("all")) {
            worldsToRemoveEntitiesFrom.addAll(worldManager.getMVWorlds());
        }
        else {
            worldsToRemoveEntitiesFrom.add(worldManager.getMVWorld(worldName));
        }

        final WorldPurger purger = worldManager.getTheWorldPurger();
        final ArrayList<String> thingsToKill = new ArrayList<>();
        if (deathName.equalsIgnoreCase("all") || deathName.equalsIgnoreCase("animals") || deathName.equalsIgnoreCase("monsters")) {
            thingsToKill.add(deathName.toUpperCase());
        }
        else {
            Collections.addAll(thingsToKill, deathName.toUpperCase().split(","));
        }
        for (final MultiverseWorld w : worldsToRemoveEntitiesFrom) {
            purger.purgeWorld(w, thingsToKill, false, false, sender);
        }
    }
}
