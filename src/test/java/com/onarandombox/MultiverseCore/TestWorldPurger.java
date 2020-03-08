package com.onarandombox.MultiverseCore;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.WorldPurger;
import com.onarandombox.MultiverseCore.utils.TestInstanceCreator;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MultiverseCore.class, PluginDescriptionFile.class, JavaPluginLoader.class })
@PowerMockIgnore("javax.script.*")
public class TestWorldPurger {
    TestInstanceCreator creator;
    MultiverseCore core;
    WorldPurger purger;

    MultiverseWorld mvWorld;
    World cbworld;

    Sheep sheep;
    Zombie zombie;

    @Before
    public void setUp() {
        creator = new TestInstanceCreator();
        assertTrue(creator.setUp());
        core   = creator.getCore();
        purger = core.getMVWorldManager().getTheWorldPurger();
        core.getMVConfig().setGlobalDebug(3);
        mvWorld = mock(MultiverseWorld.class);
        cbworld = mock(World.class);
        when(mvWorld.getCBWorld()).thenReturn(cbworld);
    }

    @After
    public void tearDown() {
        creator.tearDown();
    }

    @Test
    public void test() {
        // test 1: purge ALL without negations ==> both should be removed
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("ALL"), false, false);
        verify(sheep).remove();
        verify(zombie).remove();

        // test 2: purge ALL with one negation ==> the zombie should survive
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("ALL"), false, true);
        verify(sheep).remove();
        verify(zombie, never()).remove();

        // test 3: purge ALL with both negations ==> everybody should survive
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("ALL"), true, true);
        verify(sheep, never()).remove();
        verify(zombie, never()).remove();

        // test 4: purge ANIMALS without negations ==> the zombie should survive
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("ANIMALS"), false, false);
        verify(sheep).remove();
        verify(zombie, never()).remove();

        // test 5: purge MONSTERS with one negation ==> nobody should survive
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("MONSTERS"), true, false);
        verify(sheep).remove();
        verify(zombie).remove();

        // test 6: purge MONSTERS both negations ==> the zombie should survive
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("MONSTERS"), true, true);
        verify(sheep).remove();
        verify(zombie, never()).remove();

        // test 7: purge SHEEP without negations ==> the zombie should survive
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("SHEEP"), false, false);
        verify(sheep).remove();
        verify(zombie, never()).remove();

        // test 8: purge SHEEP with one negation ==> nobody should survive
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("SHEEP"), false, true);
        verify(sheep).remove();
        verify(zombie).remove();

        // test 9: purge ZOMBIE with both negations ==> the zombie should survive
        createAnimals();
        purger.purgeWorld(mvWorld, Collections.singletonList("ZOMBIE"), true, true);
        verify(sheep).remove();
        verify(zombie, never()).remove();

        // I like sheep.
    }

    private void createAnimals() {
        final World world = mvWorld.getCBWorld();
        sheep = mock(Sheep.class);
        when(sheep.getType()).thenReturn(EntityType.SHEEP);
        when(sheep.getWorld()).thenReturn(world);
        zombie = mock(Zombie.class);
        when(zombie.getType()).thenReturn(EntityType.ZOMBIE);
        when(zombie.getWorld()).thenReturn(world);
        when(cbworld.getEntities()).thenReturn(Arrays.asList(sheep, zombie));
    }
}
