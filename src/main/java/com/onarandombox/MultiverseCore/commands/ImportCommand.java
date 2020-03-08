/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Imports a new world of the specified type.
 */
public class ImportCommand extends MultiverseCommand {
    private static final Pattern RELATIVE_PATHS = Pattern.compile("^[./\\\\]+");
    private final MVWorldManager worldManager;

    public ImportCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Import World");
        setCommandUsage("/mv import" + ChatColor.GREEN + " {NAME} {ENV}" + ChatColor.GOLD + " -g [GENERATOR[:ID]] [-n]");
        setArgRange(1, 5); // SUPPRESS CHECKSTYLE: MagicNumberCheck
        addKey("mvimport");
        addKey("mvim");
        addKey("mv import");
        addCommandExample("/mv import " + ChatColor.GOLD + "gargamel" + ChatColor.GREEN + " normal");
        addCommandExample("/mv import " + ChatColor.GOLD + "hell_world" + ChatColor.GREEN + " nether");
        addCommandExample("To import a world that uses a generator, you'll need the generator:");
        addCommandExample("/mv import " + ChatColor.GOLD + "CleanRoom" + ChatColor.GREEN + " normal" + ChatColor.DARK_AQUA + " CleanRoomGenerator");
        setPermission("multiverse.core.import", "Imports a new world of the specified type.", PermissionDefault.OP);
        worldManager = this.plugin.getMVWorldManager();
    }

    /**
     * A very basic check to see if a folder has a level.dat file.
     * If it does, we can safely assume it's a world folder.
     *
     * @param worldFolder The File that may be a world.
     *
     * @return True if it looks like a world, false if not.
     */
    private static boolean checkIfIsWorld(final File worldFolder) {
        if (worldFolder.isDirectory()) {
            final File[] files = worldFolder.listFiles((file, name) -> name.toLowerCase().endsWith(".dat"));
            return files != null && files.length > 0;
        }
        return false;
    }

    private String getPotentialWorlds() {
        final File worldFolder = plugin.getServer().getWorldContainer();
        final File[] files = worldFolder.listFiles();
        final StringBuilder worldList = new StringBuilder();
        final Collection<MultiverseWorld> worlds = worldManager.getMVWorlds();
        final List<String> worldStrings = new ArrayList<>();
        for (final MultiverseWorld world : worlds) {
            worldStrings.add(world.getName());
        }
        worldStrings.addAll(worldManager.getUnloadedWorlds());
        ChatColor currColor = ChatColor.WHITE;
        assert files != null;
        for (final File file : files) {
            if (file.isDirectory() && checkIfIsWorld(file) && !worldStrings.contains(file.getName())) {
                worldList.append(currColor).append(file.getName()).append(" ");
                if (currColor == ChatColor.WHITE) {
                    currColor = ChatColor.YELLOW;
                }
                else {
                    currColor = ChatColor.WHITE;
                }
            }
        }
        return worldList.toString();
    }

    private String trimWorldName(final String userInput) {
        // Removes relative paths.
        return RELATIVE_PATHS.matcher(userInput).replaceAll("");
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        final String worldName = trimWorldName(args.get(0));

        if (worldName.toLowerCase().equals("--list") || worldName.toLowerCase().equals("-l")) {
            final String worldList = getPotentialWorlds();
            if (worldList.length() > 2) {
                sender.sendMessage(ChatColor.AQUA + "====[ These look like worlds ]====");
                sender.sendMessage(worldList);
            }
            else {
                sender.sendMessage(ChatColor.RED + "No potential worlds found. Sorry!");
            }
            return;
        }
        // Since we made an exception for the list, we have to make sure they have at least 2 params:
        // Note the exception is --list, which is covered above.
        if (args.size() == 1 || worldName.length() < 1) {
            showHelp(sender);
            return;
        }

        // Make sure we don't already know about this world.
        if (worldManager.isMVWorld(worldName)) {
            sender.sendMessage(ChatColor.GREEN + "Multiverse" + ChatColor.WHITE
                                       + " already knows about '" + ChatColor.AQUA + worldName + ChatColor.WHITE + "'!");
            return;
        }

        final File worldFile = new File(plugin.getServer().getWorldContainer(), worldName);

        final String generator = CommandHandler.getFlag("-g", args);
        boolean useSpawnAdjust = true;
        for (final String s : args) {
            if (s.equalsIgnoreCase("-n")) {
                useSpawnAdjust = false;
                break;
            }
        }

        final String env = args.get(1);
        final Environment environment = EnvironmentCommand.getEnvFromString(env);
        if (environment == null) {
            sender.sendMessage(ChatColor.RED + "That is not a valid environment.");
            EnvironmentCommand.showEnvironments(sender);
            return;
        }

        if (!worldFile.exists()) {
            sender.sendMessage(ChatColor.RED + "FAILED.");
            final String worldList = getPotentialWorlds();
            sender.sendMessage("That world folder does not exist. These look like worlds to me:");
            sender.sendMessage(worldList);
        } else if (!checkIfIsWorld(worldFile)) {
            sender.sendMessage(ChatColor.RED + "FAILED.");
            sender.sendMessage(String.format("'%s' does not appear to be a world. It is lacking a .dat file.",
                                             worldName));
        } else if (env == null) {
            sender.sendMessage(ChatColor.RED + "FAILED.");
            sender.sendMessage("That world environment did not exist.");
            sender.sendMessage("For a list of available world types, type: " + ChatColor.AQUA + "/mvenv");
        } else {
            Command.broadcastCommandMessage(sender, String.format("Starting import of world '%s'...", worldName));
            if (worldManager.addWorld(worldName, environment, null, null, null, generator, useSpawnAdjust))
                Command.broadcastCommandMessage(sender, ChatColor.GREEN + "Complete!");
            else
                Command.broadcastCommandMessage(sender, ChatColor.RED + "Failed!");
        }
    }
}
