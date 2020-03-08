/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.BlockSafety;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.configuration.SpawnLocation;
import com.onarandombox.MultiverseCore.configuration.SpawnSettings;
import com.onarandombox.MultiverseCore.configuration.WorldPropertyValidator;
import com.onarandombox.MultiverseCore.enums.AllowedPortalType;
import com.onarandombox.MultiverseCore.enums.EnglishChatColor;
import com.onarandombox.MultiverseCore.exceptions.PropertyDoesNotExistException;
import me.main__.util.SerializationConfig.ChangeDeniedException;
import me.main__.util.SerializationConfig.NoSuchPropertyException;
import me.main__.util.SerializationConfig.VirtualProperty;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * The implementation of a Multiverse handled world.
 */
public class MVWorld implements MultiverseWorld {
    private static final int SPAWN_LOCATION_SEARCH_TOLERANCE = 16;
    private static final int SPAWN_LOCATION_SEARCH_RADIUS = 16;

    private final MultiverseCore plugin; // Hold the Plugin Instance.
    private final String name; // The Worlds Name, EG its folder name.
    private final UUID worldUID;
    private final WorldProperties props;

    public MVWorld(final MultiverseCore plugin, final World world, final WorldProperties properties) {
        this(plugin, world, properties, true);
    }

    /*
     * We have to use setCBWorld(), setPlugin() and initPerms() to prepare this object for use.
     */
    public MVWorld(final MultiverseCore plugin, final World world, final WorldProperties properties, final boolean fixSpawn) {
        this.plugin = plugin;
        name        = world.getName();
        worldUID    = world.getUID();
        props       = properties;

        setupProperties();

        if (!fixSpawn) {
            props.setAdjustSpawn(false);
        }

        // Setup spawn separately so we can use the validator with the world spawn value..
        final SpawnLocationPropertyValidator spawnValidator = new SpawnLocationPropertyValidator();
        props.setValidator("spawn", spawnValidator);
        props.spawnLocation.setWorld(world);
        if (props.spawnLocation instanceof NullLocation) {
            final SpawnLocation newLoc = new SpawnLocation(readSpawnFromWorld(world));
            props.spawnLocation = newLoc;
            world.setSpawnLocation(newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ());
        }

        props.environment = world.getEnvironment();
        props.seed        = world.getSeed();

        initPerms();

        props.flushChanges();

        validateProperties();
    }

    private void setupProperties() {
        props.setMVWorld(this);
        props.pvp = new VirtualProperty<Boolean>() {
            @Override
            public void set(final Boolean newValue) {
                final World world = getCBWorld();
                if (world != null) {
                    world.setPVP(newValue);
                }
            }

            @Override
            public Boolean get() {
                final World world = getCBWorld();
                return world != null ? world.getPVP() : null;
            }
        };

        props.difficulty = new VirtualProperty<Difficulty>() {
            @Override
            public void set(final Difficulty newValue) {
                final World world = getCBWorld();
                if (world != null) {
                    world.setDifficulty(newValue);
                }
            }

            @Override
            public Difficulty get() {
                final World world = getCBWorld();
                return world != null ? world.getDifficulty() : null;
            }
        };

        props.keepSpawnInMemory = new VirtualProperty<Boolean>() {
            @Override
            public void set(final Boolean newValue) {
                final World world = getCBWorld();
                if (world != null) {
                    world.setKeepSpawnInMemory(newValue);
                }
            }

            @Override
            public Boolean get() {
                final World world = getCBWorld();
                return world != null ? world.getKeepSpawnInMemory() : null;
            }
        };

        props.spawn = new VirtualProperty<Location>() {
            @Override
            public void set(final Location newValue) {
                if (getCBWorld() != null)
                    getCBWorld().setSpawnLocation(newValue.getBlockX(), newValue.getBlockY(), newValue.getBlockZ());

                props.spawnLocation = new SpawnLocation(newValue);
            }

            @Override
            public Location get() {
                props.spawnLocation.setWorld(getCBWorld());
                // basically, everybody should accept our "SpawnLocation", right?
                // so just returning it should be fine
                return props.spawnLocation;
            }
        };

        props.time = new VirtualProperty<Long>() {
            @Override
            public void set(final Long newValue) {
                final World world = getCBWorld();
                if (world != null) {
                    world.setTime(newValue);
                }
            }

            @Override
            public Long get() {
                final World world = getCBWorld();
                return world != null ? world.getTime() : null;
            }
        };

        props.setValidator("scale", new ScalePropertyValidator());
        props.setValidator("respawnWorld", new RespawnWorldPropertyValidator());
        props.setValidator("allowWeather", new AllowWeatherPropertyValidator());
        props.setValidator("spawning", new SpawningPropertyValidator());
        props.setValidator("gameMode", new GameModePropertyValidator());

        //this.props.validate();
    }

