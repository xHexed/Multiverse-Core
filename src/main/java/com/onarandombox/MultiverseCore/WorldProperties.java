package com.onarandombox.MultiverseCore;

import com.onarandombox.MultiverseCore.MVWorld.NullLocation;
import com.onarandombox.MultiverseCore.configuration.EntryFee;
import com.onarandombox.MultiverseCore.configuration.SpawnLocation;
import com.onarandombox.MultiverseCore.configuration.SpawnSettings;
import com.onarandombox.MultiverseCore.configuration.WorldPropertyValidator;
import com.onarandombox.MultiverseCore.enums.AllowedPortalType;
import com.onarandombox.MultiverseCore.enums.EnglishChatColor;
import com.onarandombox.MultiverseCore.enums.EnglishChatStyle;
import me.main__.util.SerializationConfig.IllegalPropertyValueException;
import me.main__.util.SerializationConfig.Property;
import me.main__.util.SerializationConfig.SerializationConfig;
import me.main__.util.SerializationConfig.Serializor;
import me.main__.util.SerializationConfig.Validator;
import me.main__.util.SerializationConfig.VirtualProperty;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This is a property class, I think we don't need that much javadoc.
 * BEGIN CHECKSTYLE-SUPPRESSION: Javadoc
 */

@SerializableAs("MVWorld")
public class WorldProperties extends SerializationConfig {

    private static final Map<String, String> PROPERTY_ALIASES;

    static {
        PROPERTY_ALIASES = new HashMap<>();
        PROPERTY_ALIASES.put("curr", "entryfee.currency");
        PROPERTY_ALIASES.put("currency", "entryfee.currency");
        PROPERTY_ALIASES.put("price", "entryfee.amount");
        PROPERTY_ALIASES.put("scaling", "scale");
        PROPERTY_ALIASES.put("aliascolor", "color");
        PROPERTY_ALIASES.put("heal", "autoHeal");
        PROPERTY_ALIASES.put("storm", "allowWeather");
        PROPERTY_ALIASES.put("weather", "allowWeather");
        PROPERTY_ALIASES.put("spawnmemory", "keepSpawnInMemory");
        PROPERTY_ALIASES.put("memory", "keepSpawnInMemory");
        PROPERTY_ALIASES.put("mode", "gameMode");
        PROPERTY_ALIASES.put("diff", "difficulty");
        PROPERTY_ALIASES.put("spawnlocation", "spawn");
        PROPERTY_ALIASES.put("limit", "playerLimit");
        PROPERTY_ALIASES.put("animals", "spawning.animals.spawn");
        PROPERTY_ALIASES.put("monsters", "spawning.monsters.spawn");
        PROPERTY_ALIASES.put("animalsrate", "spawning.animals.spawnrate");
        PROPERTY_ALIASES.put("monstersrate", "spawning.monsters.spawnrate");
        PROPERTY_ALIASES.put("flight", "allowFlight");
        PROPERTY_ALIASES.put("fly", "allowFlight");
        PROPERTY_ALIASES.put("allowfly", "allowFlight");
    }

    private final boolean keepSpawnFallback;

    public WorldProperties(final Map<String, Object> values) {
        super(values);
        final Object keepSpawnObject = values.get("keepSpawnInMemory");
        keepSpawnFallback = keepSpawnObject == null || Boolean.parseBoolean(keepSpawnObject.toString());
    }

    public WorldProperties() {
        super();
        keepSpawnFallback = true;
    }

    public WorldProperties(final boolean fixSpawn, final Environment environment) {
        super();
        if (!fixSpawn) {
            adjustSpawn = false;
        }
        setScaling(getDefaultScale(environment));
        keepSpawnFallback = true;
    }

    private static double getDefaultScale(final Environment environment) {
        if (environment == Environment.NETHER) {
            return 8.0; // SUPPRESS CHECKSTYLE: MagicNumberCheck
        }
        else if (environment == Environment.THE_END) {
            return 16.0; // SUPPRESS CHECKSTYLE: MagicNumberCheck
        }
        return 1.0;
    }

    void setMVWorld(final MVWorld world) {
        registerObjectUsing(world);
        registerGlobalValidator(new WorldPropertyValidator());
    }

