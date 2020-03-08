/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.WorldPurger;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Squid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Utility class that removes animals from worlds that don't belong there.
 */
public class SimpleWorldPurger implements WorldPurger {

    private final MultiverseCore plugin;

    private Class<Entity> ambientClass;

    public SimpleWorldPurger(final MultiverseCore plugin) {
        this.plugin = plugin;
        try {
            final Class entityClass = Class.forName("org.bukkit.entity.Ambient");
            if (Entity.class.isAssignableFrom(entityClass)) {
                ambientClass = entityClass;
            }
        }
        catch (final ClassNotFoundException ignore) { }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void purgeWorlds(final List<MultiverseWorld> worlds) {
        if (worlds == null || worlds.isEmpty()) {
            return;
        }
        for (final MultiverseWorld world : worlds) {
            purgeWorld(world);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void purgeWorld(final MultiverseWorld world) {
        if (world == null) {
            return;
        }
        final ArrayList<String> allMobs = new ArrayList<>(world.getAnimalList());
        allMobs.addAll(world.getMonsterList());
        purgeWorld(world, allMobs, !world.canAnimalsSpawn(), !world.canMonstersSpawn());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldWeKillThisCreature(final MultiverseWorld world, final Entity e) {
        final ArrayList<String> allMobs = new ArrayList<>(world.getAnimalList());
        allMobs.addAll(world.getMonsterList());
        return shouldWeKillThisCreature(e, allMobs, !world.canAnimalsSpawn(), !world.canMonstersSpawn());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void purgeWorld(final MultiverseWorld mvworld, final List<String> thingsToKill,
                           final boolean negateAnimals, final boolean negateMonsters, final CommandSender sender) {
        if (mvworld == null) {
            return;
        }
        final World world = mvworld.getCBWorld();
        if (world == null) {
            return;
        }
        int projectilesKilled = 0;
        int entitiesKilled = 0;
        final boolean specifiedAll = thingsToKill.contains("ALL");
        final boolean specifiedAnimals = thingsToKill.contains("ANIMALS") || specifiedAll;
        final boolean specifiedMonsters = thingsToKill.contains("MONSTERS") || specifiedAll;
        final List<Entity> worldEntities = world.getEntities();
        final List<LivingEntity> livingEntities = new ArrayList<>(worldEntities.size());
        final List<Projectile> projectiles = new ArrayList<>(worldEntities.size());
        for (final Entity e : worldEntities) {
            if (e instanceof Projectile) {
                final Projectile p = (Projectile) e;
                if (p.getShooter() != null) {
                    projectiles.add((Projectile) e);
                }
            }
            else if (e instanceof LivingEntity) {
                livingEntities.add((LivingEntity) e);
            }
        }
        for (final LivingEntity e : livingEntities) {
            if (killDecision(e, thingsToKill, negateAnimals, negateMonsters, specifiedAnimals, specifiedMonsters)) {
                final Iterator<Projectile> it = projectiles.iterator();
                while (it.hasNext()) {
                    final Projectile p = it.next();
                    if (Objects.equals(p.getShooter(), e)) {
                        p.remove();
                        it.remove();
                        projectilesKilled++;
                    }
                }
                e.remove();
                entitiesKilled++;
            }
        }
        if (sender != null) {
            sender.sendMessage(entitiesKilled + " entities purged from the world '" + world.getName() + "' along with " + projectilesKilled + " projectiles that belonged to them.");
        }
    }

    private boolean killDecision(final Entity e, final List<String> thingsToKill, final boolean negateAnimals,
                                 final boolean negateMonsters, final boolean specifiedAnimals, final boolean specifiedMonsters) {
        boolean negate = false;
        boolean specified = false;
        if (e instanceof Golem || e instanceof Squid || e instanceof Animals
                || (ambientClass != null && ambientClass.isInstance(e))) {
            // it's an animal
            if (specifiedAnimals && !negateAnimals) {
                Logging.finest("Removing an entity because I was told to remove all animals in world %s: %s", e.getWorld().getName(), e);
                return true;
            }
            if (specifiedAnimals)
                specified = true;
            negate = negateAnimals;
        }
        else if (e instanceof Monster || e instanceof Ghast || e instanceof Slime || e instanceof Phantom) {
            // it's a monster
            if (specifiedMonsters && !negateMonsters) {
                Logging.finest("Removing an entity because I was told to remove all monsters in world %s: %s", e.getWorld().getName(), e);
                return true;
            }
            if (specifiedMonsters)
                specified = true;
            negate = negateMonsters;
        }
        for (final String s : thingsToKill) {
            final EntityType type = EntityType.fromName(s);
            if (type != null && type.equals(e.getType())) {
                specified = true;
                if (!negate) {
                    Logging.finest("Removing an entity because it WAS specified and we are NOT negating in world %s: %s", e.getWorld().getName(), e);
                    return true;
                }
                break;
            }
        }
        if (!specified && negate) {
            Logging.finest("Removing an entity because it was NOT specified and we ARE negating in world %s: %s", e.getWorld().getName(), e);
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldWeKillThisCreature(final Entity e, final List<String> thingsToKill, final boolean negateAnimals, final boolean negateMonsters) {
        final boolean specifiedAll = thingsToKill.contains("ALL");
        final boolean specifiedAnimals = thingsToKill.contains("ANIMALS") || specifiedAll;
        final boolean specifiedMonsters = thingsToKill.contains("MONSTERS") || specifiedAll;
        return killDecision(e, thingsToKill, negateAnimals, negateMonsters, specifiedAnimals, specifiedMonsters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void purgeWorld(final MultiverseWorld mvworld, final List<String> thingsToKill, final boolean negateAnimals, final boolean negateMonsters) {
        purgeWorld(mvworld, thingsToKill, negateAnimals, negateMonsters, null);
    }
}