    /**
     * This method is here to provide a stopgap until the add/remove/clear methods are implemented with
     * SerializationConfig.
     */
    public void validateEntitySpawns() {
        setAllowAnimalSpawn(canAnimalsSpawn());
        setAllowMonsterSpawn(canMonstersSpawn());
    }

    private void validateProperties() {
        setPVPMode(isPVPEnabled());
        setDifficulty(getDifficulty());
        setKeepSpawnInMemory(isKeepingSpawnInMemory());
        setScaling(getScaling());
        setRespawnToWorld(props.getRespawnToWorld());
        validateEntitySpawns();
        setGameMode(getGameMode());
    }

    /**
     * Initializes permissions.
     */
    private void initPerms() {
        permission = new Permission("multiverse.access." + name, "Allows access to " + name, PermissionDefault.OP);
        // This guy is special. He shouldn't be added to any parent perms.
        ignoreperm = new Permission("mv.bypass.gamemode." + name,
                                    "Allows players with this permission to ignore gamemode changes.", PermissionDefault.FALSE);

        exempt = new Permission("multiverse.exempt." + name,
                                "A player who has this does not pay to enter this world, or use any MV portals in it " + name, PermissionDefault.OP);

        limitbypassperm = new Permission("mv.bypass.playerlimit." + name,
                                         "A player who can enter this world regardless of wether its full", PermissionDefault.OP);
        try {
            plugin.getServer().getPluginManager().addPermission(permission);
            plugin.getServer().getPluginManager().addPermission(exempt);
            plugin.getServer().getPluginManager().addPermission(ignoreperm);
            plugin.getServer().getPluginManager().addPermission(limitbypassperm);
            // Add the permission and exempt to parents.
            addToUpperLists(permission);

            // Add ignore to it's parent:
            ignoreperm.addParent("mv.bypass.gamemode.*", true);
            // Add limit bypass to it's parent
            limitbypassperm.addParent("mv.bypass.playerlimit.*", true);
        }
        catch (final IllegalArgumentException e) {
            plugin.log(Level.FINER, "Permissions nodes were already added for " + name);
        }
    }

    private Location readSpawnFromWorld(final World w) {
        final Location location = w.getSpawnLocation();
        // Set the worldspawn to our configspawn
        final BlockSafety bs = plugin.getBlockSafety();
        // Verify that location was safe
        if (!bs.playerCanSpawnHereSafely(location)) {
            if (!getAdjustSpawn()) {
                plugin.log(Level.FINE, "Spawn location from world.dat file was unsafe!!");
                plugin.log(Level.FINE, "NOT adjusting spawn for '" + getAlias() + "' because you told me not to.");
                plugin.log(Level.FINE, "To turn on spawn adjustment for this world simply type:");
                plugin.log(Level.FINE, "/mvm set adjustspawn true " + getAlias());
                return location;
            }
            // If it's not, find a better one.
            final SafeTTeleporter teleporter = plugin.getSafeTTeleporter();
            plugin.log(Level.WARNING, "Spawn location from world.dat file was unsafe. Adjusting...");
            plugin.log(Level.WARNING, "Original Location: " + plugin.getLocationManipulation().strCoordsRaw(location));
            final Location newSpawn = teleporter.getSafeLocation(location,
                                                                 SPAWN_LOCATION_SEARCH_TOLERANCE, SPAWN_LOCATION_SEARCH_RADIUS);
            // I think we could also do this, as I think this is what Notch does.
            // Not sure how it will work in the nether...
            //Location newSpawn = this.spawnLocation.getWorld().getHighestBlockAt(this.spawnLocation).getLocation();
            if (newSpawn != null) {
                Logging.info("New Spawn for '%s' is located at: %s",
                             name, plugin.getLocationManipulation().locationToString(newSpawn));
                return newSpawn;
            }
            else {
                // If it's a standard end world, let's check in a better place:
                final Location newerSpawn;
                newerSpawn = bs.getTopBlock(new Location(w, 0, 0, 0));
                if (newerSpawn != null) {
                    Logging.info("New Spawn for '%s' is located at: %s",
                                 name, plugin.getLocationManipulation().locationToString(newerSpawn));
                    return newerSpawn;
                }
                else {
                    plugin.log(Level.SEVERE, "Safe spawn NOT found!!!");
                }
            }
        }
        return location;
    }