    void setValidator(final String fieldName, final Validator validator) {
        registerValidator(fieldName, validator);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyValues(final SerializationConfig other) {
        super.copyValues(other);
    }

    /**
     * This prepares the MVWorld for unloading.
     */
    public void cacheVirtualProperties() {
        try {
            buildVPropChanges();
        }
        catch (final IllegalStateException e) {
            // do nothing
        }
    }

    // --------------------------------------------------------------
    // Begin properties
    @Property(description = "Sorry, 'hidden' must either be: true or false.")
    private volatile boolean hidden;
    @Property(description = "Alias must be a valid string.")
    private volatile String alias;
    @Property(serializor = EnumPropertySerializor.class, description = "Sorry, 'color' must be a valid color-name.")
    private volatile EnglishChatColor color;
    @Property(serializor = EnumPropertySerializor.class, description = "Sorry, 'style' must be a valid style-name.")
    private volatile EnglishChatStyle style;
    @Property(description = "Sorry, 'pvp' must either be: true or false.", virtualType = Boolean.class, persistVirtual = true)
    volatile VirtualProperty<Boolean> pvp; // SUPPRESS CHECKSTYLE: VisibilityModifier
    @Property(description = "Scale must be a positive double value. ex: 2.3")
    private volatile double scale;
    @Property(description = "You must set this to the NAME not alias of a world.")
    private volatile String respawnWorld;
    @Property(description = "Sorry, this must either be: true or false.")
    private volatile boolean allowWeather;
    @Property(serializor = DifficultyPropertySerializor.class, virtualType = Difficulty.class, persistVirtual = true,
            description = "Difficulty must be set as one of the following: peaceful easy normal hard")
    volatile VirtualProperty<Difficulty> difficulty; // SUPPRESS CHECKSTYLE: VisibilityModifier
    @Property(description = "Sorry, 'animals' must either be: true or false.")
    private volatile SpawnSettings spawning;
    @Property
    volatile EntryFee entryfee;
    @Property(description = "Sorry, 'hunger' must either be: true or false.")
    private volatile boolean hunger;
    @Property(description = "Sorry, 'autoheal' must either be: true or false.")
    private volatile boolean autoHeal;
    @Property(description = "Sorry, 'adjustspawn' must either be: true or false.")
    private volatile boolean adjustSpawn;
    @Property(serializor = EnumPropertySerializor.class, description = "Allow portal forming must be NONE, ALL, NETHER or END.")
    private volatile AllowedPortalType portalForm;
    @Property(serializor = GameModePropertySerializor.class, description = "GameMode must be set as one of the following: survival creative")
    private volatile GameMode gameMode;
    @Property(description = "Sorry, this must either be: true or false.", virtualType = Boolean.class, persistVirtual = true)
    volatile VirtualProperty<Boolean> keepSpawnInMemory; // SUPPRESS CHECKSTYLE: VisibilityModifier
    @Property
    volatile SpawnLocation spawnLocation; // SUPPRESS CHECKSTYLE: VisibilityModifier
    @Property(virtualType = Location.class,
            description = "There is no help available for this variable. Go bug Rigby90 about it.")
    volatile VirtualProperty<Location> spawn; // SUPPRESS CHECKSTYLE: VisibilityModifier
    @Property(description = "Set this to false ONLY if you don't want this world to load itself on server restart.")
    private volatile boolean autoLoad;
    @Property(description = "If a player dies in this world, shoudld they go to their bed?")
    private volatile boolean bedRespawn;
    @Property
    private volatile List<String> worldBlacklist;
    @Property(serializor = TimePropertySerializor.class, virtualType = Long.class,
            description = "Set the time to whatever you want! (Will NOT freeze time)")
    volatile VirtualProperty<Long> time; // SUPPRESS CHECKSTYLE: VisibilityModifier
    @Property
    volatile Environment environment; // SUPPRESS CHECKSTYLE: VisibilityModifier
    @Property
    volatile long seed; // SUPPRESS CHECKSTYLE: VisibilityModifier
    @Property
    private volatile String generator;
    @Property
    private volatile int playerLimit;
    @Property
    private volatile boolean allowFlight;
    // End of properties
    // --------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setDefaults() {
        hidden         = false;
        alias          = "";
        color          = EnglishChatColor.WHITE;
        style          = EnglishChatStyle.NORMAL;
        scale          = 1D;
        respawnWorld   = "";
        allowWeather   = true;
        spawning       = new SpawnSettings();
        entryfee       = new EntryFee();
        hunger         = true;
        autoHeal       = true;
        adjustSpawn    = true;
        portalForm     = AllowedPortalType.ALL;
        gameMode       = GameMode.SURVIVAL;
        spawnLocation  = new NullLocation();
        autoLoad       = true;
        bedRespawn     = true;
        worldBlacklist = new ArrayList<>();
        generator      = null;
        playerLimit    = -1;
        allowFlight    = true;
    }

