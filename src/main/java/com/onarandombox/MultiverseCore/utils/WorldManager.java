/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.MultiverseCoreConfiguration;
import com.onarandombox.MultiverseCore.WorldProperties;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.api.WorldPurger;
import com.onarandombox.MultiverseCore.event.MVWorldDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Public facing API to add/remove Multiverse worlds.
 */
public class WorldManager implements MVWorldManager {
    private final MultiverseCore plugin;
    private final WorldPurger worldPurger;
    private final Map<String, MultiverseWorld> worlds;
    private Map<String, WorldProperties> worldsFromTheConfig;
    private FileConfiguration configWorlds;
    private Map<String, String> defaultGens;
    private String firstSpawn;

    public WorldManager(final MultiverseCore core) {
        plugin              = core;
        worldsFromTheConfig = new HashMap<>();
        worlds              = new ConcurrentHashMap<>();
        worldPurger         = new SimpleWorldPurger(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getDefaultWorldGenerators() {
        defaultGens = new HashMap<>();
        final File[] files = plugin.getServerFolder().listFiles((file, s) -> s.equalsIgnoreCase("bukkit.yml"));
        if (files != null && files.length == 1) {
            final FileConfiguration bukkitConfig = YamlConfiguration.loadConfiguration(files[0]);
            if (bukkitConfig.isConfigurationSection("worlds")) {
                final Set<String> keys = bukkitConfig.getConfigurationSection("worlds").getKeys(false);
                for (final String key : keys) {
                    defaultGens.put(key, bukkitConfig.getString("worlds." + key + ".generator", ""));
                }
            }
        }
        else {
            plugin.log(Level.WARNING, "Could not read 'bukkit.yml'. Any Default worldgenerators will not be loaded!");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #cloneWorld(String, String)} instead.
     */
    @Override
    @Deprecated
    public boolean cloneWorld(final String oldName, final String newName, final String generator) {
        return cloneWorld(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cloneWorld(String oldName, final String newName) {
        // Make sure we already know about the old world and that we don't
        // already know about the new world.
        if (!worldsFromTheConfig.containsKey(oldName)) {
            for (final Map.Entry<String, WorldProperties> entry : worldsFromTheConfig.entrySet()) {
                if (oldName.equals(entry.getValue().getAlias())) {
                    oldName = entry.getKey();
                    break;
                }
            }
            if (!worldsFromTheConfig.containsKey(oldName)) {
                Logging.warning("Old world '%s' does not exist", oldName);
                return false;
            }
        }
        if (isMVWorld(newName)) {
            Logging.warning("New world '%s' already exists", newName);
            return false;
        }

        final File oldWorldFile = new File(plugin.getServer().getWorldContainer(), oldName);
        final File newWorldFile = new File(plugin.getServer().getWorldContainer(), newName);

        // Make sure the new world doesn't exist outside of multiverse.
        if (newWorldFile.exists()) {
            Logging.warning("File for new world '%s' already exists", newName);
            return false;
        }

        // Load the old world... but just the metadata.
        boolean wasJustLoaded = false;
        boolean wasLoadSpawn = false;
        if (plugin.getServer().getWorld(oldName) == null) {
            wasJustLoaded = true;
            final WorldProperties props = worldsFromTheConfig.get(oldName);
            wasLoadSpawn = props.isKeepingSpawnInMemory();
            if (wasLoadSpawn) {
                // No chunks please.
                props.setKeepSpawnInMemory(false);
            }
            if (!loadWorld(oldName)) {
                return false;
            }
            plugin.getServer().getWorld(oldName).setAutoSave(false);
        }

        // Grab a bit of metadata from the old world.
        MVWorld oldWorld = (MVWorld) getMVWorld(oldName);
        final Environment environment = oldWorld.getEnvironment();
        final String seedString = String.valueOf(oldWorld.getSeed());
        final WorldType worldType = oldWorld.getWorldType();
        final Boolean generateStructures = oldWorld.getCBWorld().canGenerateStructures();
        final String generator = oldWorld.getGenerator();
        final boolean useSpawnAdjust = oldWorld.getAdjustSpawn();

        // Don't need the loaded world anymore.
        if (wasJustLoaded) {
            unloadWorld(oldName, true);
            oldWorld = null;
            if (wasLoadSpawn) {
                worldsFromTheConfig.get(oldName).setKeepSpawnInMemory(true);
            }
        }

        boolean wasAutoSave = false;
        if (oldWorld != null && oldWorld.getCBWorld().isAutoSave()) {
            wasAutoSave = true;
            Logging.config("Saving world '%s'", oldName);
            oldWorld.getCBWorld().setAutoSave(false);
            oldWorld.getCBWorld().save();
        }
        Logging.config("Copying files for world '%s'", oldName);
        if (!FileUtils.copyFolder(oldWorldFile, newWorldFile, Logging.getLogger())) {
            Logging.warning("Failed to copy files for world '%s', see the log info", newName);
            return false;
        }
        if (oldWorld != null && wasAutoSave) {
            oldWorld.getCBWorld().setAutoSave(true);
        }

        final File uidFile = new File(newWorldFile, "uid.dat");
        if (uidFile.exists() && !uidFile.delete()) {
            Logging.warning("Failed to delete unique ID file for world '%s'", newName);
            return false;
        }

        if (newWorldFile.exists()) {
            Logging.fine("Succeeded at copying files");
            if (addWorld(newName, environment, seedString, worldType, generateStructures, generator, useSpawnAdjust)) {
                // getMVWorld() doesn't actually return an MVWorld
                Logging.fine("Succeeded at importing world");
                final MVWorld newWorld = (MVWorld) getMVWorld(newName);
                newWorld.copyValues(worldsFromTheConfig.get(oldName));
                // don't keep the alias the same -- that would be useless
                newWorld.setAlias(null);
                return true;
            }
        }
        Logging.warning("Failed to copy files for world '%s', see the log info", newName);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addWorld(final String name, final Environment env, final String seedString, final WorldType type, final Boolean generateStructures,
                            final String generator) {
        return addWorld(name, env, seedString, type, generateStructures, generator, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addWorld(final String name, final Environment env, final String seedString, final WorldType type, final Boolean generateStructures,
                            final String generator, final boolean useSpawnAdjust) {
        if (name.equalsIgnoreCase("plugins") || name.equalsIgnoreCase("logs")) {
            return false;
        }
        Long seed = null;
        final WorldCreator c = new WorldCreator(name);
        if (seedString != null && seedString.length() > 0) {
            try {
                seed = Long.parseLong(seedString);
            }
            catch (final NumberFormatException numberformatexception) {
                seed = (long) seedString.hashCode();
            }
            c.seed(seed);
        }

        // TODO: Use the fancy kind with the commandSender
        if (generator != null && generator.length() != 0) {
            c.generator(generator);
        }
        c.environment(env);
        if (type != null) {
            c.type(type);
        }
        if (generateStructures != null) {
            c.generateStructures(generateStructures);
        }

        // Important: doLoad() needs the MVWorld-object in worldsFromTheConfig
        if (!worldsFromTheConfig.containsKey(name)) {
            final WorldProperties props = new WorldProperties(useSpawnAdjust, env);
            worldsFromTheConfig.put(name, props);
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("Loading World & Settings - '").append(name).append("'");
        builder.append(" - Env: ").append(env);
        builder.append(" - Type: ").append(type);
        if (seed != null) {
            builder.append(" & seed: ").append(seed);
        }
        if (generator != null) {
            builder.append(" & generator: ").append(generator);
        }
        Logging.info(builder.toString());

        if (!doLoad(c, true)) {
            plugin.log(Level.SEVERE, "Failed to Create/Load the world '" + name + "'");
            return false;
        }

        // set generator (special case because we can't read it from org.bukkit.World)
        worlds.get(name).setGenerator(generator);

        saveWorldsConfig();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChunkGenerator getChunkGenerator(final String generator, final String generatorID, final String worldName) {
        if (generator == null) {
            return null;
        }

        final Plugin myPlugin = plugin.getServer().getPluginManager().getPlugin(generator);
        if (myPlugin == null) {
            return null;
        }
        else {
            return plugin.getUnsafeCallWrapper().wrap(() -> myPlugin.getDefaultWorldGenerator(worldName, generatorID), myPlugin.getName(), "Failed to get the default chunk generator: %s");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeWorldFromConfig(final String name) {
        if (!unloadWorld(name)) {
            return false;
        }
        if (worldsFromTheConfig.containsKey(name)) {
            worldsFromTheConfig.remove(name);
            Logging.info("World '%s' was removed from config.yml", name);

            saveWorldsConfig();
            return true;
        }
        else {
            Logging.info("World '%s' was already removed from config.yml", name);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseWorld getFirstSpawnWorld() {
        final MultiverseWorld world = getMVWorld(firstSpawn);
        if (world == null) {
            // If the spawn world was unloaded, get the default world
            plugin.log(Level.WARNING, "The world specified as the spawn world (" + firstSpawn + ") did not exist!!");
            try {
                return getMVWorld(plugin.getServer().getWorlds().get(0));
            }
            catch (final IndexOutOfBoundsException e) {
                // This should only happen in tests.
                return null;
            }
        }
        return world;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstSpawnWorld(final String world) {
        if ((world == null) && (plugin.getServer().getWorlds().size() > 0)) {
            firstSpawn = plugin.getServer().getWorlds().get(0).getName();
        }
        else {
            firstSpawn = world;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unloadWorld(final String name) {
        return unloadWorld(name, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unloadWorld(final String name, final boolean unloadBukkit) {
        if (worlds.containsKey(name)) {
            worldsFromTheConfig.get(name).cacheVirtualProperties();
            if (unloadBukkit && unloadWorldFromBukkit(name)) {
                worlds.remove(name);
                Logging.info("World '%s' was unloaded from memory.", name);
                return true;
            }
            else if (!unloadBukkit) {
                worlds.remove(name);
                Logging.info("World '%s' was unloaded from memory.", name);
                return true;
            }
            else {
                Logging.warning("World '%s' could not be unloaded. Is it a default world?", name);
            }
        }
        else if (plugin.getServer().getWorld(name) != null) {
            Logging.warning("Hmm Multiverse does not know about this world but it's loaded in memory.");
            Logging.warning("To let Multiverse know about it, use:");
            Logging.warning("/mv import %s %s", name, plugin.getServer().getWorld(name).getEnvironment().toString());
        }
        else if (worldsFromTheConfig.containsKey(name)) {
            return true; // it's already unloaded
        }
        else {
            Logging.info("Multiverse does not know about '%s' and it's not loaded by Bukkit.", name);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadWorld(final String name) {
        // Check if the World is already loaded
        if (worlds.containsKey(name)) {
            return true;
        }

        // Check that the world is in the config
        if (worldsFromTheConfig.containsKey(name)) {
            return doLoad(name);
        }
        else {
            return false;
        }
    }

    private void brokenWorld(final String name) {
        plugin.log(Level.SEVERE, "The world '" + name + "' could NOT be loaded because it contains errors and is probably corrupt!");
        plugin.log(Level.SEVERE, "Try using Minecraft Region Fixer to repair your world! '" + name + "'");
        plugin.log(Level.SEVERE, "https://github.com/Fenixin/Minecraft-Region-Fixer");
    }

    private void nullWorld(final String name) {
        plugin.log(Level.SEVERE, "The world '" + name + "' could NOT be loaded because the server didn't like it!");
        plugin.log(Level.SEVERE, "We don't really know why this is. Contact the developer of your server software!");
        plugin.log(Level.SEVERE, "Server version info: " + Bukkit.getServer().getVersion());
    }

    private boolean doLoad(final String name) {
        return doLoad(name, false, null);
    }

    private boolean doLoad(final String name, final boolean ignoreExists, final WorldType type) {
        if (!worldsFromTheConfig.containsKey(name))
            throw new IllegalArgumentException("That world doesn't exist!");

        final WorldProperties world = worldsFromTheConfig.get(name);
        final WorldCreator creator = WorldCreator.name(name);

        creator.environment(world.getEnvironment()).seed(world.getSeed());
        if (type != null) {
            creator.type(type);
        }

        boolean generatorSuccess = true;
        if ((world.getGenerator() != null) && (!world.getGenerator().equals("null")))
            generatorSuccess = null != plugin.getUnsafeCallWrapper().wrap(() -> {
                creator.generator(world.getGenerator());
                return new Object();
            }, "the generator plugin", "Failed to set the generator for world '%s' to '%s': %s", name, world.getGenerator());

        return generatorSuccess && doLoad(creator, ignoreExists);
    }

    private boolean doLoad(final WorldCreator creator, final boolean ignoreExists) {
        final String worldName = creator.name();
        if (!worldsFromTheConfig.containsKey(worldName))
            throw new IllegalArgumentException("That world doesn't exist!");
        if (worlds.containsKey(worldName))
            throw new IllegalArgumentException("That world is already loaded!");

        if (!ignoreExists && !new File(plugin.getServer().getWorldContainer(), worldName).exists() && !new File(plugin.getServer().getWorldContainer().getParent(), worldName).exists()) {
            plugin.log(Level.WARNING, "WorldManager: Can't load this world because the folder was deleted/moved: " + worldName);
            plugin.log(Level.WARNING, "Use '/mv remove' to remove it from the config!");
            return false;
        }

        final WorldProperties mvworld = worldsFromTheConfig.get(worldName);
        final World cbworld;
        try {
            cbworld = creator.createWorld();
        }
        catch (final Exception e) {
            e.printStackTrace();
            brokenWorld(worldName);
            return false;
        }
        if (cbworld == null) {
            nullWorld(worldName);
            return false;
        }
        final MVWorld world = new MVWorld(plugin, cbworld, mvworld);
        if (MultiverseCoreConfiguration.getInstance().isAutoPurgeEnabled()) {
            worldPurger.purgeWorld(world);
        }
        worlds.put(worldName, world);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteWorld(final String name, final boolean removeFromConfig, final boolean deleteWorldFolder) {
        final World world = plugin.getServer().getWorld(name);
        if (world == null) {
            // We can only delete loaded worlds
            return false;
        }

        // call the event!
        final MVWorldDeleteEvent mvwde = new MVWorldDeleteEvent(getMVWorld(name), removeFromConfig);
        plugin.getServer().getPluginManager().callEvent(mvwde);
        if (mvwde.isCancelled()) {
            plugin.log(Level.FINE, "Tried to delete a world, but the event was cancelled!");
            return false;
        }

        if (removeFromConfig) {
            if (!removeWorldFromConfig(name)) {
                return false;
            }
        } else {
            if (!unloadWorld(name)) {
                return false;
            }
        }

        try {
            final File worldFile = world.getWorldFolder();
            plugin.log(Level.FINER, "deleteWorld(): worldFile: " + worldFile.getAbsolutePath());
            if (deleteWorldFolder ? FileUtils.deleteFolder(worldFile) : FileUtils.deleteFolderContents(worldFile)) {
                Logging.info("World '%s' was DELETED.", name);
                return true;
            }
            else {
                Logging.severe("World '%s' was NOT deleted.", name);
                Logging.severe("Are you sure the folder %s exists?", name);
                Logging.severe("Please check your file permissions on '%s'", name);
                return false;
            }
        }
        catch (final Throwable e) {
            Logging.severe("Hrm, something didn't go as planned. Here's an exception for ya.");
            Logging.severe("You can go politely explain your situation in #multiverse on esper.net");
            Logging.severe("But from here, it looks like your folder is oddly named.");
            Logging.severe("This world has been removed from Multiverse-Core so your best bet is to go delete the folder by hand. Sorry.");
            Logging.severe(e.getMessage());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteWorld(final String name, final boolean removeFromConfig) {
        return deleteWorld(name, removeFromConfig, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteWorld(final String name) {
        return deleteWorld(name, true);
    }

    /**
     * Unload a world from Bukkit.
     *
     * @param name Name of the world to unload
     *
     * @return True if the world was unloaded, false if not.
     */
    private boolean unloadWorldFromBukkit(final String name) {
        removePlayersFromWorld(name);
        return plugin.getServer().unloadWorld(name, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePlayersFromWorld(final String name) {
        final World w = plugin.getServer().getWorld(name);
        if (w != null) {
            final World safeWorld = plugin.getServer().getWorlds().get(0);
            final List<Player> ps = w.getPlayers();
            final SafeTTeleporter teleporter = plugin.getSafeTTeleporter();
            for (final Player p : ps) {
                // We're removing players forcefully from a world, they'd BETTER spawn safely.
                teleporter.safelyTeleport(null, p, safeWorld.getSpawnLocation(), true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MultiverseWorld> getMVWorlds() {
        return worlds.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseWorld getMVWorld(final String name) {
        if (name == null) {
            return null;
        }
        final MultiverseWorld world = worlds.get(name);
        if (world != null) {
            return world;
        }
        return getMVWorldByAlias(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseWorld getMVWorld(final World world) {
        if (world != null) {
            return getMVWorld(world.getName());
        }
        return null;
    }

    /**
     * Returns a {@link MVWorld} if it exists, and null if it does not. This will search ONLY alias.
     *
     * @param alias The alias of the world to get.
     *
     * @return A {@link MVWorld} or null.
     */
    private MultiverseWorld getMVWorldByAlias(final String alias) {
        for (final MultiverseWorld w : worlds.values()) {
            if (w.getAlias().equalsIgnoreCase(alias)) {
                return w;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMVWorld(final String name) {
        return (worlds.containsKey(name) || isMVWorldAlias(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMVWorld(final World world) {
        return world != null && isMVWorld(world.getName());
    }

    /**
     * This method ONLY checks the alias of each world.
     *
     * @param alias The alias of the world to check.
     * @return True if the world exists, false if not.
     */
    private boolean isMVWorldAlias(final String alias) {
        for (final MultiverseWorld w : worlds.values()) {
            if (w.getAlias().equalsIgnoreCase(alias)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDefaultWorlds() {
        ensureConfigIsPrepared();
        final List<World> myWorlds = plugin.getServer().getWorlds();
        for (final World w : myWorlds) {
            final String name = w.getName();
            if (!worldsFromTheConfig.containsKey(name)) {
                String generator = null;
                if (defaultGens.containsKey(name)) {
                    generator = defaultGens.get(name);
                }
                addWorld(name, w.getEnvironment(), String.valueOf(w.getSeed()), w.getWorldType(), w.canGenerateStructures(), generator);
            }
        }
    }

    private void ensureConfigIsPrepared() {
        configWorlds.options().pathSeparator(SEPARATOR);
        if (configWorlds.getConfigurationSection("worlds") == null) {
            configWorlds.createSection("worlds");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadWorlds(final boolean forceLoad) {
        // Basic Counter to count how many Worlds we are loading.
        int count = 0;
        ensureConfigIsPrepared();
        ensureSecondNamespaceIsPrepared();

        // Force the worlds to be loaded, ie don't just load new worlds.
        if (forceLoad) {
            // Remove all world permissions.
            final Permission allAccess = plugin.getServer().getPluginManager().getPermission("multiverse.access.*");
            final Permission allExempt = plugin.getServer().getPluginManager().getPermission("multiverse.exempt.*");
            for (final MultiverseWorld w : worlds.values()) {
                // Remove this world from the master list
                if (allAccess != null) {
                    allAccess.getChildren().remove(w.getAccessPermission().getName());
                }
                if (allExempt != null) {
                    allExempt.getChildren().remove(w.getAccessPermission().getName());
                }
                plugin.getServer().getPluginManager().removePermission(w.getAccessPermission().getName());
                plugin.getServer().getPluginManager().removePermission(w.getExemptPermission().getName());
                // Special namespace for gamemodes
                plugin.getServer().getPluginManager().removePermission("mv.bypass.gamemode." + w.getName());
            }
            // Recalc the all permission
            assert allAccess != null;
            plugin.getServer().getPluginManager().recalculatePermissionDefaults(allAccess);
            assert allExempt != null;
            plugin.getServer().getPluginManager().recalculatePermissionDefaults(allExempt);
            worlds.clear();
        }

        for (final Map.Entry<String, WorldProperties> entry : worldsFromTheConfig.entrySet()) {
            if (worlds.containsKey(entry.getKey())) {
                continue;
            }
            if (!entry.getValue().getAutoLoad())
                continue;

            if (doLoad(entry.getKey()))
                count++;
        }

        // Simple Output to the Console to show how many Worlds were loaded.
        Logging.config("%s - World(s) loaded.", count);
        saveWorldsConfig();
    }

    private void ensureSecondNamespaceIsPrepared() {
        Permission special = plugin.getServer().getPluginManager().getPermission("mv.bypass.gamemode.*");
        if (special == null) {
            special = new Permission("mv.bypass.gamemode.*", PermissionDefault.FALSE);
            plugin.getServer().getPluginManager().addPermission(special);
        }
    }

    /**
     * {@inheritDoc}
     * @deprecated This is deprecated!
     */
    @Override
    @Deprecated
    public PurgeWorlds getWorldPurger() {
        return new PurgeWorlds(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldPurger getTheWorldPurger() {
        return worldPurger;
    }

    private static final char SEPARATOR = '\uF8FF';

    public boolean isKeepingSpawnInMemory(final World world) {
        final WorldProperties properties = worldsFromTheConfig.get(world.getName());
        return properties == null || properties.isKeepingSpawnInMemory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileConfiguration loadWorldConfig(final File file) {
        configWorlds = YamlConfiguration.loadConfiguration(file);
        ensureConfigIsPrepared();
        try {
            configWorlds.save(new File(plugin.getDataFolder(), "worlds.yml"));
        }
        catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // load world-objects
        final Stack<String> worldKeys = new Stack<>();
        worldKeys.addAll(configWorlds.getConfigurationSection("worlds").getKeys(false));
        final Map<String, WorldProperties> newWorldsFromTheConfig = new HashMap<>();
        while (!worldKeys.isEmpty()) {
            final String key = worldKeys.pop();
            final String path = "worlds" + SEPARATOR + key;
            final Object obj = configWorlds.get(path);
            if ((obj instanceof WorldProperties)) {
                final String worldName = key.replaceAll(String.valueOf(SEPARATOR), ".");
                final WorldProperties props = (WorldProperties) obj;
                if (worldsFromTheConfig.containsKey(worldName)) {
                    // Object-Recycling :D
                    // TODO Why is is checking worldsFromTheConfig and then getting from worlds?  So confused... (DTM)
                    final MVWorld mvWorld = (MVWorld) worlds.get(worldName);
                    if (mvWorld != null) {
                        mvWorld.copyValues((WorldProperties) obj);
                    }
                }
                newWorldsFromTheConfig.put(worldName, props);
            }
            else if (configWorlds.isConfigurationSection(path)) {
                final ConfigurationSection section = configWorlds.getConfigurationSection(path);
                assert section != null;
                final Set<String> subkeys = section.getKeys(false);
                for (final String subkey : subkeys) {
                    worldKeys.push(key + SEPARATOR + subkey);
                }
            }
        }
        worldsFromTheConfig = newWorldsFromTheConfig;
        worlds.keySet().retainAll(worldsFromTheConfig.keySet());
        return configWorlds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveWorldsConfig() {
        try {
            configWorlds.options().pathSeparator(SEPARATOR);
            configWorlds.set("worlds", null);
            for (final Map.Entry<String, WorldProperties> entry : worldsFromTheConfig.entrySet()) {
                configWorlds.set("worlds" + SEPARATOR + entry.getKey(), entry.getValue());
            }
            configWorlds.save(new File(plugin.getDataFolder(), "worlds.yml"));
            return true;
        }
        catch (final IOException e) {
            plugin.log(Level.SEVERE, "Could not save worlds.yml. Please check your settings.");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseWorld getSpawnWorld() {
        return getMVWorld(plugin.getServer().getWorlds().get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUnloadedWorlds() {
        final List<String> allNames = new ArrayList<>(worldsFromTheConfig.keySet());
        allNames.removeAll(worlds.keySet());
        return allNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean regenWorld(final String name, final boolean useNewSeed, final boolean randomSeed, final String seed) {
        final MultiverseWorld world = getMVWorld(name);
        if (world == null)
            return false;

        final List<Player> ps = world.getCBWorld().getPlayers();

        if (useNewSeed) {
            long theSeed;

            if (randomSeed) {
                theSeed = new SecureRandom().nextLong();
            } else {
                try {
                    theSeed = Long.parseLong(seed);
                }
                catch (final NumberFormatException e) {
                    theSeed = seed.hashCode();
                }
            }

            world.setSeed(theSeed);
        }
        final WorldType type = world.getWorldType();

        if (deleteWorld(name, false, false)) {
            doLoad(name, true, type);
            final SafeTTeleporter teleporter = plugin.getSafeTTeleporter();
            final Location newSpawn = world.getSpawnLocation();
            // Send all players that were in the old world, BACK to it!
            for (final Player p : ps) {
                teleporter.safelyTeleport(null, p, newSpawn, true);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the {@link FileConfiguration} that this {@link WorldManager} is using.
     * @return The {@link FileConfiguration} that this {@link WorldManager} is using.
     */
    public FileConfiguration getConfigWorlds() {
        return configWorlds;
    }

    @Override
    public boolean hasUnloadedWorld(final String name, final boolean includeLoaded) {
        if (getMVWorld(name) != null) {
            return includeLoaded;
        }
        for (final Map.Entry<String, WorldProperties> entry : worldsFromTheConfig.entrySet()) {
            if (name.equals(entry.getKey()) || name.equals(entry.getValue().getAlias())) {
                return true;
            }
        }
		return false;
	}
}