    private void addToUpperLists(final Permission perm) {
        Permission all = plugin.getServer().getPluginManager().getPermission("multiverse.*");
        Permission allWorlds = plugin.getServer().getPluginManager().getPermission("multiverse.access.*");
        Permission allExemption = plugin.getServer().getPluginManager().getPermission("multiverse.exempt.*");

        if (allWorlds == null) {
            allWorlds = new Permission("multiverse.access.*");
            plugin.getServer().getPluginManager().addPermission(allWorlds);
        }
        allWorlds.getChildren().put(perm.getName(), true);
        if (allExemption == null) {
            allExemption = new Permission("multiverse.exempt.*");
            plugin.getServer().getPluginManager().addPermission(allExemption);
        }
        allExemption.getChildren().put(exempt.getName(), true);
        if (all == null) {
            all = new Permission("multiverse.*");
            plugin.getServer().getPluginManager().addPermission(all);
        }
        all.getChildren().put("multiverse.access.*", true);
        all.getChildren().put("multiverse.exempt.*", true);

        plugin.getServer().getPluginManager().recalculatePermissionDefaults(all);
        plugin.getServer().getPluginManager().recalculatePermissionDefaults(allWorlds);
    }

    /**
     * Copies all properties from another {@link MVWorld} object.
     *
     * @param other The other world object.
     */
    public void copyValues(final MVWorld other) {
        props.copyValues(other.props);
    }

