/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import buscript.Buscript;
import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MVWorld.NullLocation;
import com.onarandombox.MultiverseCore.api.BlockSafety;
import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.LocationManipulation;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseCoreConfig;
import com.onarandombox.MultiverseCore.api.MultiverseMessaging;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.commands.AnchorCommand;
import com.onarandombox.MultiverseCore.commands.CheckCommand;
import com.onarandombox.MultiverseCore.commands.CloneCommand;
import com.onarandombox.MultiverseCore.commands.ConfigCommand;
import com.onarandombox.MultiverseCore.commands.ConfirmCommand;
import com.onarandombox.MultiverseCore.commands.CoordCommand;
import com.onarandombox.MultiverseCore.commands.CreateCommand;
import com.onarandombox.MultiverseCore.commands.DebugCommand;
import com.onarandombox.MultiverseCore.commands.DeleteCommand;
import com.onarandombox.MultiverseCore.commands.EnvironmentCommand;
import com.onarandombox.MultiverseCore.commands.GameruleCommand;
import com.onarandombox.MultiverseCore.commands.GamerulesCommand;
import com.onarandombox.MultiverseCore.commands.GeneratorCommand;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseCore.commands.ImportCommand;
import com.onarandombox.MultiverseCore.commands.InfoCommand;
import com.onarandombox.MultiverseCore.commands.ListCommand;
import com.onarandombox.MultiverseCore.commands.LoadCommand;
import com.onarandombox.MultiverseCore.commands.ModifyAddCommand;
import com.onarandombox.MultiverseCore.commands.ModifyClearCommand;
import com.onarandombox.MultiverseCore.commands.ModifyCommand;
import com.onarandombox.MultiverseCore.commands.ModifyRemoveCommand;
import com.onarandombox.MultiverseCore.commands.ModifySetCommand;
import com.onarandombox.MultiverseCore.commands.PurgeCommand;
import com.onarandombox.MultiverseCore.commands.RegenCommand;
import com.onarandombox.MultiverseCore.commands.ReloadCommand;
import com.onarandombox.MultiverseCore.commands.RemoveCommand;
import com.onarandombox.MultiverseCore.commands.ScriptCommand;
import com.onarandombox.MultiverseCore.commands.SetSpawnCommand;
import com.onarandombox.MultiverseCore.commands.SilentCommand;
import com.onarandombox.MultiverseCore.commands.SpawnCommand;
import com.onarandombox.MultiverseCore.commands.TeleportCommand;
import com.onarandombox.MultiverseCore.commands.UnloadCommand;
import com.onarandombox.MultiverseCore.commands.VersionCommand;
import com.onarandombox.MultiverseCore.commands.WhoCommand;
import com.onarandombox.MultiverseCore.destination.AnchorDestination;
import com.onarandombox.MultiverseCore.destination.BedDestination;
import com.onarandombox.MultiverseCore.destination.CannonDestination;
import com.onarandombox.MultiverseCore.destination.DestinationFactory;
import com.onarandombox.MultiverseCore.destination.ExactDestination;
import com.onarandombox.MultiverseCore.destination.PlayerDestination;
import com.onarandombox.MultiverseCore.destination.WorldDestination;
import com.onarandombox.MultiverseCore.event.MVDebugModeEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseCore.listeners.MVEntityListener;
import com.onarandombox.MultiverseCore.listeners.MVMapListener;
import com.onarandombox.MultiverseCore.listeners.MVPlayerListener;
import com.onarandombox.MultiverseCore.listeners.MVPortalListener;
import com.onarandombox.MultiverseCore.listeners.MVWeatherListener;
import com.onarandombox.MultiverseCore.listeners.MVWorldInitListener;
import com.onarandombox.MultiverseCore.listeners.MVWorldListener;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import com.onarandombox.MultiverseCore.utils.MVEconomist;
import com.onarandombox.MultiverseCore.utils.MVMessaging;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import com.onarandombox.MultiverseCore.utils.MVPlayerSession;
import com.onarandombox.MultiverseCore.utils.MaterialConverter;
import com.onarandombox.MultiverseCore.utils.SimpleBlockSafety;
import com.onarandombox.MultiverseCore.utils.SimpleLocationManipulation;
import com.onarandombox.MultiverseCore.utils.SimpleSafeTTeleporter;
import com.onarandombox.MultiverseCore.utils.UnsafeCallWrapper;
import com.onarandombox.MultiverseCore.utils.VaultHandler;
import com.onarandombox.MultiverseCore.utils.WorldManager;
import com.pneumaticraft.commandhandler.CommandHandler;
import me.main__.util.SerializationConfig.NoSuchPropertyException;
import me.main__.util.SerializationConfig.SerializationConfig;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of the Multiverse-{@link Core}.
 */
