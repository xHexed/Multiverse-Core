/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Manages anchors.
 */
public class AnchorManager {
    private final MultiverseCore plugin;
    private Map<String, Location> anchors;
    private FileConfiguration anchorConfig;

    public AnchorManager(final MultiverseCore plugin) {
        this.plugin = plugin;
        anchors     = new HashMap<>();
    }

    /**
     * Loads all anchors.
     */
    public void loadAnchors() {
        anchors      = new HashMap<>();
        anchorConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "anchors.yml"));
        ensureConfigIsPrepared();
        final ConfigurationSection anchorsSection = anchorConfig.getConfigurationSection("anchors");
        assert anchorsSection != null;
        final Set<String> anchorKeys = anchorsSection.getKeys(false);
        for (final String key : anchorKeys) {
            //world:x,y,z:pitch:yaw
            final Location anchorLocation = plugin.getLocationManipulation().stringToLocation(anchorsSection.getString(key, ""));
            if (anchorLocation != null) {
                Logging.config("Loading anchor:  '%s'...", key);
                anchors.put(key, anchorLocation);
            }
            else {
                Logging.warning("The location for anchor '%s' is INVALID.", key);
            }

        }
    }

    private void ensureConfigIsPrepared() {
        if (anchorConfig.getConfigurationSection("anchors") == null) {
            anchorConfig.createSection("anchors");
        }
    }

    /**
     * Saves all anchors.
     * @return True if all anchors were successfully saved.
     */
    public boolean saveAnchors() {
        try {
            anchorConfig.save(new File(plugin.getDataFolder(), "anchors.yml"));
            return true;
        }
        catch (final IOException e) {
            plugin.log(Level.SEVERE, "Failed to save anchors.yml. Please check your file permissions.");
            return false;
        }
    }

    /**
     * Gets the {@link Location} associated with an anchor.
     *
     * @param anchor The name of the anchor.
     *
     * @return The {@link Location}.
     */
    public Location getAnchorLocation(final String anchor) {
        if (anchors.containsKey(anchor)) {
            return anchors.get(anchor);
        }
        return null;
    }

    /**
     * Saves an anchor.
     *
     * @param anchor   The name of the anchor.
     * @param location The location of the anchor as string.
     *
     * @return True if the anchor was successfully saved.
     */
    public boolean saveAnchorLocation(final String anchor, final String location) {
        final Location parsed = plugin.getLocationManipulation().stringToLocation(location);
        return parsed != null && saveAnchorLocation(anchor, parsed);
    }

    /**
     * Saves an anchor.
     *
     * @param anchor The name of the anchor.
     * @param l      The {@link Location} of the anchor.
     *
     * @return True if the anchor was successfully saved.
     */
    public boolean saveAnchorLocation(final String anchor, final Location l) {
        if (l == null) {
            return false;
        }
        anchorConfig.set("anchors." + anchor, plugin.getLocationManipulation().locationToString(l));
        anchors.put(anchor, l);
        return saveAnchors();
    }

    /**
     * Gets all anchors.
     * @return An unmodifiable {@link Set} containing all anchors.
     */
    public Set<String> getAllAnchors() {
        return Collections.unmodifiableSet(anchors.keySet());
    }

    /**
     * Gets all anchors that the specified {@link Player} can access.
     *
     * @param p The {@link Player}.
     *
     * @return An unmodifiable {@link Set} containing all anchors the specified {@link Player} can access.
     */
    public Set<String> getAnchors(final Player p) {
        if (p == null) {
            return anchors.keySet();
        }
        final Set<String> myAnchors = new HashSet<>();
        for (final Map.Entry<String, Location> entry : anchors.entrySet()) {
            final String anchor = entry.getKey();
            final Location ancLoc = entry.getValue();
            if (ancLoc == null) {
                continue;
            }
            final String worldPerm = "multiverse.access." + ancLoc.getWorld().getName();
            // Add to the list if we're not enforcing access
            // OR
            // We are enforcing access and the user has the permission.
            if (!plugin.getMVConfig().getEnforceAccess() ||
                    (plugin.getMVConfig().getEnforceAccess() && p.hasPermission(worldPerm))) {
                myAnchors.add(anchor);
            }
            else {
                Logging.finer(String.format("Not adding anchor %s to the list, user %s doesn't have the %s " +
                                                    "permission and 'enforceaccess' is enabled!",
                                            anchor, p.getName(), worldPerm));
            }
        }
        return Collections.unmodifiableSet(myAnchors);
    }

    /**
     * Deletes the specified anchor.
     *
     * @param s The name of the anchor.
     *
     * @return True if the anchor was successfully deleted.
     */
    public boolean deleteAnchor(final String s) {
        if (anchors.containsKey(s)) {
            anchors.remove(s);
            anchorConfig.set("anchors." + s, null);
            return saveAnchors();
        }
        return false;
    }
}