    /**
     * Copies all properties from a {@link WorldProperties} object.
     *
     * @param other The world properties object.
     */
    public void copyValues(final WorldProperties other) {
        props.copyValues(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColoredWorldString() {
        if (props.getAlias().length() == 0) {
            props.setAlias(name);
        }

        if ((props.getColor() == null) || (props.getColor().getColor() == null)) {
            props.setColor(EnglishChatColor.WHITE);
        }

        final StringBuilder nameBuilder = new StringBuilder().append(props.getColor().getColor());
        if (props.getStyle().getColor() != null)
            nameBuilder.append(props.getStyle().getColor());
        nameBuilder.append(props.getAlias()).append(ChatColor.WHITE).toString();

        return nameBuilder.toString();
    }

    private Permission permission;
    private Permission exempt;
    private Permission ignoreperm;
    private Permission limitbypassperm;

    /**
     * {@inheritDoc}
     *
     * @deprecated This is deprecated.
     */
    @Override
    @Deprecated
    public boolean clearList(final String property) {
        return clearVariable(property);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated This is deprecated.
     */
    @Override
    @Deprecated
    public boolean clearVariable(final String property) {
        final List<String> list = getOldAndEvilList(property);
        if (list == null)
            return false;
        list.clear();
        validateEntitySpawns();
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated This is deprecated.
     */
    @Override
    @Deprecated
    public boolean addToVariable(final String property, final String value) {
        final List<String> list = getOldAndEvilList(property);
        if (list == null)
            return false;
        list.add(value);
        validateEntitySpawns();
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated This is deprecated.
     */
    @Override
    @Deprecated
    public boolean removeFromVariable(final String property, final String value) {
        final List<String> list = getOldAndEvilList(property);
        if (list == null)
            return false;
        list.remove(value);
        validateEntitySpawns();
        return true;
    }

    /**
     * @deprecated This is deprecated.
     */
    @Deprecated
    private List<String> getOldAndEvilList(final String property) {
        if (property.equalsIgnoreCase("worldblacklist"))
            return props.getWorldBlacklist();
        else if (property.equalsIgnoreCase("animals"))
            return props.getAnimalList();
        else if (property.equalsIgnoreCase("monsters"))
            return props.getMonsterList();
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyValue(final String property) throws PropertyDoesNotExistException {
        try {
            return props.getProperty(property, true);
        }
        catch (final NoSuchPropertyException e) {
            throw new PropertyDoesNotExistException(property, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getCBWorld() {
        final World world = plugin.getServer().getWorld(worldUID);
        if (world == null) {
            throw new IllegalStateException("Lost reference to bukkit world '" + name + "'");
        }
        return world;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setPropertyValue(final String property, final String value) throws PropertyDoesNotExistException {
        try {
            return props.setProperty(property, value, true);
        }
        catch (final NoSuchPropertyException e) {
            throw new PropertyDoesNotExistException(property, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyHelp(final String property) throws PropertyDoesNotExistException {
        try {
            return props.getPropertyDescription(property, true);
        }
        catch (final NoSuchPropertyException e) {
            throw new PropertyDoesNotExistException(property, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Environment getEnvironment() {
        return props.getEnvironment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnvironment(final Environment environment) {
        props.setEnvironment(environment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSeed() {
        return props.getSeed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSeed(final long seed) {
        props.setSeed(seed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGenerator() {
        return props.getGenerator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGenerator(final String generator) {
        props.setGenerator(generator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPlayerLimit() {
        return props.getPlayerLimit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldType getWorldType() {
        // This variable is not settable in-game, therefore does not get a property.
        final World world = getCBWorld();
        return world != null ? world.getWorldType() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPlayerLimit(final int limit) {
        props.setPlayerLimit(limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        // This variable is not settable in-game, therefore does not get a property.
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPermissibleName() {
        return name.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAlias() {
        if (props.getAlias() == null || props.getAlias().length() == 0) {
            return name;
        }
        return props.getAlias();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlias(final String alias) {
        props.setAlias(alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAnimalsSpawn() {
        return props.canAnimalsSpawn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllowAnimalSpawn(final boolean animals) {
        props.setAllowAnimalSpawn(animals);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAnimalList() {
        // These don't fire events at the moment. Should they?
        return props.getAnimalList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canMonstersSpawn() {
        return props.canMonstersSpawn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllowMonsterSpawn(final boolean monsters) {
        props.setAllowMonsterSpawn(monsters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getMonsterList() {
        // These don't fire events at the moment. Should they?
        return props.getMonsterList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPVPEnabled() {
        return props.isPVPEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPVPMode(final boolean pvp) {
        props.setPVPMode(pvp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden() {
        return props.isHidden();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHidden(final boolean hidden) {
        props.setHidden(hidden);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getWorldBlacklist() {
        return props.getWorldBlacklist();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getScaling() {
        return props.getScaling();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setScaling(final double scaling) {
        return props.setScaling(scaling);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setColor(final String aliasColor) {
        return props.setColor(aliasColor);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated This is deprecated.
     */
    @Override
    @Deprecated
    public boolean isValidAliasColor(final String aliasColor) {
        return (EnglishChatColor.fromString(aliasColor) != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatColor getColor() {
        return props.getColor().getColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getRespawnToWorld() {
        return plugin.getServer().getWorld(props.getRespawnToWorld());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setRespawnToWorld(final String respawnToWorld) {
        if (!plugin.getMVWorldManager().isMVWorld(respawnToWorld)) return false;
        return props.setRespawnToWorld(respawnToWorld);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Permission getAccessPermission() {
        return permission;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Material getCurrency() {
        return props.getCurrency();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrency(@Nullable final Material currency) {
        props.setCurrency(currency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPrice() {
        return props.getPrice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrice(final double price) {
        props.setPrice(price);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated This is deprecated.
     */
    @Override
    @Deprecated
    public boolean getFakePVP() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Permission getExemptPermission() {
        return exempt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setGameMode(final String mode) {
        return props.setGameMode(mode);
    }

    @Override
    public boolean setGameMode(final GameMode mode) {
        return props.setGameMode(mode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameMode getGameMode() {
        return props.getGameMode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnableWeather(final boolean weather) {
        props.setEnableWeather(weather);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWeatherEnabled() {
        return props.isWeatherEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isKeepingSpawnInMemory() {
        return props.isKeepingSpawnInMemory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setKeepSpawnInMemory(final boolean value) {
        props.setKeepSpawnInMemory(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getHunger() {
        return props.getHunger();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHunger(final boolean hunger) {
        props.setHunger(hunger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getSpawnLocation() {
        return props.getSpawnLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawnLocation(final Location l) {
        props.setSpawnLocation(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Difficulty getDifficulty() {
        return props.getDifficulty();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated This is deprecated.
     */
    @Override
    @Deprecated
    public boolean setDifficulty(final String difficulty) {
        return props.setDifficulty(difficulty);
    }

    @Override
    public boolean setDifficulty(final Difficulty difficulty) {
        return props.setDifficulty(difficulty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAutoHeal() {
        return props.getAutoHeal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoHeal(final boolean heal) {
        props.setAutoHeal(heal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAdjustSpawn() {
        return props.getAdjustSpawn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAdjustSpawn(final boolean adjust) {
        props.setAdjustSpawn(adjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAutoLoad() {
        return props.getAutoLoad();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoLoad(final boolean load) {
        props.setAutoLoad(load);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBedRespawn() {
        return props.getBedRespawn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBedRespawn(final boolean respawn) {
        props.setBedRespawn(respawn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAllPropertyNames() {
        return props.getAllPropertyNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTime() {
        return props.getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setTime(final String timeAsString) {
        return props.setTime(timeAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allowPortalMaking(final AllowedPortalType portalType) {
        props.allowPortalMaking(portalType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatColor getStyle() {
        return props.getStyle().getColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setStyle(final String style) {
        return props.setStyle(style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAllowFlight() {
        return props.getAllowFlight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllowFlight(final boolean allowFlight) {
        props.setAllowFlight(allowFlight);
    }

    @Override
    public String toString() {
        final JSONObject jsonData = new JSONObject();
        jsonData.put("Name", name);
        jsonData.put("Env", getEnvironment().toString());
        jsonData.put("Type", getWorldType().toString());
        jsonData.put("Gen", getGenerator());
        final JSONObject topLevel = new JSONObject();
        topLevel.put(getClass().getSimpleName() + "@" + hashCode(), jsonData);
        return topLevel.toString();
    }

    /**
     * Null-location.
     */
    @SerializableAs("MVNullLocation (It's a bug if you see this in your config file)")
    public static final class NullLocation extends SpawnLocation {
        public NullLocation() {
            super(0, -1, 0);
        }

        /**
         * Let Bukkit be able to deserialize this.
         *
         * @param args The map.
         *
         * @return The deserialized object.
         */
        public static NullLocation deserialize(final Map<String, Object> args) {
            return new NullLocation();
        }

        @NotNull
        @Override
        public Location clone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> serialize() {
            return Collections.emptyMap();
        }

        @NotNull
        @Override
        public Vector toVector() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return -1;
        }

        @Override
        public String toString() {
            return "NULL LOCATION";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllowedPortalType getAllowedPortals() {
        return props.getAllowedPortals();
    }

    /**
     * Validates the scale-property.
     */
    private final class ScalePropertyValidator extends WorldPropertyValidator<Double> {
        @Override
        public Double validateChange(final String property, final Double newValue, final Double oldValue,
                                     final MVWorld object) throws ChangeDeniedException {
            if (newValue <= 0) {
                plugin.log(Level.FINE, "Someone tried to set a scale <= 0, aborting!");
                throw new ChangeDeniedException();
            }
            return super.validateChange(property, newValue, oldValue, object);
        }
    }

    /**
     * Validates the respawnWorld-property.
     */
    private final class RespawnWorldPropertyValidator extends WorldPropertyValidator<String> {
        @Override
        public String validateChange(final String property, final String newValue, final String oldValue,
                                     final MVWorld object) throws ChangeDeniedException {
            if (!newValue.isEmpty() && !plugin.getMVWorldManager().isMVWorld(newValue))
                throw new ChangeDeniedException();
            return super.validateChange(property, newValue, oldValue, object);
        }
    }

    /**
     * Used to apply the allowWeather-property.
     */
    private final class AllowWeatherPropertyValidator extends WorldPropertyValidator<Boolean> {
        @Override
        public Boolean validateChange(final String property, final Boolean newValue, final Boolean oldValue,
                                      final MVWorld object) throws ChangeDeniedException {
            if (!newValue) {
                final World world = getCBWorld();
                if (world != null) {
                    world.setStorm(false);
                    world.setThundering(false);
                }
            }
            return super.validateChange(property, newValue, oldValue, object);
        }
    }

    /**
     * Used to apply the spawning-property.
     */
    private final class SpawningPropertyValidator extends WorldPropertyValidator<SpawnSettings> {
        @Override
        public SpawnSettings validateChange(final String property, final SpawnSettings newValue, final SpawnSettings oldValue,
                                            final MVWorld object) throws ChangeDeniedException {
            final boolean allowMonsters;
            final boolean allowAnimals;
            if (getAnimalList().isEmpty()) {
                allowAnimals = canAnimalsSpawn();
            }
            else {
                allowAnimals = true;
            }
            if (getMonsterList().isEmpty()) {
                allowMonsters = canMonstersSpawn();
            }
            else {
                allowMonsters = true;
            }
            final World world = getCBWorld();
            if (world != null) {
                if (props.getAnimalSpawnRate() != -1) {
                    world.setTicksPerAnimalSpawns(props.getAnimalSpawnRate());
                }
                if (props.getMonsterSpawnRate() != -1) {
                    world.setTicksPerMonsterSpawns(props.getMonsterSpawnRate());
                }
                world.setSpawnFlags(allowMonsters, allowAnimals);
            }
            if (MultiverseCoreConfiguration.getInstance().isAutoPurgeEnabled()) {
                plugin.getMVWorldManager().getTheWorldPurger().purgeWorld(MVWorld.this);
            }
            return super.validateChange(property, newValue, oldValue, object);
        }
    }

    /**
     * Used to apply the gameMode-property.
     */
    private final class GameModePropertyValidator extends WorldPropertyValidator<GameMode> {
        @Override
        public GameMode validateChange(final String property, final GameMode newValue, final GameMode oldValue,
                                       final MVWorld object) throws ChangeDeniedException {
            for (final Player p : plugin.getServer().getWorld(getName()).getPlayers()) {
                plugin.log(Level.FINER, String.format("Setting %s's GameMode to %s",
                                                      p.getName(), newValue.toString()));
                plugin.getPlayerListener().handleGameModeAndFlight(p, MVWorld.this);
            }
            return super.validateChange(property, newValue, oldValue, object);
        }
    }

    /**
     * Validator for the spawnLocation-property.
     */
    private final class SpawnLocationPropertyValidator extends WorldPropertyValidator<Location> {
        @Override
        public Location validateChange(final String property, Location newValue, final Location oldValue,
                                       final MVWorld object) throws ChangeDeniedException {
            if (newValue == null)
                throw new ChangeDeniedException();
            if (props.getAdjustSpawn()) {
                final BlockSafety bs = plugin.getBlockSafety();
                // verify that the location is safe
                if (!bs.playerCanSpawnHereSafely(newValue)) {
                    // it's not ==> find a better one!
                    plugin.log(Level.WARNING, String.format("Somebody tried to set the spawn location for '%s' to an unsafe value! Adjusting...", getAlias()));
                    plugin.log(Level.WARNING, "Old Location: " + plugin.getLocationManipulation().strCoordsRaw(oldValue));
                    plugin.log(Level.WARNING, "New (unsafe) Location: " + plugin.getLocationManipulation().strCoordsRaw(newValue));
                    final SafeTTeleporter teleporter = plugin.getSafeTTeleporter();
                    newValue = teleporter.getSafeLocation(newValue, SPAWN_LOCATION_SEARCH_TOLERANCE, SPAWN_LOCATION_SEARCH_RADIUS);
                    if (newValue == null) {
                        plugin.log(Level.WARNING, "Couldn't fix the location. I have to abort the spawn location-change :/");
                        throw new ChangeDeniedException();
                    }
                    plugin.log(Level.WARNING, "New (safe) Location: " + plugin.getLocationManipulation().strCoordsRaw(newValue));
                }
            }
            return super.validateChange(property, newValue, oldValue, object);
        }
    }
}
