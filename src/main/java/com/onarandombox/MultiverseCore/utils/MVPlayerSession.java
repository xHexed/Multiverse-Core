/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.api.MultiverseCoreConfig;
import org.bukkit.entity.Player;

import java.util.Date;

/**
 * A player-session.
 */
public class MVPlayerSession {

    private static final long messageLast = 0L; // Timestamp for the Players last Alert Message.
    private final Player player; // Player holder, may be unnecessary.
    private final MultiverseCoreConfig config; // Configuration file to find out Cooldown Timers.
    private long teleportLast; // Timestamp for the Players last Portal Teleportation.

    public MVPlayerSession(final Player player, final MultiverseCoreConfig config) {
        this.player = player;
        this.config = config;
        // this.bedSpawn = null;
    }

    /** Update the Teleport time. */
    public void teleport() {
        teleportLast = (new Date()).getTime();
    }

    /**
     * Grab whether the cooldown on Portal use has expired or not.
     * @return True if the {@link Player} associated with this player-session is teleportable.
     */
    public boolean getTeleportable() {
        final long time = (new Date()).getTime();
        return ((time - teleportLast) > config.getTeleportCooldown());
    }
}
