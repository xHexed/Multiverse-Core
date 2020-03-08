package com.onarandombox.MultiverseCore;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.listeners.MVEntityListener;
import com.onarandombox.MultiverseCore.utils.MockWorldFactory;
import com.onarandombox.MultiverseCore.utils.TestInstanceCreator;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MultiverseCore.class, PluginDescriptionFile.class, JavaPluginLoader.class})
@PowerMockIgnore("javax.script.*")
public class TestEntitySpawnRules {
    TestInstanceCreator creator;
    MultiverseCore core;
    MVEntityListener listener;

    MultiverseWorld mvWorld;
    World cbworld;

    Sheep sheep;
    Zombie zombie;

    CreatureSpawnEvent sheepEvent;
    CreatureSpawnEvent zombieEvent;

    private static CreatureSpawnEvent mockSpawnEvent(final LivingEntity e, final SpawnReason reason) {
        final CreatureSpawnEvent event = mock(CreatureSpawnEvent.class);
        when(event.getEntity()).thenReturn(e);
        final EntityType type = e.getType();
        when(event.getEntityType()).thenReturn(type);
        when(event.getSpawnReason()).thenReturn(reason);
        return event;
    }

    @Before
    public void setUp() throws Exception {
        creator = new TestInstanceCreator();
        assertTrue(creator.setUp());
        core     = creator.getCore();
        listener = core.getEntityListener();

        mvWorld = mock(MultiverseWorld.class);
        cbworld = MockWorldFactory.makeNewMockWorld("world", World.Environment.NORMAL, WorldType.NORMAL);
        when(mvWorld.getCBWorld()).thenReturn(cbworld);

        final MVWorldManager worldman = mock(MVWorldManager.class);
        when(worldman.isMVWorld(anyString())).thenReturn(true);
        when(worldman.getMVWorld(anyString())).thenReturn(mvWorld);
        final Field worldmanfield = MVEntityListener.class.getDeclaredField("worldManager");
        worldmanfield.setAccessible(true);
        worldmanfield.set(listener, worldman);

        core.getMVConfig().setGlobalDebug(3);
    }

    @After
    public void tearDown() {
        creator.tearDown();
    }

    private void spawnAll(final SpawnReason reason) {
        sheepEvent  = mockSpawnEvent(sheep, reason);
        zombieEvent = mockSpawnEvent(zombie, reason);
        listener.creatureSpawn(sheepEvent);
        listener.creatureSpawn(zombieEvent);
    }

    private void spawnAllNatural() {
        spawnAll(SpawnReason.NATURAL);
    }

    private void adjustSettings(final boolean animalSpawn, final boolean monsterSpawn,
                                final List<String> animalExceptions, final List<String> monsterExceptions) {
        when(mvWorld.canAnimalsSpawn()).thenReturn(animalSpawn);
        when(mvWorld.canMonstersSpawn()).thenReturn(monsterSpawn);
        when(mvWorld.getAnimalList()).thenReturn(animalExceptions);
        when(mvWorld.getMonsterList()).thenReturn(monsterExceptions);
    }

    @Test
    public void test() {
        // test 1: no spawning at all allowed
        adjustSettings(false, false, Collections.emptyList(), Collections.emptyList());
        createAnimals();
        spawnAllNatural();
        verify(sheepEvent).setCancelled(true);
        verify(zombieEvent).setCancelled(true);

        // test 2: only monsters
        adjustSettings(false, true, Collections.emptyList(), Collections.emptyList());
        createAnimals();
        spawnAllNatural();
        verify(sheepEvent).setCancelled(true);
        verify(zombieEvent).setCancelled(false);

        // test 3: all spawning allowed
        adjustSettings(true, true, Collections.emptyList(), Collections.emptyList());
        createAnimals();
        spawnAllNatural();
        verify(sheepEvent).setCancelled(false);
        verify(zombieEvent).setCancelled(false);

        // test 4: no spawning with zombie exception
        adjustSettings(false, false, Collections.emptyList(), Collections.singletonList("ZOMBIE"));
        createAnimals();
        spawnAllNatural();
        verify(sheepEvent).setCancelled(true);
        verify(zombieEvent).setCancelled(false);

        // test 5: all spawning with sheep exception
        adjustSettings(true, true, Collections.singletonList("SHEEP"), Collections.emptyList());
        createAnimals();
        spawnAllNatural();
        verify(sheepEvent).setCancelled(true);
        verify(zombieEvent).setCancelled(false);

        // test 6: eggs
        adjustSettings(false, false, Collections.emptyList(), Collections.emptyList());
        createAnimals();
        spawnAll(SpawnReason.SPAWNER_EGG);
        verify(sheepEvent, never()).setCancelled(anyBoolean());
        verify(zombieEvent, never()).setCancelled(anyBoolean());
    }

    private void createAnimals() {
        sheep = mock(Sheep.class);
        when(sheep.getType()).thenReturn(EntityType.SHEEP);
        when(sheep.getWorld()).thenReturn(cbworld);
        zombie = mock(Zombie.class);
        when(zombie.getType()).thenReturn(EntityType.ZOMBIE);
        when(zombie.getWorld()).thenReturn(cbworld);

        when(cbworld.getEntities()).thenReturn(Arrays.asList(sheep, zombie));
    }
}