    void flushChanges() {
        flushPendingVPropChanges();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) {
        setPropertyValueUnchecked("alias", alias);
    }

    public Environment getEnvironment() {
        return environment;
    }

    /**
     * getAliases().
     *
     * @return The alias-map.
     * @see SerializationConfig
     */
    protected static Map<String, String> getAliases() {
        return PROPERTY_ALIASES;
    }

    public void setEnvironment(final Environment environment) {
        setPropertyValueUnchecked("environment", environment);
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(final long seed) {
        setPropertyValueUnchecked("seed", seed);
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(final String generator) {
        setPropertyValueUnchecked("generator", generator);
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public void setPlayerLimit(final int limit) {
        setPropertyValueUnchecked("playerLimit", limit);
    }

    public boolean canAnimalsSpawn() {
        return spawning.getAnimalSettings().doSpawn();
    }

    public void setAllowAnimalSpawn(final boolean animals) {
        setPropertyValueUnchecked("spawning.animals.spawn", animals);
    }

    public List<String> getAnimalList() {
        // These don't fire events at the moment. Should they?
        return spawning.getAnimalSettings().getExceptions();
    }

    public boolean canMonstersSpawn() {
        return spawning.getMonsterSettings().doSpawn();
    }

    public void setAllowMonsterSpawn(final boolean monsters) {
        setPropertyValueUnchecked("spawning.monsters.spawn", monsters);
    }

    public int getAnimalSpawnRate() {
        return spawning.getAnimalSettings().getSpawnRate();
    }

    public int getMonsterSpawnRate() {
        return spawning.getMonsterSettings().getSpawnRate();
    }

    public List<String> getMonsterList() {
        // These don't fire events at the moment. Should they?
        return spawning.getMonsterSettings().getExceptions();
    }

    public boolean isPVPEnabled() {
        return pvp.get();
    }

    public void setPVPMode(final boolean pvp) {
        setPropertyValueUnchecked("pvp", pvp);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        setPropertyValueUnchecked("hidden", hidden);
    }

    public List<String> getWorldBlacklist() {
        return worldBlacklist;
    }

    public double getScaling() {
        return scale;
    }

    public boolean setScaling(final double scaling) {
        return setPropertyValueUnchecked("scale", scaling);
    }

    public boolean setColor(final String aliasColor) {
        return setPropertyUnchecked("color", aliasColor);
    }

    public boolean setColor(final EnglishChatColor color) {
        return setPropertyValueUnchecked("color", color);
    }

    public EnglishChatColor getColor() {
        return color;
    }

    public String getRespawnToWorld() {
        return respawnWorld;
    }

    public boolean setRespawnToWorld(final String respawnToWorld) {
        return setPropertyValueUnchecked("respawnWorld", respawnToWorld);
    }

    public Material getCurrency() {
        return entryfee.getCurrency();
    }

    public void setCurrency(@Nullable final Material currency) {
        setPropertyValueUnchecked("entryfee.currency", currency);
    }

    public double getPrice() {
        return entryfee.getAmount();
    }

    public void setPrice(final double price) {
        setPropertyValueUnchecked("entryfee.amount", price);
    }

    public boolean setGameMode(final String mode) {
        return setPropertyUnchecked("gameMode", mode);
    }

    public boolean setGameMode(final GameMode mode) {
        return setPropertyValueUnchecked("gameMode", mode);
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setEnableWeather(final boolean weather) {
        setPropertyValueUnchecked("allowWeather", weather);
    }

    public boolean isWeatherEnabled() {
        return allowWeather;
    }

    public boolean isKeepingSpawnInMemory() {
        if (keepSpawnInMemory == null) {
            return keepSpawnFallback;
        }
        try {
            return keepSpawnInMemory.get();
        }
        catch (final IllegalStateException e) {
            return keepSpawnFallback;
        }
    }

    public void setKeepSpawnInMemory(final boolean value) {
        setPropertyValueUnchecked("keepSpawnInMemory", value);
    }

    public boolean getHunger() {
        return hunger;
    }

    public void setHunger(final boolean hunger) {
        setPropertyValueUnchecked("hunger", hunger);
    }

    public Location getSpawnLocation() {
        return spawn.get();
    }

    public void setSpawnLocation(final Location l) {
        setPropertyValueUnchecked("spawn", l);
    }

    public Difficulty getDifficulty() {
        return difficulty.get();
    }

    @Deprecated // SUPPRESS CHECKSTYLE: Deprecated
    public boolean setDifficulty(final String difficulty) {
        return setPropertyUnchecked("difficulty", difficulty);
    }

    public boolean setDifficulty(final Difficulty difficulty) {
        return setPropertyValueUnchecked("difficulty", difficulty);
    }

    public boolean getAutoHeal() {
        return autoHeal;
    }

    public void setAutoHeal(final boolean heal) {
        setPropertyValueUnchecked("autoHeal", heal);
    }

    public boolean getAdjustSpawn() {
        return adjustSpawn;
    }

    public void setAdjustSpawn(final boolean adjust) {
        setPropertyValueUnchecked("adjustSpawn", adjust);
    }

    public boolean getAutoLoad() {
        return autoLoad;
    }

    public void setAutoLoad(final boolean load) {
        setPropertyValueUnchecked("autoLoad", load);
    }

    public boolean getBedRespawn() {
        return bedRespawn;
    }

    public void setBedRespawn(final boolean respawn) {
        setPropertyValueUnchecked("bedRespawn", respawn);
    }

    public String getAllPropertyNames() {
        ChatColor myColor = ChatColor.AQUA;
        final StringBuilder result = new StringBuilder();
        final Map<String, Object> serialized = serialize();
        for (final String key : serialized.keySet()) {
            result.append(myColor).append(key).append(' ');
            myColor = (myColor == ChatColor.AQUA) ? ChatColor.GOLD : ChatColor.AQUA;
        }
        return result.toString();
    }

    public String getTime() {
        return getPropertyUnchecked("time");
    }

    public boolean setTime(final String timeAsString) {
        return setPropertyUnchecked("time", timeAsString);
    }

    public void allowPortalMaking(final AllowedPortalType portalType) {
        setPropertyValueUnchecked("portalForm", portalType);
    }

    public boolean setStyle(final String style) {
        return setPropertyUnchecked("style", style);
    }

    public boolean getAllowFlight() {
        return allowFlight;
    }

    public void setAllowFlight(final boolean allowFlight) {
        setPropertyValueUnchecked("allowFlight", allowFlight);
    }

    public AllowedPortalType getAllowedPortals() {
        return portalForm;
    }

    /**
     * Serializor for the color-property.
     */
    private static final class EnumPropertySerializor<T extends Enum<T>> implements Serializor<T, String> {
        @Override
        public String serialize(final T from) {
            return from.toString();
        }

        @Override
        public T deserialize(final String serialized, final Class<T> wanted) throws IllegalPropertyValueException {
            try {
                return Enum.valueOf(wanted, serialized.toUpperCase());
            }
            catch (final IllegalArgumentException e) {
                throw new IllegalPropertyValueException(e);
            }
        }
    }

    public EnglishChatStyle getStyle() {
        return style;
    }

    /**
     * Serializor for the difficulty-property.
     */
    private static final class DifficultyPropertySerializor implements Serializor<Difficulty, String> {
        @Override
        public String serialize(final Difficulty from) {
            return from.toString();
        }

        @Override
        public Difficulty deserialize(final String serialized, final Class<Difficulty> wanted) throws IllegalPropertyValueException {
            try {
                return Difficulty.getByValue(Integer.parseInt(serialized));
            }
            catch (final Exception ignored) {
            }
            try {
                return Difficulty.valueOf(serialized.toUpperCase());
            }
            catch (final Exception ignored) {
            }
            throw new IllegalPropertyValueException();
        }
    }

    /**
     * Serializor for the gameMode-property.
     */
    private static final class GameModePropertySerializor implements Serializor<GameMode, String> {
        @Override
        public String serialize(final GameMode from) {
            return from.toString();
        }

        @Override
        public GameMode deserialize(final String serialized, final Class<GameMode> wanted) throws IllegalPropertyValueException {
            try {
                return GameMode.getByValue(Integer.parseInt(serialized));
            }
            catch (final NumberFormatException ignored) {
            }
            try {
                return GameMode.valueOf(serialized.toUpperCase());
            }
            catch (final Exception ignored) {
            }
            throw new IllegalPropertyValueException();
        }
    }

    /**
     * Serializor for the time-property.
     */
    private static final class TimePropertySerializor implements Serializor<Long, String> {
        // BEGIN CHECKSTYLE-SUPPRESSION: MagicNumberCheck
        private static final String TIME_REGEX = "(\\d\\d?):?(\\d\\d)([ap])?m?";
        private static final Map<String, String> TIME_ALIASES;

        static {
            final Map<String, String> staticTimes = new HashMap<>();
            staticTimes.put("morning", "8:00");
            staticTimes.put("day", "12:00");
            staticTimes.put("noon", "12:00");
            staticTimes.put("midnight", "0:00");
            staticTimes.put("night", "20:00");

            // now set TIME_ALIASES to a "frozen" map
            TIME_ALIASES = Collections.unmodifiableMap(staticTimes);
        }

        @Override
        public String serialize(final Long from) {
            // I'm tired, so they get time in 24 hour for now.
            // Someone else can add 12 hr format if they want :P

            final int hours = (int) ((from / 1000 + 8) % 24);
            final int minutes = (int) (60 * (from % 1000) / 1000);

            return String.format("%d:%02d", hours, minutes);
        }

        @Override
        public Long deserialize(String serialized, final Class<Long> wanted) throws IllegalPropertyValueException {
            if (TIME_ALIASES.containsKey(serialized.toLowerCase())) {
                serialized = TIME_ALIASES.get(serialized.toLowerCase());
            }
            // Regex that extracts a time in the following formats:
            // 11:11pm, 11:11, 23:11, 1111, 1111p, and the aliases at the top of this file.
            final Pattern pattern = Pattern.compile(TIME_REGEX, Pattern.CASE_INSENSITIVE);
            final Matcher matcher = pattern.matcher(serialized);
            matcher.find();
            int hour = 0;
            double minute = 0;
            final int count = matcher.groupCount();
            if (count >= 2) {
                hour   = Integer.parseInt(matcher.group(1));
                minute = Integer.parseInt(matcher.group(2));
            }
            // If there were 4 matches (all, hour, min, am/pm)
            if (count == 4) {
                // We want 24 hour time for calcs, but if they
                // added a p[m], turn it into a 24 hr one.
                if (matcher.group(3).equals("p")) {
                    hour += 12;
                }
            }
            // Translate 24th hour to 0th hour.
            if (hour == 24) {
                hour = 0;
            }
            // Clamp the hour
            if (hour > 23 || hour < 0) {
                throw new IllegalPropertyValueException("Illegal hour!");
            }
            // Clamp the minute
            if (minute > 59 || minute < 0) {
                throw new IllegalPropertyValueException("Illegal minute!");
            }
            // 60 seconds in a minute, time needs to be in hrs * 1000, per
            // the bukkit docs.
            double totaltime = (hour + (minute / 60.0)) * 1000;
            // Somehow there's an 8 hour offset...
            totaltime -= 8000;
            if (totaltime < 0) {
                totaltime = 24000 + totaltime;
            }

            return (long) totaltime;
        }
        // END CHECKSTYLE-SUPPRESSION: MagicNumberCheck
    }
}
