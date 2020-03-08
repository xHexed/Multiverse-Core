/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * Multiverse's World {@link Listener}.
 */
public class MVWorldListener implements Listener {
    private final MultiverseCore plugin;
    private final MVWorldManager worldManager;

    public MVWorldListener(final MultiverseCore plugin) {
        this.plugin  = plugin;
        worldManager = plugin.getMVWorldManager();
    }

    /**
     * This method is called when Bukkit fires off a WorldUnloadEvent.
     *
     * @param event The Event that was fired.
     */
    @EventHandler
    public void unloadWorld(final WorldUnloadEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.getWorld();
        final World world = event.getWorld();
        plugin.getMVWorldManager().unloadWorld(world.getName(), false);
    }

    /**
     * This method is called when Bukkit fires off a WorldLoadEvent.
     *
     * @param event The Event that was fired.
     */
    @EventHandler
    public void loadWorld(final WorldLoadEvent event) {
        final World world = event.getWorld();
        if (plugin.getMVWorldManager().getUnloadedWorlds().contains(world.getName())) {
            plugin.getMVWorldManager().loadWorld(world.getName());
        }
        final MultiverseWorld mvWorld = plugin.getMVWorldManager().getMVWorld(world);
        if (mvWorld != null) {
            // This is where we can temporarily fix those pesky property issues!
            world.setPVP(mvWorld.isPVPEnabled());
            world.setDifficulty(mvWorld.getDifficulty());
        }
    }
}
