/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseCore.utils.webpaste.BitlyURLShortener;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteFailedException;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteService;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceFactory;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceType;
import com.onarandombox.MultiverseCore.utils.webpaste.URLShortener;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dumps version info to the console.
 */
public class VersionCommand extends MultiverseCommand {
    private static final URLShortener SHORTENER = new BitlyURLShortener();

    public VersionCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Multiverse Version");
        setCommandUsage("/mv version " + ChatColor.GOLD + "-[bh] [--include-plugin-list]");
        setArgRange(0, 2);
        addKey("mv version");
        addKey("mvv");
        addKey("mvversion");
        setPermission("multiverse.core.version",
                      "Dumps version info to the console, optionally to pastie.org with -p or pastebin.com with a -b.", PermissionDefault.TRUE);
    }

    /**
     * Send the current contents of this.pasteBinBuffer to a web service.
     *
     * @param type       Service type to send paste data to.
     * @param pasteData  Legacy string only data to post to a service.
     * @param pasteFiles Map of filenames/contents of debug info.
     *
     * @return URL of visible paste
     */
    private static String postToService(final PasteServiceType type, final String pasteData,
                                        final Map<String, String> pasteFiles) {
        final PasteService ps = PasteServiceFactory.getService(type, true);
        try {
            final String result;
            assert ps != null;
            if (ps.supportsMultiFile()) {
                result = ps.postData(ps.encodeData(pasteFiles), ps.getPostURL());
            }
            else {
                result = ps.postData(ps.encodeData(pasteData), ps.getPostURL());
            }
            return SHORTENER.shorten(result);
        }
        catch (final PasteFailedException e) {
            System.out.print(e);
            return "Error posting to service";
        }
    }

    private String getLegacyString() {
        final String legacyFile = "[Multiverse-Core] Multiverse-Core Version: " + plugin.getDescription().getVersion() + '\n' +
                "[Multiverse-Core] Bukkit Version: " + plugin.getServer().getVersion() + '\n' +
                "[Multiverse-Core] Loaded Worlds: " + plugin.getMVWorldManager().getMVWorlds() + '\n' +
                "[Multiverse-Core] Multiverse Plugins Loaded: " + plugin.getPluginCount() + '\n' +
                "[Multiverse-Core] Economy being used: " + plugin.getEconomist().getEconomyName() + '\n' +
                "[Multiverse-Core] Permissions Plugin: " + plugin.getMVPerms().getType() + '\n' +
                "[Multiverse-Core] Dumping Config Values: (version " +
                plugin.getMVConfig().getVersion() + ")" + '\n' +
                "[Multiverse-Core]  messagecooldown: " + plugin.getMessaging().getCooldown() + '\n' +
                "[Multiverse-Core]  teleportcooldown: " + plugin.getMVConfig().getTeleportCooldown() + '\n' +
                "[Multiverse-Core]  enforceaccess: " + plugin.getMVConfig().getEnforceAccess() + '\n' +
                "[Multiverse-Core]  displaypermerrors: " + plugin.getMVConfig().getDisplayPermErrors() + '\n' +
                "[Multiverse-Core]  teleportintercept: " + plugin.getMVConfig().getTeleportIntercept() + '\n' +
                "[Multiverse-Core]  firstspawnoverride: " + plugin.getMVConfig().getFirstSpawnOverride() + '\n' +
                "[Multiverse-Core]  firstspawnworld: " + plugin.getMVConfig().getFirstSpawnWorld() + '\n' +
                "[Multiverse-Core]  debug: " + plugin.getMVConfig().getGlobalDebug() + '\n' +
                "[Multiverse-Core] Special Code: FRN002" + '\n';
        return legacyFile;
    }

    private String getMarkdownString() {
        final String markdownString = "# Multiverse-Core\n" +
                "## Overview\n" +
                "| Name | Value |\n" +
                "| --- | --- |\n" +
                "| Multiverse-Core Version | `" + plugin.getDescription().getVersion() + "` |\n" +
                "| Bukkit Version | `" + plugin.getServer().getVersion() + "` |\n" +
                //markdownString.append("| Loaded Worlds | `").append(this.plugin.getMVWorldManager().getMVWorlds()).append("` |\n");
                "| Multiverse Plugins Loaded | `" + plugin.getPluginCount() + "` |\n" +
                "| Economy being used | `" + plugin.getEconomist().getEconomyName() + "` |\n" +
                "| Permissions Plugin | `" + plugin.getMVPerms().getType() + "` |\n" +
                "## Parsed Config\n" +
                "These are what Multiverse thought the in-memory values of the config were.\n\n" +
                "| Config Key  | Value |\n" +
                "| --- | --- |\n" +
                "| version | `" + plugin.getMVConfig().getVersion() + "` |\n" +
                "| messagecooldown | `" + plugin.getMessaging().getCooldown() + "` |\n" +
                "| teleportcooldown | `" + plugin.getMVConfig().getTeleportCooldown() + "` |\n" +
                "| enforceaccess | `" + plugin.getMVConfig().getEnforceAccess() + "` |\n" +
                "| displaypermerrors | `" + plugin.getMVConfig().getDisplayPermErrors() + "` |\n" +
                "| teleportintercept | `" + plugin.getMVConfig().getTeleportIntercept() + "` |\n" +
                "| firstspawnoverride | `" + plugin.getMVConfig().getFirstSpawnOverride() + "` |\n" +
                "| firstspawnworld | `" + plugin.getMVConfig().getFirstSpawnWorld() + "` |\n" +
                "| debug | `" + plugin.getMVConfig().getGlobalDebug() + "` |\n";
        return markdownString;
    }

    private String readFile(final String filename) {
        StringBuilder result;
        try {
            final FileReader reader = new FileReader(filename);
            final BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            result = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append('\n');
            }
        }
        catch (final FileNotFoundException e) {
            Logging.severe("Unable to find %s. Here's the traceback: %s", filename, e.getMessage());
            e.printStackTrace();
            result = new StringBuilder(String.format("ERROR: Could not load: %s", filename));
        }
        catch (final IOException e) {
            Logging.severe("Something bad happend when reading %s. Here's the traceback: %s", filename, e.getMessage());
            e.printStackTrace();
            result = new StringBuilder(String.format("ERROR: Could not load: %s", filename));
        }
        return result.toString();
    }

    private Map<String, String> getVersionFiles() {
        final Map<String, String> files = new HashMap<>();

        // Add the legacy file, but as markdown so it's readable
        files.put("version.md", getMarkdownString());

        // Add the config.yml
        final File configFile = new File(plugin.getDataFolder(), "config.yml");
        files.put(configFile.getName(), readFile(configFile.getAbsolutePath()));

        // Add the config.yml
        final File worldConfig = new File(plugin.getDataFolder(), "worlds.yml");
        files.put(worldConfig.getName(), readFile(worldConfig.getAbsolutePath()));
        return files;
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        // Check if the command was sent from a Player.
        if (sender instanceof Player) {
            sender.sendMessage("Version info dumped to console. Please check your server logs.");
        }

        final MVVersionEvent versionEvent = new MVVersionEvent(getLegacyString(), getVersionFiles());
        final Map<String, String> files = getVersionFiles();
        plugin.getServer().getPluginManager().callEvent(versionEvent);

        String versionInfo = versionEvent.getVersionInfo();

        if (CommandHandler.hasFlag("--include-plugin-list", args)) {
            versionInfo = versionInfo + "\nPlugins: " + getPluginList();
        }

        final String data = versionInfo;

        // log to console
        final String[] lines = data.split("\n");
        for (final String line : lines) {
            if (!line.isEmpty()) {
                Logging.info(line);
            }
        }

        final BukkitRunnable logPoster = new BukkitRunnable() {
            @Override
            public void run() {
                if (args.size() > 0) {
                    final String pasteUrl;
                    if (CommandHandler.hasFlag("-b", args)) {
                        // private post to pastebin
                        pasteUrl = postToService(PasteServiceType.PASTEBIN, data, files);
                    }
                    else if (CommandHandler.hasFlag("-h", args)) {
                        // private post to pastebin
                        pasteUrl = postToService(PasteServiceType.HASTEBIN, data, files);
                    } else {
                        return;
                    }

                    if (!(sender instanceof ConsoleCommandSender)) {
                        sender.sendMessage("Version info dumped here: " + ChatColor.GREEN + pasteUrl);
                    }
                    Logging.info("Version info dumped here: %s", pasteUrl);
                }
            }
        };

        // Run the log posting operation asynchronously, since we don't know how long it will take.
        logPoster.runTaskAsynchronously(plugin);
    }

    private String getPluginList() {
        return StringUtils.join(plugin.getServer().getPluginManager().getPlugins(), ", ");
    }
}
