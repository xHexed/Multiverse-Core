/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import buscript.Buscript;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.listeners.MVEntityListener;
import com.onarandombox.MultiverseCore.listeners.MVPlayerListener;
import com.onarandombox.MultiverseCore.listeners.MVWeatherListener;
import junit.framework.Assert;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockGateway;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;

public class TestInstanceCreator {
    private MultiverseCore core;
    private Server mockServer;
    private CommandSender commandSender;

    public static final File pluginDirectory = new File("bin/test/server/plugins/coretest");
    public static final File serverDirectory = new File("bin/test/server");
    public static final File worldsDirectory = new File("bin/test/server");

    public boolean setUp() {
        try {
            pluginDirectory.mkdirs();
            assertTrue(pluginDirectory.exists());

            MockGateway.MOCK_STANDARD_METHODS = false;

            // Initialize the Mock server.
            mockServer = mock(Server.class);
            final JavaPluginLoader mockPluginLoader = PowerMock.createMock(JavaPluginLoader.class);
            Whitebox.setInternalState(mockPluginLoader, "server", mockServer);
            when(mockServer.getName()).thenReturn("TestBukkit");
            Logger.getLogger("Minecraft").setParent(Util.logger);
            when(mockServer.getLogger()).thenReturn(Util.logger);
            when(mockServer.getWorldContainer()).thenReturn(worldsDirectory);

            // Return a fake PDF file.
            final PluginDescriptionFile pdf = PowerMockito.spy(new PluginDescriptionFile("Multiverse-Core", "2.2-Test",
                                                                                         "com.onarandombox.MultiverseCore.MultiverseCore"));
            when(pdf.getAuthors()).thenReturn(new ArrayList<>());
            core = PowerMockito.spy(new MultiverseCore(mockPluginLoader, pdf, pluginDirectory, new File(pluginDirectory, "testPluginFile")));
            PowerMockito.doAnswer((Answer<Void>) invocation -> {
                return null; // don't run metrics in tests
            }).when(core, "setupMetrics");

            // Let's let all MV files go to bin/test
            doReturn(pluginDirectory).when(core).getDataFolder();

            doReturn(true).when(core).isEnabled();
            doReturn(Util.logger).when(core).getLogger();
            core.setServerFolder(serverDirectory);

            // Add Core to the list of loaded plugins
            final JavaPlugin[] plugins = {core};

            // Mock the Plugin Manager
            final PluginManager mockPluginManager = PowerMockito.mock(PluginManager.class);
            when(mockPluginManager.getPlugins()).thenReturn(plugins);
            when(mockPluginManager.getPlugin("Multiverse-Core")).thenReturn(core);
            when(mockPluginManager.getPermission(anyString())).thenReturn(null);
            // Tell Buscript Vault is not available.
            when(mockPluginManager.getPermission("Vault")).thenReturn(null);

            // Make some fake folders to fool the fake MV into thinking these worlds exist
            final File worldNormalFile = new File(core.getServerFolder(), "world");
            Util.log("Creating world-folder: " + worldNormalFile.getAbsolutePath());
            worldNormalFile.mkdirs();
            final File worldNetherFile = new File(core.getServerFolder(), "world_nether");
            Util.log("Creating world-folder: " + worldNetherFile.getAbsolutePath());
            worldNetherFile.mkdirs();
            final File worldSkylandsFile = new File(core.getServerFolder(), "world_the_end");
            Util.log("Creating world-folder: " + worldSkylandsFile.getAbsolutePath());
            worldSkylandsFile.mkdirs();

            // Give the server some worlds
            when(mockServer.getWorld(anyString())).thenAnswer((Answer<World>) invocation -> {
                final String arg;
                try {
                    arg = (String) invocation.getArguments()[0];
                }
                catch (final Exception e) {
                    return null;
                }
                return MockWorldFactory.getWorld(arg);
            });

            when(mockServer.getWorld(any(UUID.class))).thenAnswer((Answer<World>) invocation -> {
                final UUID arg;
                try {
                    arg = (UUID) invocation.getArguments()[0];
                }
                catch (final Exception e) {
                    return null;
                }
                return MockWorldFactory.getWorld(arg);
            });

            when(mockServer.getWorlds()).thenAnswer((Answer<List<World>>) invocation -> MockWorldFactory.getWorlds());

            when(mockServer.getPluginManager()).thenReturn(mockPluginManager);

            when(mockServer.createWorld(ArgumentMatchers.isA(WorldCreator.class))).thenAnswer(
                    (Answer<World>) invocation -> {
                        final WorldCreator arg;
                        try {
                            arg = (WorldCreator) invocation.getArguments()[0];
                        }
                        catch (final Exception e) {
                            return null;
                        }
                        // Add special case for creating null worlds.
                        // Not sure I like doing it this way, but this is a special case
                        if (arg.name().equalsIgnoreCase("nullworld")) {
                            return MockWorldFactory.makeNewNullMockWorld(arg.name(), arg.environment(), arg.type());
                        }
                        return MockWorldFactory.makeNewMockWorld(arg.name(), arg.environment(), arg.type());
                    });

            when(mockServer.unloadWorld(anyString(), anyBoolean())).thenReturn(true);

            // add mock scheduler
            final BukkitScheduler mockScheduler = mock(BukkitScheduler.class);
            when(mockScheduler.scheduleSyncDelayedTask(any(Plugin.class), any(Runnable.class), anyLong())).
                    thenAnswer((Answer<Integer>) invocation -> {
                        final Runnable arg;
                        try {
                            arg = (Runnable) invocation.getArguments()[1];
                        }
                        catch (final Exception e) {
                            return null;
                        }
                        arg.run();
                        return null;
                    });
            when(mockScheduler.scheduleSyncDelayedTask(any(Plugin.class), any(Runnable.class))).
                    thenAnswer((Answer<Integer>) invocation -> {
                        final Runnable arg;
                        try {
                            arg = (Runnable) invocation.getArguments()[1];
                        }
                        catch (final Exception e) {
                            return null;
                        }
                        arg.run();
                        return null;
                    });
            when(mockServer.getScheduler()).thenReturn(mockScheduler);

            // Set server
            final Field serverfield = JavaPlugin.class.getDeclaredField("server");
            serverfield.setAccessible(true);
            serverfield.set(core, mockServer);

            // Set buscript
            final Buscript buscript = PowerMockito.spy(new Buscript(core));
            final Field buscriptfield = MultiverseCore.class.getDeclaredField("buscript");
            buscriptfield.setAccessible(true);
            buscriptfield.set(core, buscript);
            when(buscript.getPlugin()).thenReturn(core);

            // Set worldManager
            final WorldManager wm = PowerMockito.spy(new WorldManager(core));
            final Field worldmanagerfield = MultiverseCore.class.getDeclaredField("worldManager");
            worldmanagerfield.setAccessible(true);
            worldmanagerfield.set(core, wm);

            // Set playerListener
            final MVPlayerListener pl = PowerMockito.spy(new MVPlayerListener(core));
            final Field playerlistenerfield = MultiverseCore.class.getDeclaredField("playerListener");
            playerlistenerfield.setAccessible(true);
            playerlistenerfield.set(core, pl);

            // Set entityListener
            final MVEntityListener el = PowerMockito.spy(new MVEntityListener(core));
            final Field entitylistenerfield = MultiverseCore.class.getDeclaredField("entityListener");
            entitylistenerfield.setAccessible(true);
            entitylistenerfield.set(core, el);

            // Set weatherListener
            final MVWeatherListener wl = PowerMockito.spy(new MVWeatherListener(core));
            final Field weatherlistenerfield = MultiverseCore.class.getDeclaredField("weatherListener");
            weatherlistenerfield.setAccessible(true);
            weatherlistenerfield.set(core, wl);

            // Init our command sender
            final Logger commandSenderLogger = Logger.getLogger("CommandSender");
            commandSenderLogger.setParent(Util.logger);
            commandSender = mock(CommandSender.class);
            doAnswer((Answer<Void>) invocation -> {
                commandSenderLogger.info(ChatColor.stripColor((String) invocation.getArguments()[0]));
                return null;
            }).when(commandSender).sendMessage(anyString());
            when(commandSender.getServer()).thenReturn(mockServer);
            when(commandSender.getName()).thenReturn("MockCommandSender");
            when(commandSender.isPermissionSet(anyString())).thenReturn(true);
            when(commandSender.isPermissionSet(ArgumentMatchers.isA(Permission.class))).thenReturn(true);
            when(commandSender.hasPermission(anyString())).thenReturn(true);
            when(commandSender.hasPermission(ArgumentMatchers.isA(Permission.class))).thenReturn(true);
            when(commandSender.addAttachment(core)).thenReturn(null);
            when(commandSender.isOp()).thenReturn(true);

            Bukkit.setServer(mockServer);

            // Load Multiverse Core
            core.onLoad();

            // Enable it.
            core.onEnable();

            return true;
        }
        catch (final Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean tearDown() {
        final List<MultiverseWorld> worlds = new ArrayList<>(core.getMVWorldManager()
                                                                     .getMVWorlds());
        for (final MultiverseWorld world : worlds) {
            core.getMVWorldManager().deleteWorld(world.getName());
        }

        try {
            final Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(Class.forName("org.bukkit.Bukkit"), null);
        }
        catch (final Exception e) {
            Util.log(Level.SEVERE,
                     "Error while trying to unregister the server from Bukkit. Has Bukkit changed?");
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return false;
        }

        core.onDisable();

        FileUtils.deleteFolder(serverDirectory);
        MockWorldFactory.clearWorlds();

        return true;
    }

    public MultiverseCore getCore() {
        return core;
    }

    public Server getServer() {
        return mockServer;
    }

    public CommandSender getCommandSender() {
        return commandSender;
    }
}
