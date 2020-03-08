package com.onarandombox.MultiverseCore.configuration;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

/**
 * Just like a regular {@link Location}, however {@code world} is usually {@code null}
 * or just a weak reference and it implements {@link ConfigurationSerializable}.
 */
@SerializableAs("MVSpawnLocation")
public class SpawnLocation extends Location {
    private Reference<World> worldRef;

    public SpawnLocation(final double x, final double y, final double z) {
        super(null, x, y, z);
    }

    public SpawnLocation(final double x, final double y, final double z, final float yaw, final float pitch) {
        super(null, x, y, z, yaw, pitch);
    }

    public SpawnLocation(final Location loc) {
        this(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Let Bukkit be able to deserialize this.
     *
     * @param args The map.
     *
     * @return The deserialized object.
     */
    public static SpawnLocation deserialize(final Map<String, Object> args) {
        final double x = ((Number) args.get("x")).doubleValue();
        final double y = ((Number) args.get("y")).doubleValue();
        final double z = ((Number) args.get("z")).doubleValue();
        final float pitch = ((Number) args.get("pitch")).floatValue();
        final float yaw = ((Number) args.get("yaw")).floatValue();
        return new SpawnLocation(x, y, z, yaw, pitch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getWorld() {
        return (worldRef != null) ? worldRef.get() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorld(final World world) {
        worldRef = new WeakReference<>(world);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Chunk getChunk() {
        if ((worldRef != null) && (worldRef.get() != null))
            return worldRef.get().getChunkAt(this);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Block getBlock() {
        if ((worldRef != null) && (worldRef.get() != null))
            return worldRef.get().getBlockAt(this);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> serialized = new HashMap<>(5); // SUPPRESS CHECKSTYLE: MagicNumberCheck
        serialized.put("x", getX());
        serialized.put("y", getY());
        serialized.put("z", getZ());
        serialized.put("pitch", getPitch());
        serialized.put("yaw", getYaw());
        return serialized;
    }
}