public class MultiverseCore extends JavaPlugin implements MVPlugin, Core {
    private static final int PROTOCOL = 24;
    // TODO: Investigate if this one is really needed to be static.
    // Doubt it. -- FernFerret
    private static final Map<String, String> teleportQueue = new HashMap<>();

    private final AnchorManager anchorManager = new AnchorManager(this);
    // TODO please let's make this non-static
    private volatile MultiverseCoreConfiguration config;

    public MultiverseCore() {
        super();
    }

    // Configurations
    private FileConfiguration multiverseConfig;

    /**
     * This is for unit testing.
     *
     * @param loader      The PluginLoader to use.
     * @param description The Description file to use.
     * @param dataFolder  The folder that other datafiles can be found in.
     * @param file        The location of the plugin.
     */
    public MultiverseCore(final JavaPluginLoader loader, final PluginDescriptionFile description, final File dataFolder, final File file) {
        super(loader, description, dataFolder, file);
    }

    /**
     * This method is used to find out who is teleporting a player.
     *
     * @param playerName The teleported player (the teleportee).
     *
     * @return The player that teleported the other one (the teleporter).
     */
    public static String getPlayerTeleporter(final String playerName) {
        if (teleportQueue.containsKey(playerName)) {
            final String teleportee = teleportQueue.get(playerName);
            teleportQueue.remove(playerName);
            return teleportee;
        }
        return null;
    }

    /**
     * This method is used to add a teleportation to the teleportQueue.
     *
     * @param teleporter The name of the player that initiated the teleportation.
     * @param teleportee The name of the player that was teleported.
     */
    public static void addPlayerToTeleportQueue(final String teleporter, final String teleportee) {
        Logging.finest("Adding mapping '%s' => '%s' to teleport queue", teleporter, teleportee);
        teleportQueue.put(teleportee, teleporter);
    }

    @Override
    public MultiverseCore getCore() {
        return this;
    }

    /**
     * Logs a message at the specified level.
     *
     * @param level The Log-{@link Level}.
     * @param msg   The message to log.
     *
     * @deprecated Replaced by {@link Logging}.  Please refrain from using this from a third party plugin as the
     * messages will appear to originate from Multiverse-Core.
     */
    @Deprecated
    public static void staticLog(final Level level, final String msg) {
        Logging.log(level, msg);
    }

    @Override
    public int getProtocolVersion() {
        return MultiverseCore.PROTOCOL;
    }

    // Setup our Map for our Commands using the CommandHandler.
    private CommandHandler commandHandler;

    private static final String LOG_TAG = "[Multiverse-Core]";

    // Multiverse Permissions Handler
    private MVPermissions ph;

    /**
     * Print messages to the Debug Log, if the servers in Debug Mode then we also want to print the messages to the
     * standard Server Console.
     *
     * @param level The Log-{@link Level}
     * @param msg   The message
     *
     * @deprecated Replaced by {@link Logging}.  Please refrain from using this from a third party plugin as the
     * messages will appear to originate from Multiverse-Core.
     */
    @Deprecated
    public static void staticDebugLog(final Level level, final String msg) {
        Logging.log(level, msg);
    }

    private final MVWorldManager worldManager = new WorldManager(this);

    // Setup the block/player/entity listener.
    private final MVPlayerListener playerListener = new MVPlayerListener(this);
    private final MVEntityListener entityListener = new MVEntityListener(this);
    private final MVWeatherListener weatherListener = new MVWeatherListener(this);
    private final MVPortalListener portalListener = new MVPortalListener(this);
    private final MVWorldListener worldListener = new MVWorldListener(this);

    // HashMap to contain information relating to the Players.
    private HashMap<String, MVPlayerSession> playerSessions;
    private MVEconomist economist;
    private Buscript buscript;
    private int pluginCount;
    private DestinationFactory destFactory;
    private MultiverseMessaging messaging;
    private BlockSafety blockSafety;
    private LocationManipulation locationManipulation;
    private SafeTTeleporter safeTTeleporter;
    private UnsafeCallWrapper unsafeCallWrapper;

    private File serverFolder = new File(System.getProperty("user.dir"));

    /**
     * {@inheritDoc}
     *
     * @deprecated This is now deprecated, nobody needs it any longer.
     * All version info-dumping is now done with {@link MVVersionEvent}.
     */
    @Override
    @Deprecated
    public String dumpVersionInfo(final String buffer) {
        return buffer;
    }

