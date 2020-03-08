/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.generator.ChunkGenerator;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;

public class MockWorldFactory {

    private static final Map<String, World> createdWorlds = new LinkedHashMap<>();
    private static final Map<UUID, World> worldUIDS = new HashMap<>();

    private static final Map<World, Boolean> pvpStates = new WeakHashMap<>();
    private static final Map<World, Boolean> keepSpawnInMemoryStates = new WeakHashMap<>();
    private static final Map<World, Difficulty> difficultyStates = new WeakHashMap<>();

    private MockWorldFactory() {
    }

    private static void registerWorld(final World world) {
        createdWorlds.put(world.getName(), world);
        worldUIDS.put(world.getUID(), world);
        createWorldDirectory(world.getName());
    }

    public static void createWorldDirectory(final String worldName) {
        final File worldFolder = new File(TestInstanceCreator.worldsDirectory, worldName);
        worldFolder.mkdir();
        try {
            new File(worldFolder, "level.dat").createNewFile();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static World basics(final String world, final World.Environment env, final WorldType type) {
        final World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn(world);
        when(mockWorld.getPVP()).thenAnswer((Answer<Boolean>) invocation -> {
            final World w = (World) invocation.getMock();
            if (!pvpStates.containsKey(w))
                pvpStates.put(w, true); // default value
            return pvpStates.get(w);
        });
        doAnswer((Answer<Void>) invocation -> {
            pvpStates.put((World) invocation.getMock(), (Boolean) invocation.getArguments()[0]);
            return null;
        }).when(mockWorld).setPVP(anyBoolean());
        when(mockWorld.getKeepSpawnInMemory()).thenAnswer((Answer<Boolean>) invocation -> {
            final World w = (World) invocation.getMock();
            if (!keepSpawnInMemoryStates.containsKey(w))
                keepSpawnInMemoryStates.put(w, true); // default value
            return keepSpawnInMemoryStates.get(w);
        });
        doAnswer((Answer<Void>) invocation -> {
            keepSpawnInMemoryStates.put((World) invocation.getMock(), (Boolean) invocation.getArguments()[0]);
            return null;
        }).when(mockWorld).setKeepSpawnInMemory(anyBoolean());
        when(mockWorld.getDifficulty()).thenAnswer((Answer<Difficulty>) invocation -> {
            final World w = (World) invocation.getMock();
            if (!difficultyStates.containsKey(w))
                difficultyStates.put(w, Difficulty.NORMAL); // default value
            return difficultyStates.get(w);
        });
        doAnswer((Answer<Void>) invocation -> {
            difficultyStates.put((World) invocation.getMock(), (Difficulty) invocation.getArguments()[0]);
            return null;
        }).when(mockWorld).setDifficulty(any(Difficulty.class));
        when(mockWorld.getEnvironment()).thenReturn(env);
        when(mockWorld.getWorldType()).thenReturn(type);
        when(mockWorld.getSpawnLocation()).thenReturn(new Location(mockWorld, 0, 64, 0));
        when(mockWorld.getWorldFolder()).thenAnswer((Answer<File>) invocation -> {
            if (!(invocation.getMock() instanceof World))
                return null;

            final World thiss = (World) invocation.getMock();
            return new File(TestInstanceCreator.serverDirectory, thiss.getName());
        });
        when(mockWorld.getBlockAt(any(Location.class))).thenAnswer((Answer<Block>) invocation -> {
            final Location loc;
            try {
                loc = (Location) invocation.getArguments()[0];
            }
            catch (final Exception e) {
                return null;
            }
            Material blockType = Material.AIR;
            final Block mockBlock = mock(Block.class);
            if (loc.getBlockY() < 64) {
                blockType = Material.DIRT;
            }

            when(mockBlock.getType()).thenReturn(blockType);
            when(mockBlock.getWorld()).thenReturn(loc.getWorld());
            when(mockBlock.getX()).thenReturn(loc.getBlockX());
            when(mockBlock.getY()).thenReturn(loc.getBlockY());
            when(mockBlock.getZ()).thenReturn(loc.getBlockZ());
            when(mockBlock.getLocation()).thenReturn(loc);
            when(mockBlock.isEmpty()).thenReturn(blockType == Material.AIR);
            return mockBlock;
        });
        when(mockWorld.getUID()).thenReturn(UUID.randomUUID());
        return mockWorld;
    }

    private static World nullWorld(final String world, final World.Environment env, final WorldType type) {
        final World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn(world);
        when(mockWorld.getEnvironment()).thenReturn(env);
        when(mockWorld.getWorldType()).thenReturn(type);
        when(mockWorld.getSpawnLocation()).thenReturn(new Location(mockWorld, 0, 64, 0));
        when(mockWorld.getWorldFolder()).thenAnswer((Answer<File>) invocation -> {
            if (!(invocation.getMock() instanceof World))
                return null;

            final World thiss = (World) invocation.getMock();
            return new File(TestInstanceCreator.serverDirectory, thiss.getName());
        });
        when(mockWorld.getBlockAt(any(Location.class))).thenAnswer((Answer<Block>) invocation -> {
            final Location loc;
            try {
                loc = (Location) invocation.getArguments()[0];
            }
            catch (final Exception e) {
                return null;
            }

            final Block mockBlock = mock(Block.class);
            final Material blockType = Material.AIR;

            when(mockBlock.getType()).thenReturn(blockType);
            when(mockBlock.getWorld()).thenReturn(loc.getWorld());
            when(mockBlock.getX()).thenReturn(loc.getBlockX());
            when(mockBlock.getY()).thenReturn(loc.getBlockY());
            when(mockBlock.getZ()).thenReturn(loc.getBlockZ());
            when(mockBlock.getLocation()).thenReturn(loc);
            when(mockBlock.isEmpty()).thenReturn(true);
            return mockBlock;
        });
        when(mockWorld.getUID()).thenReturn(UUID.randomUUID());
        return mockWorld;
    }

    public static World makeNewMockWorld(final String world, final World.Environment env, final WorldType type) {
        final World w = basics(world, env, type);
        registerWorld(w);
        return w;
    }

    public static World makeNewNullMockWorld(final String world, final World.Environment env, final WorldType type) {
        final World w = nullWorld(world, env, type);
        registerWorld(w);
        return w;
    }

    public static World makeNewMockWorld(final String world, final World.Environment env, final WorldType type, final long seed,
                                         final ChunkGenerator generator) {
        final World mockWorld = basics(world, env, type);
        when(mockWorld.getGenerator()).thenReturn(generator);
        when(mockWorld.getSeed()).thenReturn(seed);
        registerWorld(mockWorld);
        return mockWorld;
    }

    public static World getWorld(final String name) {
        return createdWorlds.get(name);
    }

    public static World getWorld(final UUID worldUID) {
        return worldUIDS.get(worldUID);
    }

    public static List<World> getWorlds() {
        return new ArrayList<>(createdWorlds.values());
    }

    public static void clearWorlds() {
        for (final String name : createdWorlds.keySet())
            new File(TestInstanceCreator.worldsDirectory, name).delete();
        createdWorlds.clear();
        worldUIDS.clear();
    }
}
