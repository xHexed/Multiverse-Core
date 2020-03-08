package com.onarandombox.MultiverseCore.utils;

import java.util.logging.Level;

import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.destination.CannonDestination;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.jetbrains.annotations.NotNull;

public class BukkitTravelAgent implements TravelAgent {
    private final MVTravelAgent agent;

    public BukkitTravelAgent(final MVTravelAgent agent) {
        this.agent = agent;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public BukkitTravelAgent setSearchRadius(final int radius) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSearchRadius() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public BukkitTravelAgent setCreationRadius(final int radius) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCreationRadius() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCanCreatePortal() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCanCreatePortal(final boolean create) {
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Location findOrCreate(@NotNull final Location location) {
        return getSafeLocation();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Location findPortal(@NotNull final Location location) {
        return getSafeLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createPortal(@NotNull final Location location) {
        return false;
    }

    private Location getSafeLocation() {
        // At this time, these can never use the velocity.
        if (agent.destination instanceof CannonDestination) {
            agent.core.log(Level.FINE, "Using Stock TP method. This cannon will have 0 velocity");
        }
        final SafeTTeleporter teleporter = agent.core.getSafeTTeleporter();
        Location newLoc = agent.destination.getLocation(agent.player);
        if (agent.destination.useSafeTeleporter()) {
            newLoc = teleporter.getSafeLocation(agent.player, agent.destination);
        }
        if (newLoc == null) {
            return agent.player.getLocation();
        }
        return newLoc;

    }

    public void setPortalEventTravelAgent(final PlayerPortalEvent event) {
        event.setPortalTravelAgent(this);
        event.useTravelAgent(true);
    }
}