    @Override
    public void setCore(final MultiverseCore core) {
        // This method is required by the interface (so core is effectively a plugin of itself) and therefore
        // this is never used.
    }

    public MVEconomist getEconomist() {
        return economist;
    }

    @Override
    public void onLoad() {
        // Register our config
        SerializationConfig.registerAll(MultiverseCoreConfiguration.class);
        // Register our world
        SerializationConfig.registerAll(WorldProperties.class);
        // Create our DataFolder
        getDataFolder().mkdirs();
        // Setup our Debug Log
        Logging.init(this);
        SerializationConfig.initLogging(Logging.getLogger());
        // Setup our BlockSafety
        blockSafety = new SimpleBlockSafety(this);
        // Setup our LocationManipulation
        locationManipulation = new SimpleLocationManipulation();
        // Setup our SafeTTeleporter
        safeTTeleporter   = new SimpleSafeTTeleporter(this);
        unsafeCallWrapper = new UnsafeCallWrapper(this);
    }

    /**
     * Initializes the buscript javascript library.
     */
    private void initializeBuscript() {
        buscript = new Buscript(this);
        // Add global variable "multiverse" to javascript environment
        buscript.setScriptVariable("multiverse", this);
    }

    @Override
    @Deprecated
    public VaultHandler getVaultHandler() {
        return economist.getVaultHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MVWorldInitListener(this), this);

        messaging = new MVMessaging();
        economist = new MVEconomist(this);
        // Load the defaultWorldGenerators
        worldManager.getDefaultWorldGenerators();

        registerEvents();
        // Setup Permissions, we'll do an initial check for the Permissions plugin then fall back on isOP().
        ph = new MVPermissions(this);

        // Setup the command manager
        commandHandler = new CommandHandler(this, ph);
        // Call the Function to assign all the Commands to their Class.
        registerCommands();

        // Initialize the Destination factor AFTER the commands
        initializeDestinationFactory();

        playerSessions = new HashMap<>();

        // Call the Function to load all the Worlds and setup the HashMap
        // When called with null, it tries to load ALL
        // this function will be called every time a plugin registers a new envtype with MV
        // Setup & Load our Configuration files.
        loadConfigs();
        if (multiverseConfig != null) {
            Logging.setShowingConfig(!getMVConfig().getSilentStart());
            worldManager.loadDefaultWorlds();
            worldManager.loadWorlds(true);
        }
        else {
            log(Level.SEVERE, "Your configs were not loaded. Very little will function in Multiverse.");
        }
        anchorManager.loadAnchors();

        // Now set the firstspawnworld (after the worlds are loaded):
        worldManager.setFirstSpawnWorld(getMVConfig().getFirstSpawnWorld());
        try {
            getMVConfig().setFirstSpawnWorld(worldManager.getFirstSpawnWorld().getName());
        }
        catch (final NullPointerException e) {
            // A test that had no worlds loaded was being run. This should never happen in production
        }
        saveMVConfig();
        initializeBuscript();

        // Output a little snippet to show it's enabled.
        Logging.config("Version %s (API v%s) Enabled - By %s", getDescription().getVersion(), PROTOCOL, getAuthors());

