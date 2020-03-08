/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates a new world and loads it.
 */
public class CreateCommand extends MultiverseCommand {
    private final MVWorldManager worldManager;

    public CreateCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Create World");
        setCommandUsage(String.format("/mv create %s{NAME} {ENV} %s-s [SEED] -g [GENERATOR[:ID]] -t [WORLDTYPE] [-n] -a [true|false]",
                                      ChatColor.GREEN, ChatColor.GOLD));
        setArgRange(2, 11); // SUPPRESS CHECKSTYLE: MagicNumberCheck
        addKey("mvcreate");
        addKey("mvc");
        addKey("mv create");
        setPermission("multiverse.core.create", "Creates a new world and loads it.", PermissionDefault.OP);
        addCommandExample("/mv create " + ChatColor.GOLD + "world" + ChatColor.GREEN + " normal");
        addCommandExample("/mv create " + ChatColor.GOLD + "lavaland" + ChatColor.RED + " nether");
        addCommandExample("/mv create " + ChatColor.GOLD + "starwars" + ChatColor.AQUA + " end");
        addCommandExample("/mv create " + ChatColor.GOLD + "flatroom" + ChatColor.GREEN + " normal" + ChatColor.AQUA + " -t flat");
        addCommandExample("/mv create " + ChatColor.GOLD + "gargamel" + ChatColor.GREEN + " normal" + ChatColor.DARK_AQUA + " -s gargamel");
        addCommandExample("/mv create " + ChatColor.GOLD + "moonworld" + ChatColor.GREEN + " normal" + ChatColor.DARK_AQUA + " -g BukkitFullOfMoon");
        worldManager = this.plugin.getMVWorldManager();
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        final String worldName = args.get(0);
        final File worldFile = new File(plugin.getServer().getWorldContainer(), worldName);
        final String env = args.get(1);
        final String seed = CommandHandler.getFlag("-s", args);
        final String generator = CommandHandler.getFlag("-g", args);
        boolean allowStructures = true;
        final String structureString = CommandHandler.getFlag("-a", args);
        if (structureString != null) {
            allowStructures = Boolean.parseBoolean(structureString);
        }
        String typeString = CommandHandler.getFlag("-t", args);
        boolean useSpawnAdjust = true;
        for (final String s : args) {
            if (s.equalsIgnoreCase("-n")) {
                useSpawnAdjust = false;
                break;
            }
        }

        if (worldManager.isMVWorld(worldName)) {
            sender.sendMessage(ChatColor.RED + "Multiverse cannot create " + ChatColor.GOLD + ChatColor.UNDERLINE
                                       + "another" + ChatColor.RESET + ChatColor.RED + " world named " + worldName);
            return;
        }

        if (worldFile.exists()) {
            sender.sendMessage(ChatColor.RED + "A Folder/World already exists with this name!");
            sender.sendMessage(ChatColor.RED + "If you are confident it is a world you can import with /mvimport");
            return;
        }

        final Environment environment = EnvironmentCommand.getEnvFromString(env);
        if (environment == null) {
            sender.sendMessage(ChatColor.RED + "That is not a valid environment.");
            EnvironmentCommand.showEnvironments(sender);
            return;
        }

        // If they didn't specify a type, default to NORMAL
        if (typeString == null) {
            typeString = "NORMAL";
        }
        final WorldType type = EnvironmentCommand.getWorldTypeFromString(typeString);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "That is not a valid World Type.");
            EnvironmentCommand.showWorldTypes(sender);
            return;
        }
        // Determine if the generator is valid. #918
        if (generator != null) {
            final List<String> genarray = new ArrayList<>(Arrays.asList(generator.split(":")));
            if (genarray.size() < 2) {
                // If there was only one arg specified, pad with another empty one.
                genarray.add("");
            }
            if (worldManager.getChunkGenerator(genarray.get(0), genarray.get(1), "test") == null) {
                // We have an invalid generator.
                sender.sendMessage("Invalid generator! '" + generator + "'. " + ChatColor.RED + "Aborting world creation.");
                return;
            }
        }
        Command.broadcastCommandMessage(sender, "Starting creation of world '" + worldName + "'...");

        if (worldManager.addWorld(worldName, environment, seed, type, allowStructures, generator, useSpawnAdjust)) {
            Command.broadcastCommandMessage(sender, "Complete!");
        }
        else {
            Command.broadcastCommandMessage(sender, "FAILED.");
        }
    }
}
