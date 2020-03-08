package com.onarandombox.MultiverseCore.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.main__.util.SerializationConfig.Property;
import me.main__.util.SerializationConfig.SerializationConfig;

import org.bukkit.configuration.serialization.SerializableAs;

/**
 * SpawnSubSettings.
 */
@SerializableAs("MVSpawnSubSettings")
public class SubSpawnSettings extends SerializationConfig {
    @Property
    private boolean spawn;
    @Property
    private int spawnrate;
    @Property
    private List<String> exceptions;

    public SubSpawnSettings() {
        super();
    }

    public SubSpawnSettings(final Map<String, Object> values) {
        super(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaults() {
        spawn      = true;
        exceptions = new ArrayList<>();
        spawnrate  = -1;
    }

    /**
     * @return spawn
     */
    public boolean doSpawn() {
        return spawn;
    }

    /**
     * @param spawn The new value.
     */
    public void setSpawn(final boolean spawn) {
        this.spawn = spawn;
    }

    /**
     * @return The exceptions
     */
    public List<String> getExceptions() {
        return exceptions;
    }

    /**
     * @return The spawn rate
     */
    public int getSpawnRate() {
        return spawnrate;
    }

    /**
     * @param rate The new spawn rate
     */
    public void setSpawnRate(final int rate) {
        spawnrate = rate;
    }
}
