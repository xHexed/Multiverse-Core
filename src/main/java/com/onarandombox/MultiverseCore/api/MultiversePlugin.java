package com.onarandombox.MultiverseCore.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.utils.DebugLog;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Make things easier for MV-Plugins!
 */
public abstract class MultiversePlugin extends JavaPlugin implements MVPlugin {
    private MultiverseCore core;
    /**
     * Prefix for standard log entrys.
     */
    protected String logTag;
    private DebugLog debugLog;

    /**
     * {@inheritDoc}
     *
     * Note: You can't override this, use {@link #onPluginEnable()} instead!
     * @see #onPluginEnable()
     */
    @Override
    public final void onEnable() {
        final MultiverseCore theCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (theCore == null) {
            getLogger().severe("Core not found! The plugin dev needs to add a dependency!");
            getLogger().severe("Disabling!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (theCore.getProtocolVersion() < getProtocolVersion()) {
            getLogger().severe("You need a newer version of Multiverse-Core!");
            getLogger().severe("Disabling!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        core = theCore;

        getServer().getLogger().info(String.format("%s - Version %s enabled - By %s",
                                                   getDescription().getName(), getDescription().getVersion(), getAuthors()));
        getDataFolder().mkdirs();
        final File debugLogFile = new File(getDataFolder(), "debug.log");
        try {
            debugLogFile.createNewFile();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        debugLog = new DebugLog(getDescription().getName(), getDataFolder() + File.separator + "debug.log");
        debugLog.setTag(String.format("[%s-Debug]", getDescription().getName()));

        onPluginEnable();
    }

    /**
     * Parse the Authors Array into a readable String with ',' and 'and'.
     *
     * @return The readable authors-{@link String}
     */
    protected String getAuthors() {
        final StringBuilder authors = new StringBuilder();
        final List<String> auths = getDescription().getAuthors();
        if (auths.size() == 0) {
            return "";
        }

        if (auths.size() == 1) {
            return auths.get(0);
        }

        for (int i = 0; i < auths.size(); i++) {
            if (i == getDescription().getAuthors().size() - 1) {
                authors.append(" and ").append(getDescription().getAuthors().get(i));
            }
            else {
                authors.append(", ").append(getDescription().getAuthors().get(i));
            }
        }
        return authors.substring(2);
    }

    /**
     * Called when the plugin is enabled.
     * @see #onEnable()
     */
    protected abstract void onPluginEnable();

    /**
     * You can register commands here.
     * @param handler The CommandHandler.
     */
    protected abstract void registerCommands(CommandHandler handler);

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (!isEnabled()) {
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }

        final ArrayList<String> allArgs = new ArrayList<>(args.length + 1);
        allArgs.add(command.getName());
        allArgs.addAll(Arrays.asList(args));
        return getCore().getCommandHandler().locateAndRunCommand(sender, allArgs);
    }

    @Override
    public void log(final Level level, final String msg) {
        final int debugLevel = getCore().getMVConfig().getGlobalDebug();
        if ((level == Level.FINE && debugLevel >= 1) || (level == Level.FINER && debugLevel >= 2)
                || (level == Level.FINEST && debugLevel >= 3)) {
            debugLog.log(level, msg);
        }
        else if (level != Level.FINE && level != Level.FINER && level != Level.FINEST) {
            final String message = getLogTag() + msg;
            getServer().getLogger().log(level, message);
            debugLog.log(level, message);
        }
    }

    private String getLogTag() {
        if (logTag == null)
            logTag = String.format("[%s]", getDescription().getName());
        return logTag;
    }

    /**
     * Sets the debug log-tag.
     *
     * @param tag The new tag.
     */
    protected final void setDebugLogTag(final String tag) {
        debugLog.setTag(tag);
    }

    @Override
    public final String dumpVersionInfo(final String buffer) {
        throw new UnsupportedOperationException("This is gone.");
    }

    @Override
    public final MultiverseCore getCore() {
        if (core == null)
            throw new IllegalStateException("Core is null!");
        return core;
    }

    @Override
    public final void setCore(final MultiverseCore core) {
        this.core = core;
    }
}
