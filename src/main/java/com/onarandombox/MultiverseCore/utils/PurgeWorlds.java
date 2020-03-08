/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import com.onarandombox.MultiverseCore.api.WorldPurger;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Squid;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Utility class that removes animals from worlds that don't belong there.
 *
 * @deprecated Use instead: {@link WorldPurger} and {@link SimpleWorldPurger}.
 */
@Deprecated
public class PurgeWorlds {

    private static final Pattern CRAFT = Pattern.compile("Craft");
    private final MultiverseCore plugin;

    public PurgeWorlds(final MultiverseCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Synchronizes the given world with it's settings.
     *
     * @param sender The {@link CommandSender} who is requesting the world be purged.
     * @param worlds A list of {@link MultiverseWorld}
     */
    public void purgeWorlds(final CommandSender sender, final List<MultiverseWorld> worlds) {
        if (worlds == null || worlds.isEmpty()) {
            return;
        }
        for (final MultiverseWorld world : worlds) {
            purgeWorld(sender, world);
        }
    }

    /**
     * Convenience method for {@link #purgeWorld(CommandSender, MultiverseWorld, List, boolean, boolean)} that takes the settings from the world-config.
     *
     * @param sender The {@link CommandSender} that initiated the action
     * @param world  The {@link MultiverseWorld}.
     */
    public void purgeWorld(final CommandSender sender, final MultiverseWorld world) {
        if (world == null) {
            return;
        }
        final ArrayList<String> allMobs = new ArrayList<>(world.getAnimalList());
        allMobs.addAll(world.getMonsterList());
        purgeWorld(sender, world, allMobs, !world.canAnimalsSpawn(), !world.canMonstersSpawn());
    }

    /**
     * Clear all animals/monsters that do not belong to a world according to the config.
     *
     * @param sender         The {@link CommandSender} that initiated the action.
     * @param mvworld        The {@link MultiverseWorld}.
     * @param thingsToKill   A {@link List} of animals/monsters to be killed.
     * @param negateAnimals  Whether the monsters in the list should be negated.
     * @param negateMonsters Whether the animals in the list should be negated.
     */
    public void purgeWorld(final CommandSender sender, final MultiverseWorld mvworld, final List<String> thingsToKill, final boolean negateAnimals, final boolean negateMonsters) {
        if (mvworld == null) {
            return;
        }
        final World world = plugin.getServer().getWorld(mvworld.getName());
        if (world == null) {
            return;
        }
        int entitiesKilled = 0;
        for (final Entity e : world.getEntities()) {
            plugin.log(Level.FINEST, "Entity list (aval for purge) from WORLD < " + mvworld.getName() + " >: " + e.toString());

            // Check against Monsters
            if (killMonster(mvworld, e, thingsToKill, negateMonsters)) {
                entitiesKilled++;
                continue;
            }
            // Check against Animals
            if (killCreature(mvworld, e, thingsToKill, negateAnimals)) {
                entitiesKilled++;
            }

        }
        if (sender != null) {
            sender.sendMessage(entitiesKilled + " entities purged from the world '" + world.getName() + "'");
        }
    }

    private boolean killCreature(final MultiverseWorld mvworld, final Entity e, final List<String> creaturesToKill, final boolean negate) {
        final String entityName = CRAFT.matcher(e.toString()).replaceAll("").toUpperCase();
        if (e instanceof Squid || e instanceof Animals) {
            if (creaturesToKill.contains(entityName) || creaturesToKill.contains("ALL") || creaturesToKill.contains("ANIMALS")) {
                if (!negate) {
                    e.remove();
                    return true;
                }
            }
            else {
                if (negate) {
                    e.remove();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Will kill the monster if it's in the list UNLESS the NEGATE boolean is set, then it will kill it if it's NOT.
     */
    private boolean killMonster(final MultiverseWorld mvworld, final Entity e, final List<String> creaturesToKill, final boolean negate) {
        final String entityName;
        if (e instanceof EnderDragon) {
            entityName = "ENDERDRAGON";
        }
        else {
            entityName = CRAFT.matcher(e.toString()).replaceAll("").toUpperCase();
        }
        if (e instanceof Slime || e instanceof Monster || e instanceof Ghast || e instanceof EnderDragon) {
            plugin.log(Level.FINEST, "Looking at a monster: " + e);
            if (creaturesToKill.contains(entityName) || creaturesToKill.contains("ALL") || creaturesToKill.contains("MONSTERS")) {
                if (!negate) {
                    plugin.log(Level.FINEST, "Removing a monster: " + e);
                    e.remove();
                    return true;
                }
            } else {
                if (negate) {
                    plugin.log(Level.FINEST, "Removing a monster: " + e);
                    e.remove();
                    return true;
                }
            }
        }
        return false;
    }

}