        if (getMVConfig().isShowingDonateMessage()) {
            getLogger().config("Help dumptruckman keep this project alive. Become a patron! https://www.patreon.com/dumptruckman");
            getLogger().config("One time donations are also appreciated: https://www.paypal.me/dumptruckman");
        }
    }

    private void initializeDestinationFactory() {
        destFactory = new DestinationFactory(this);
        destFactory.registerDestinationType(WorldDestination.class, "");
        destFactory.registerDestinationType(WorldDestination.class, "w");
        destFactory.registerDestinationType(ExactDestination.class, "e");
        destFactory.registerDestinationType(PlayerDestination.class, "pl");
        destFactory.registerDestinationType(CannonDestination.class, "ca");
        destFactory.registerDestinationType(BedDestination.class, "b");
        destFactory.registerDestinationType(AnchorDestination.class, "a");
    }

    /**
     * Function to Register all the Events needed.
     */
    private void registerEvents() {
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(entityListener, this);
        pm.registerEvents(weatherListener, this);
        pm.registerEvents(portalListener, this);
        log(Level.INFO, "We are aware of the warning about the deprecated event. There is no alternative that allows us to do what we need to do. The performance impact is negligible.");
        pm.registerEvents(worldListener, this);
        pm.registerEvents(new MVMapListener(this), this);
    }

    private static final char PATH_SEPARATOR = '\uF8FF';

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadConfigs() {
        // Now grab the Configuration Files.
        multiverseConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        final InputStream resourceURL = getClass().getResourceAsStream("/defaults/config.yml");

        // Read in our default config with UTF-8 now
        final Configuration coreDefaults;
        coreDefaults = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(resourceURL, StandardCharsets.UTF_8)));
        multiverseConfig.setDefaults(coreDefaults);

        multiverseConfig.options().copyDefaults(false);
        multiverseConfig.options().copyHeader(true);

        MultiverseCoreConfiguration wantedConfig = null;
        try {
            wantedConfig = (MultiverseCoreConfiguration) multiverseConfig.get("multiverse-configuration");
        }
        catch (final Exception ignore) {
        }
        finally {
            config = ((wantedConfig == null) ? new MultiverseCoreConfiguration() : wantedConfig);
        }
        migrateWorldConfig();
        worldManager.loadWorldConfig(new File(getDataFolder(), "worlds.yml"));

        messaging.setCooldown(getMVConfig().getMessageCooldown());

        // Remove old values.
        multiverseConfig.set("enforcegamemodes", null);
        multiverseConfig.set("bedrespawn", null);
        multiverseConfig.set("opfallback", null);

        // Old Config Format
        migrate22Values();
        saveMVConfigs();

        final int level = Logging.getDebugLevel();
        Logging.setDebugLevel(getMVConfig().getGlobalDebug());
        if (level != Logging.getDebugLevel()) {
            getServer().getPluginManager().callEvent(new MVDebugModeEvent(level));
        }
    }

    /**
     * Thes are the MV config 2.0-2.2 values,
     * they should be migrated to the new format.
     */
    private void migrate22Values() {
        if (multiverseConfig.isSet("worldnameprefix")) {
            Logging.config("Migrating 'worldnameprefix'...");
            getMVConfig().setPrefixChat(multiverseConfig.getBoolean("worldnameprefix"));
            multiverseConfig.set("worldnameprefix", null);
        }
        if (multiverseConfig.isSet("firstspawnworld")) {
            Logging.config("Migrating 'firstspawnworld'...");
            getMVConfig().setFirstSpawnWorld(multiverseConfig.getString("firstspawnworld"));
            multiverseConfig.set("firstspawnworld", null);
        }
        if (multiverseConfig.isSet("enforceaccess")) {
            Logging.config("Migrating 'enforceaccess'...");
            getMVConfig().setEnforceAccess(multiverseConfig.getBoolean("enforceaccess"));
            multiverseConfig.set("enforceaccess", null);
        }
        if (multiverseConfig.isSet("displaypermerrors")) {
            Logging.config("Migrating 'displaypermerrors'...");
            getMVConfig().setDisplayPermErrors(multiverseConfig.getBoolean("displaypermerrors"));
            multiverseConfig.set("displaypermerrors", null);
        }
        if (multiverseConfig.isSet("teleportintercept")) {
            Logging.config("Migrating 'teleportintercept'...");
            getMVConfig().setTeleportIntercept(multiverseConfig.getBoolean("teleportintercept"));
            multiverseConfig.set("teleportintercept", null);
        }
        if (multiverseConfig.isSet("firstspawnoverride")) {
            Logging.config("Migrating 'firstspawnoverride'...");
            getMVConfig().setFirstSpawnOverride(multiverseConfig.getBoolean("firstspawnoverride"));
            multiverseConfig.set("firstspawnoverride", null);
        }
        if (multiverseConfig.isSet("messagecooldown")) {
            Logging.config("Migrating 'messagecooldown'...");
            getMVConfig().setMessageCooldown(multiverseConfig.getInt("messagecooldown"));
            multiverseConfig.set("messagecooldown", null);
        }
        if (multiverseConfig.isSet("debug")) {
            Logging.config("Migrating 'debug'...");
            getMVConfig().setGlobalDebug(multiverseConfig.getInt("debug"));
            multiverseConfig.set("debug", null);
        }
        if (multiverseConfig.isSet("version")) {
            Logging.config("Migrating 'version'...");
            multiverseConfig.set("version", null);
        }
    }

    /**
     * Migrate the worlds.yml to SerializationConfig.
     */
    private void migrateWorldConfig() { // SUPPRESS CHECKSTYLE: MethodLength
        final FileConfiguration wconf = new YamlConfiguration();
        wconf.options().pathSeparator(PATH_SEPARATOR);
        final File worldsFile = new File(getDataFolder(), "worlds.yml");
        try {
            wconf.load(worldsFile);
        }
        catch (final IOException e) {
            log(Level.WARNING, "Cannot load worlds.yml");
        }
        catch (final InvalidConfigurationException e) {
            log(Level.WARNING, "Your worlds.yml is invalid!");
        }

        if (!wconf.isConfigurationSection("worlds")) { // empty config
            log(Level.FINE, "No worlds to migrate!");
            return;
        }

        final Map<String, Object> values = wconf.getConfigurationSection("worlds").getValues(false);

        boolean wasChanged = false;
        final Map<String, Object> newValues = new LinkedHashMap<>(values.size());
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() instanceof WorldProperties) {
                // fine
                newValues.put(entry.getKey(), entry.getValue());
            }
            else if (entry.getValue() instanceof ConfigurationSection) {
                log(Level.FINE, "Migrating: " + entry.getKey());
                // we have to migrate this
                final WorldProperties world = new WorldProperties(Collections.emptyMap());
                final ConfigurationSection section = (ConfigurationSection) entry.getValue();

                // migrate animals and monsters
                if (section.isConfigurationSection("animals")) {
                    final ConfigurationSection animalSection = section.getConfigurationSection("animals");
                    assert animalSection != null;
                    if (animalSection.contains("spawn")) {
                        if (animalSection.isBoolean("spawn"))
                            world.setAllowAnimalSpawn(animalSection.getBoolean("spawn"));
                        else
                            world.setAllowAnimalSpawn(Boolean.parseBoolean(animalSection.getString("spawn")));
                    }
                    if (animalSection.isList("exceptions")) {
                        world.getAnimalList().clear();
                        world.getAnimalList().addAll(animalSection.getStringList("exceptions"));
                    }
                }
                if (section.isConfigurationSection("monsters")) {
                    final ConfigurationSection monsterSection = section.getConfigurationSection("monsters");
                    assert monsterSection != null;
                    if (monsterSection.contains("spawn")) {
                        if (monsterSection.isBoolean("spawn"))
                            world.setAllowMonsterSpawn(monsterSection.getBoolean("spawn"));
                        else
                            world.setAllowMonsterSpawn(Boolean.parseBoolean(monsterSection.getString("spawn")));
                    }
                    if (monsterSection.isList("exceptions")) {
                        world.getMonsterList().clear();
                        world.getMonsterList().addAll(monsterSection.getStringList("exceptions"));
                    }
                }

                // migrate entryfee
                if (section.isConfigurationSection("entryfee")) {
                    final ConfigurationSection feeSection = section.getConfigurationSection("entryfee");
                    assert feeSection != null;
                    if (feeSection.isInt("currency")) {
                        world.setCurrency(MaterialConverter.convertConfigType(feeSection, "currency"));
                    }

                    if (feeSection.isDouble("amount"))
                        world.setPrice(feeSection.getDouble("amount"));
                    else if (feeSection.isInt("amount"))
                        world.setPrice(feeSection.getInt("amount"));
                }

                // migrate pvp
                if (section.isBoolean("pvp")) {
                    world.setPVPMode(section.getBoolean("pvp"));
                }

                // migrate alias
                if (section.isConfigurationSection("alias")) {
                    final ConfigurationSection aliasSection = section.getConfigurationSection("alias");
                    assert aliasSection != null;
                    if (aliasSection.isString("color"))
                        world.setColor(aliasSection.getString("color"));
                    if (aliasSection.isString("name"))
                        world.setAlias(aliasSection.getString("name"));
                }

                // migrate worldblacklist
                if (section.isList("worldblacklist")) {
                    world.getWorldBlacklist().clear();
                    world.getWorldBlacklist().addAll(section.getStringList("worldblacklist"));
                }

                // migrate scale
                if (section.isDouble("scale")) {
                    world.setScaling(section.getDouble("scale"));
                }

                // migrate gamemode
                if (section.isString("gamemode")) {
                    final GameMode gameMode = GameMode.valueOf(section.getString("gamemode").toUpperCase());
                    world.setGameMode(gameMode);
                }

                // migrate hunger
                if (section.isBoolean("hunger")) {
                    world.setHunger(section.getBoolean("hunger"));
                }

                // migrate hidden
                if (section.isBoolean("hidden")) {
                    world.setHidden(section.getBoolean("hidden"));
                }

                // migrate autoheal
                if (section.isBoolean("autoheal")) {
                    world.setAutoHeal(section.getBoolean("autoheal"));
                }

                // migrate portalform
                if (section.isString("portalform")) {
                    try {
                        world.setProperty("portalform", section.getString("portalform"), true);
                    }
                    catch (final NoSuchPropertyException e) {
                        throw new RuntimeException("Who forgot to update the migrator?", e);
                    }
                }

                // migrate environment
                if (section.isString("environment")) {
                    try {
                        world.setProperty("environment", section.getString("environment"), true);
                    }
                    catch (final NoSuchPropertyException e) {
                        throw new RuntimeException("Who forgot to update the migrator?", e);
                    }
                }

                // migrate generator
                if (section.isString("generator")) {
                    world.setGenerator(section.getString("generator"));
                }

                // migrate seed
                if (section.isLong("seed")) {
                    world.setSeed(section.getLong("seed"));
                }

                // migrate weather
                if (section.isBoolean("allowweather")) {
                    world.setEnableWeather(section.getBoolean("allowweather"));
                }

                // migrate adjustspawn
                if (section.isBoolean("adjustspawn")) {
                    world.setAdjustSpawn(section.getBoolean("adjustspawn"));
                }

                // migrate autoload
                if (section.isBoolean("autoload")) {
                    world.setAutoLoad(section.getBoolean("autoload"));
                }

                // migrate bedrespawn
                if (section.isBoolean("bedrespawn")) {
                    world.setBedRespawn(section.getBoolean("bedrespawn"));
                }

                // migrate spawn
                if (section.isConfigurationSection("spawn")) {
                    final ConfigurationSection spawnSect = section.getConfigurationSection("spawn");
                    final Location spawnLoc = new NullLocation();
                    assert spawnSect != null;
                    if (spawnSect.isDouble("yaw"))
                        spawnLoc.setYaw((float) spawnSect.getDouble("yaw"));
                    if (spawnSect.isDouble("pitch"))
                        spawnLoc.setPitch((float) spawnSect.getDouble("pitch"));
                    if (spawnSect.isDouble("x"))
                        spawnLoc.setX(spawnSect.getDouble("x"));
                    if (spawnSect.isDouble("y"))
                        spawnLoc.setY(spawnSect.getDouble("y"));
                    if (spawnSect.isDouble("z"))
                        spawnLoc.setZ(spawnSect.getDouble("z"));

                    world.setSpawnLocation(spawnLoc);
                }

                // migrate difficulty
                if (section.isString("difficulty")) {
                    Difficulty difficulty;
                    try {
                        difficulty = Difficulty.valueOf(section.getString("difficulty").toUpperCase());
                    }
                    catch (final IllegalArgumentException e) {
                        log(Level.WARNING, "Could not parse difficulty: " + section.getString("difficulty"));
                        log(Level.WARNING, "Setting world " + entry.getKey() + " difficulty to NORMAL");
                        difficulty = Difficulty.NORMAL;
                    }
                    world.setDifficulty(difficulty);
                }

                // migrate keepspawninmemory
                if (section.isBoolean("keepspawninmemory")) {
                    world.setKeepSpawnInMemory(section.getBoolean("keepspawninmemory"));
                }

                newValues.put(entry.getKey(), world);
                wasChanged = true;
            } else {
                // huh?
                log(Level.WARNING, "Removing unknown entry in the config: " + entry);
                // just don't add to newValues
                wasChanged = true;
            }
        }

        if (wasChanged) {
            // clear config
            wconf.set("worlds", null);

            // and rebuild it
            final ConfigurationSection rootSection = wconf.createSection("worlds");
            for (final Map.Entry<String, Object> entry : newValues.entrySet()) {
                rootSection.set(entry.getKey(), entry.getValue());
            }

            try {
                wconf.save(new File(getDataFolder(), "worlds.yml"));
            }
            catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseMessaging getMessaging() {
        return messaging;
    }

    /**
     * Register Multiverse-Core commands to Command Manager.
     */
    private void registerCommands() {
        // Intro Commands
        commandHandler.registerCommand(new HelpCommand(this));
        commandHandler.registerCommand(new VersionCommand(this));
        commandHandler.registerCommand(new ListCommand(this));
        commandHandler.registerCommand(new InfoCommand(this));
        commandHandler.registerCommand(new CreateCommand(this));
        commandHandler.registerCommand(new CloneCommand(this));
        commandHandler.registerCommand(new ImportCommand(this));
        commandHandler.registerCommand(new ReloadCommand(this));
        commandHandler.registerCommand(new SetSpawnCommand(this));
        commandHandler.registerCommand(new CoordCommand(this));
        commandHandler.registerCommand(new TeleportCommand(this));
        commandHandler.registerCommand(new WhoCommand(this));
        commandHandler.registerCommand(new SpawnCommand(this));
        // Dangerous Commands
        commandHandler.registerCommand(new UnloadCommand(this));
        commandHandler.registerCommand(new LoadCommand(this));
        commandHandler.registerCommand(new RemoveCommand(this));
        commandHandler.registerCommand(new DeleteCommand(this));
        commandHandler.registerCommand(new RegenCommand(this));
        commandHandler.registerCommand(new ConfirmCommand(this));
        // Modification commands
        commandHandler.registerCommand(new ModifyCommand(this));
        commandHandler.registerCommand(new PurgeCommand(this));
        commandHandler.registerCommand(new ModifyAddCommand(this));
        commandHandler.registerCommand(new ModifySetCommand(this));
        commandHandler.registerCommand(new ModifyRemoveCommand(this));
        commandHandler.registerCommand(new ModifyClearCommand(this));
        commandHandler.registerCommand(new ConfigCommand(this));
        commandHandler.registerCommand(new AnchorCommand(this));
        // Misc Commands
        commandHandler.registerCommand(new EnvironmentCommand(this));
        commandHandler.registerCommand(new DebugCommand(this));
        commandHandler.registerCommand(new SilentCommand(this));
        commandHandler.registerCommand(new GeneratorCommand(this));
        commandHandler.registerCommand(new CheckCommand(this));
        commandHandler.registerCommand(new ScriptCommand(this));
        commandHandler.registerCommand(new GameruleCommand(this));
        commandHandler.registerCommand(new GamerulesCommand(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        saveMVConfigs();
        Logging.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MVPlayerSession getPlayerSession(final Player player) {
        if (playerSessions.containsKey(player.getName())) {
            return playerSessions.get(player.getName());
        }
        else {
            playerSessions.put(player.getName(), new MVPlayerSession(player, getMVConfig()));
            return playerSessions.get(player.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MVPermissions getMVPerms() {
        return ph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String commandLabel, @NotNull final String[] args) {
        if (!isEnabled()) {
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }
        final ArrayList<String> allArgs = new ArrayList<>(Arrays.asList(args));
        allArgs.add(0, command.getName());
        try {
            return commandHandler.locateAndRunCommand(sender, allArgs, getMVConfig().getDisplayPermErrors());
        }
        catch (final Exception e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "An internal error occurred when attempting to perform this command.");
            if (sender.isOp())
                sender.sendMessage(ChatColor.RED + "Details were printed to the server console and logs, please add that to your bug report.");
            else
                sender.sendMessage(ChatColor.RED + "Try again and contact the server owner or an admin if this problem persists.");
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(final Level level, final String msg) {
        Logging.log(level, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthors() {
        final StringBuilder authors = new StringBuilder();
        final List<String> auths = getDescription().getAuthors();
        if (auths.size() == 0) {
            return "";
        }

        if (auths.size() == 1) {
            return auths.get(0);
        }

        for (int i = 0; i < auths.size(); i++) {
            if (i == getDescription().getAuthors().size() - 1) {
                authors.append(" and ").append(getDescription().getAuthors().get(i));
            }
            else {
                authors.append(", ").append(getDescription().getAuthors().get(i));
            }
        }
        return authors.substring(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    /**
     * Gets the log-tag.
     *
     * @return The log-tag
     */
    // TODO this should be static!
    public String getTag() {
        return MultiverseCore.LOG_TAG;
    }

    /**
     * Shows a message that the given world is not a MultiverseWorld.
     *
     * @param sender    The {@link CommandSender} that should receive the message
     * @param worldName The name of the invalid world
     */
    public void showNotMVWorldMessage(final CommandSender sender, final String worldName) {
        sender.sendMessage("Multiverse doesn't know about " + ChatColor.DARK_AQUA + worldName + ChatColor.WHITE + " yet.");
        sender.sendMessage("Type " + ChatColor.DARK_AQUA + "/mv import ?" + ChatColor.WHITE + " for help!");
    }

    /**
     * Removes a player-session.
     *
     * @param player The {@link Player} that owned the session.
     */
    public void removePlayerSession(final Player player) {
        playerSessions.remove(player.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPluginCount() {
        return pluginCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementPluginCount() {
        pluginCount += 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementPluginCount() {
        pluginCount -= 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DestinationFactory getDestFactory() {
        return destFactory;
    }

    /**
     * This is a convenience method to allow the QueuedCommand system to call it. You should NEVER call this directly.
     *
     * @param teleporter The Person requesting that the teleport should happen.
     * @param p          Player The Person being teleported.
     * @param l          The potentially unsafe location.
     */
    public void teleportPlayer(final CommandSender teleporter, final Player p, final Location l) {
        // This command is the override, and MUST NOT TELEPORT SAFELY
        safeTTeleporter.safelyTeleport(teleporter, p, l, false);
    }

    /**
     * Gets the server's root-folder as {@link File}.
     *
     * @return The server's root-folder
     */
    public File getServerFolder() {
        return serverFolder;
    }

    /**
     * Sets this server's root-folder.
     *
     * @param newServerFolder The new server-root
     */
    public void setServerFolder(final File newServerFolder) {
        if (!newServerFolder.isDirectory())
            throw new IllegalArgumentException("That's not a folder!");

        serverFolder = newServerFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MVWorldManager getMVWorldManager() {
        return worldManager;
    }

    /**
     * Gets the {@link MVPlayerListener}.
     *
     * @return The {@link MVPlayerListener}.
     */
    public MVPlayerListener getPlayerListener() {
        return playerListener;
    }

    /**
     * Gets the {@link MVEntityListener}.
     *
     * @return The {@link MVEntityListener}.
     */
    public MVEntityListener getEntityListener() {
        return entityListener;
    }

    /**
     * Gets the {@link MVWeatherListener}.
     *
     * @return The {@link MVWeatherListener}.
     */
    public MVWeatherListener getWeatherListener() {
        return weatherListener;
    }

    /**
     * Saves the Multiverse-Config.
     *
     * @return Whether the Multiverse-Config was successfully saved
     */
    public boolean saveMVConfig() {
        try {
            multiverseConfig.set("multiverse-configuration", getMVConfig());
            multiverseConfig.save(new File(getDataFolder(), "config.yml"));
            return true;
        }
        catch (final IOException e) {
            log(Level.SEVERE, "Could not save Multiverse config.yml config. Please check your file permissions.");
            return false;
        }
    }

    /**
     * Saves the world config.
     *
     * @return Whether the world-config was successfully saved
     */
    public boolean saveWorldConfig() {
        return worldManager.saveWorldsConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveMVConfigs() {
        return saveMVConfig() && saveWorldConfig();
    }

    /**
     * NOT deprecated for the time as queued commands use this.
     * However, this is not in the API and other plugins should therefore not use it.
     *
     * @param name World to delete
     *
     * @return True if success, false if fail.
     */
    public Boolean deleteWorld(final String name) {
        return worldManager.deleteWorld(name);
    }

    /**
     * NOT deprecated for the time as queued commands use this.
     * However, this is not in the API and other plugins should therefore not use it.
     *
     * @param oldName   World to copy
     * @param newName   World to create
     * @param generator The Custom generator plugin to use.
     *
     * @return True if success, false if fail.
     */
    public Boolean cloneWorld(final String oldName, final String newName, final String generator) {
        return worldManager.cloneWorld(oldName, newName, generator);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated This is deprecated!
     */
    @Override
    @Deprecated
    public Boolean regenWorld(final String name, final Boolean useNewSeed, final Boolean randomSeed, final String seed) {
        return worldManager.regenWorld(name, useNewSeed, randomSeed, seed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnchorManager getAnchorManager() {
        return anchorManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockSafety getBlockSafety() {
        return blockSafety;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlockSafety(final BlockSafety bs) {
        if (bs == null) {
            throw new NullPointerException("block safety may not be null.");
        }
        blockSafety = bs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocationManipulation getLocationManipulation() {
        return locationManipulation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocationManipulation(final LocationManipulation locationManipulation) {
        this.locationManipulation = locationManipulation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SafeTTeleporter getSafeTTeleporter() {
        return safeTTeleporter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSafeTTeleporter(final SafeTTeleporter safeTTeleporter) {
        this.safeTTeleporter = safeTTeleporter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseCoreConfig getMVConfig() {
        return config;
    }

    /**
     * This method is currently used by other plugins.
     * It will be removed in 2.4
     * @return The Multiverse config.
     * @deprecated This is deprecated.
     */
    @Deprecated
    public static MultiverseCoreConfiguration getStaticConfig() {
        return MultiverseCoreConfiguration.getInstance();
    }

    @Override
    public Buscript getScriptAPI() {
        return buscript;
    }

    public UnsafeCallWrapper getUnsafeCallWrapper() {
        return unsafeCallWrapper;
    }
}
