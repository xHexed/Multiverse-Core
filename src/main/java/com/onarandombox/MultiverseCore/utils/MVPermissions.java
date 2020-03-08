/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.pneumaticraft.commandhandler.PermissionsInterface;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Multiverse's {@link PermissionsInterface}.
 */
public class MVPermissions implements PermissionsInterface {

    private static final Pattern CHOP = Pattern.compile(".*", Pattern.LITERAL);
    private final MultiverseCore plugin;
    private final MVWorldManager worldMgr;

    public MVPermissions(final MultiverseCore plugin) {
        this.plugin = plugin;
        worldMgr    = plugin.getMVWorldManager();

    }

    /**
     * Pulls one level off of a yaml style node.
     * Given multiverse.core.list.worlds will return multiverse.core.list
     *
     * @param node The root node to check.
     *
     * @return The parent of the node
     */
    private static String pullOneLevelOff(final String node) {
        if (node == null) {
            return null;
        }
        final int index = node.lastIndexOf('.');
        if (index > 0) {
            return node.substring(0, index);
        }
        return null;
    }

    /**
     * If the given permission was 'multiverse.core.tp.self', this would return 'multiverse.core.tp.*'.
     */
    private static String getParentPerm(final String[] seperated) {
        if (seperated.length == 1) {
            return null;
        }
        final StringBuilder returnString = new StringBuilder();
        for (int i = 0; i < seperated.length - 1; i++) {
            returnString.append(seperated[i]).append(".");
        }
        return returnString + "*";
    }

    /**
     * Check if a Player can ignore GameMode restrictions for world they travel to.
     *
     * @param p The {@link Player} to check.
     * @param w The {@link MultiverseWorld} the player wants to teleport to.
     *
     * @return True if they should bypass restrictions.
     */
    public boolean canIgnoreGameModeRestriction(final Player p, final MultiverseWorld w) {
        return p.hasPermission("mv.bypass.gamemode." + w.getName());
    }

    /**
     * Check if a Player can teleport to the Destination world from there current world.
     *
     * @param p The {@link Player} to check.
     * @param w The {@link MultiverseWorld} the player wants to teleport to.
     *
     * @return Whether the player can teleport to the given {@link MultiverseWorld}.
     */
    public boolean canTravelFromWorld(final Player p, final MultiverseWorld w) {
        final List<String> blackList = w.getWorldBlacklist();

        boolean returnValue = true;

        for (final String s : blackList) {
            if (s.equalsIgnoreCase(p.getWorld().getName())) {
                returnValue = false;
                break;
            }
        }

        return returnValue;
    }

    /**
     * Checks if the specified {@link CommandSender} can travel to the specified {@link Location}.
     *
     * @param sender   The {@link CommandSender}.
     * @param location The {@link Location}.
     *
     * @return Whether the {@link CommandSender} can travel to the specified {@link Location}.
     */
    public boolean canTravelFromLocation(final CommandSender sender, final Location location) {
        // Now The Bed destination can return null now.
        if (location == null) {
            return false;
        }
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player teleporter = (Player) sender;
        if (!worldMgr.isMVWorld(location.getWorld().getName())) {
            return false;
        }
        return canTravelFromWorld(teleporter, worldMgr.getMVWorld(location.getWorld().getName()));
    }

    /**
     * Check if the Player has the permissions to enter this world.
     *
     * @param p The {@link Player} player that wants to enter
     * @param w The {@link MultiverseWorld} he wants to enter
     *
     * @return Whether he has the permission to enter the world
     */
    public boolean canEnterWorld(final Player p, final MultiverseWorld w) {
        // If we're not enforcing access, anyone can enter.
        if (!plugin.getMVConfig().getEnforceAccess()) {
            plugin.log(Level.FINEST, "EnforceAccess is OFF. Player was allowed in " + w.getAlias());
            return true;
        }
        return hasPermission(p, "multiverse.access." + w.getName(), false);
    }

    private boolean canEnterLocation(final Player p, final Location l) {
        if (l == null) {
            return false;
        }
        final String worldName = l.getWorld().getName();
        if (!plugin.getMVWorldManager().isMVWorld(worldName)) {
            return false;
        }
        return hasPermission(p, "multiverse.access." + worldName, false);
    }

    /**
     * Check to see if a sender can enter a destination.
     * The reason this is not a player, is it can be used to simply check permissions
     * The console should, for exmaple, always see all worlds
     *
     * @param sender The CommandSender to check.
     * @param d      The destination they are requesting.
     *
     * @return True if that sender can go to that destination
     */
    public boolean canEnterDestination(final CommandSender sender, final MVDestination d) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player p = (Player) sender;
        if (d == null || d.getLocation(p) == null) {
            return false;
        }
        final String worldName = d.getLocation(p).getWorld().getName();
        if (!worldMgr.isMVWorld(worldName)) {
            return false;
        }
        if (!canEnterLocation(p, d.getLocation(p))) {
            return false;
        }
        return hasPermission(p, d.getRequiredPermission(), false);
    }

    /**
     * Tells a {@link CommandSender} why another {@link CommandSender} can or can not access a certain {@link MVDestination}.
     *
     * @param asker            The {@link CommandSender} that's asking.
     * @param playerInQuestion The {@link CommandSender} whose permissions we want to know.
     * @param d                The {@link MVDestination}.
     */
    public void tellMeWhyICantDoThis(final CommandSender asker, final CommandSender playerInQuestion, final MVDestination d) {
        boolean cango = true;
        if (!(playerInQuestion instanceof Player)) {
            asker.sendMessage(String.format("The console can do %severything%s.", ChatColor.RED, ChatColor.WHITE));
            return;
        }
        final Player p = (Player) playerInQuestion;
        if (d == null) {
            asker.sendMessage(String.format("The provided Destination is %sNULL%s, and therefore %sINVALID%s.",
                                            ChatColor.RED, ChatColor.WHITE, ChatColor.RED, ChatColor.WHITE));
            cango = false;
        }
        // We know it'll be a player here due to the first line of this method.
        assert d != null;
        if (d.getLocation(p) == null) {
            asker.sendMessage(String.format(
                    "The player will spawn at an %sindeterminate location%s. Talk to the MV Devs if you see this",
                    ChatColor.RED, ChatColor.WHITE));
            cango = false;
        }
        final String worldName = d.getLocation(p).getWorld().getName();
        if (!worldMgr.isMVWorld(worldName)) {
            asker.sendMessage(String.format("The destination resides in a world(%s%s%s) that is not managed by Multiverse.",
                                            ChatColor.AQUA, worldName, ChatColor.WHITE));
            asker.sendMessage(String.format("Type %s/mv import ?%s to see the import command's help page.",
                                            ChatColor.DARK_AQUA, ChatColor.WHITE));
            cango = false;
        }
        if (!hasPermission(p, "multiverse.access." + worldName, false)) {
            asker.sendMessage(String.format("The player (%s%s%s) does not have the required world entry permission (%s%s%s) to go to the destination (%s%s%s).",
                                            ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE,
                                            ChatColor.GREEN, "multiverse.access." + worldName, ChatColor.WHITE,
                                            ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            cango = false;
        }
        if (!hasPermission(p, d.getRequiredPermission(), false)) {
            asker.sendMessage(String.format("The player (%s%s%s) does not have the required entry permission (%s%s%s) to go to the destination (%s%s%s).",
                                            ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE,
                                            ChatColor.GREEN, d.getRequiredPermission(), ChatColor.WHITE,
                                            ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            cango = false;
        }
        if (cango) {
            asker.sendMessage(String.format("The player (%s%s%s) CAN go to the destination (%s%s%s).",
                                            ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE,
                                            ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
        }
        else {
            asker.sendMessage(String.format("The player (%s%s%s) cannot access the destination %s%s%s. Therefore they can't use mvtp at all for this.",
                                            ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE,
                                            ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
            return;
        }
        if (!hasPermission(p, "multiverse.teleport.self." + d.getIdentifier(), false)) {
            asker.sendMessage(String.format("The player (%s%s%s) does not have the required teleport permission (%s%s%s) to use %s/mvtp %s%s.",
                                            ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE,
                                            ChatColor.GREEN, "multiverse.teleport.self." + d.getIdentifier(), ChatColor.WHITE,
                                            ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
        }
        else {
            asker.sendMessage(String.format("The player (%s%s%s) has the required teleport permission (%s%s%s) to use %s/mvtp %s%s.",
                                            ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE,
                                            ChatColor.GREEN, "multiverse.teleport.self." + d.getIdentifier(), ChatColor.WHITE,
                                            ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
        }
        if (!hasPermission(p, "multiverse.teleport.other." + d.getIdentifier(), false)) {
            asker.sendMessage(String.format("The player (%s%s%s) does not have the required teleport permission (%s%s%s) to send others to %s%s%s via mvtp.",
                                            ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE,
                                            ChatColor.GREEN, "multiverse.teleport.other." + d.getIdentifier(), ChatColor.WHITE,
                                            ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
        }
        else {
            asker.sendMessage(String.format("The player (%s%s%s) has required teleport permission (%s%s%s) to send others to %s%s%s via mvtp.",
                                            ChatColor.AQUA, p.getDisplayName(), ChatColor.WHITE,
                                            ChatColor.GREEN, "multiverse.teleport.other." + d.getIdentifier(), ChatColor.WHITE,
                                            ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE));
        }
    }

    /**
     * Check to see if a player has a permission.
     *
     * @param sender       Who is requesting the permission.
     * @param node         The permission node in string format; multiverse.core.list.worlds for example.
     * @param isOpRequired deprecated This is not used for anything anymore.
     *
     * @return True if they have that permission or any parent.
     */
    @Override
    public boolean hasPermission(final CommandSender sender, final String node, final boolean isOpRequired) {
        if (!(sender instanceof Player)) {
            return true;
        }
        // NO one can access a null permission (mainly used for destinations):w
        if (node == null) {
            return false;
        }
        // Everyone can access an empty permission
        // Currently used for the PlayerDestination
        if (node.isEmpty()) {
            return true;
        }

        return checkActualPermission(sender, node);
    }

    // TODO: Better player checks, most likely not needed, but safer.
    private boolean checkActualPermission(final CommandSender sender, final String node) {
        final Player player = (Player) sender;

        final boolean hasPermission = sender.hasPermission(node);
        if (!sender.isPermissionSet(node)) {
            plugin.log(Level.FINER, String.format("The node [%s%s%s] was %sNOT%s set for [%s%s%s].",
                                                  ChatColor.RED, node, ChatColor.WHITE, ChatColor.RED, ChatColor.WHITE, ChatColor.AQUA,
                                                  player.getDisplayName(), ChatColor.WHITE));
        }
        if (hasPermission) {
            plugin.log(Level.FINER, "Checking to see if player [" + player.getName() + "] has permission [" + node + "]... YES");
        }
        else {
            plugin.log(Level.FINER, "Checking to see if player [" + player.getName() + "] has permission [" + node + "]... NO");
        }
        return hasPermission;
    }

    /**
     * Gets the type of this {@link PermissionsInterface}.
     *
     * @return The type of this {@link PermissionsInterface}.
     */
    public String getType() {
        return "Bukkit Permissions (SuperPerms)";
    }

    /**
     * Checks to see if the sender has any parent perms.
     * Stops when it finds one or when there are no more parents.
     * This method is recursive.
     *
     * @param sender Who is asking for the permission.
     * @param node   The permission node to check (possibly already a parent).
     *
     * @return True if they have any parent perm, false if none.
     */
    // TODO remove this...?
    private boolean hasAnyParentPermission(final CommandSender sender, final String node) {
        final String parentPerm = pullOneLevelOff(node);
        // Base case
        if (parentPerm == null) {
            return false;
        }
        // If they have a parent, they're good
        if (checkActualPermission(sender, parentPerm + ".*")) {
            return true;
        }
        return hasAnyParentPermission(sender, parentPerm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAnyPermission(final CommandSender sender, final List<String> nodes, final boolean isOpRequired) {
        for (final String node : nodes) {
            if (hasPermission(sender, node, isOpRequired)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAllPermission(final CommandSender sender, final List<String> nodes, final boolean isOpRequired) {
        for (final String node : nodes) {
            if (!hasPermission(sender, node, isOpRequired)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a permission.
     *
     * @param string       The permission as {@link String}.
     * @param defaultValue The default-value.
     *
     * @return The permission as {@link Permission}.
     */
    public Permission addPermission(final String string, final PermissionDefault defaultValue) {
        if (plugin.getServer().getPluginManager().getPermission(string) == null) {
            final Permission permission = new Permission(string, defaultValue);
            plugin.getServer().getPluginManager().addPermission(permission);
            addToParentPerms(string);
        }
        return plugin.getServer().getPluginManager().getPermission(string);
    }

    private void addToParentPerms(final String permString) {
        final String permStringChopped = CHOP.matcher(permString).replaceAll(Matcher.quoteReplacement(""));

        final String[] seperated = permStringChopped.split("\\.");
        final String parentPermString = getParentPerm(seperated);
        if (parentPermString == null) {
            addToRootPermission("*", permStringChopped);
            addToRootPermission("*.*", permStringChopped);
            return;
        }
        Permission parentPermission = plugin.getServer().getPluginManager().getPermission(parentPermString);
        // Creat parent and grandparents
        if (parentPermission == null) {
            parentPermission = new Permission(parentPermString);
            plugin.getServer().getPluginManager().addPermission(parentPermission);

            addToParentPerms(parentPermString);
        }
        // Create actual perm.
        Permission actualPermission = plugin.getServer().getPluginManager().getPermission(permString);
        // Extra check just to make sure the actual one is added
        if (actualPermission == null) {

            actualPermission = new Permission(permString);
            plugin.getServer().getPluginManager().addPermission(actualPermission);
        }
        if (!parentPermission.getChildren().containsKey(permString)) {
            parentPermission.getChildren().put(actualPermission.getName(), true);
            plugin.getServer().getPluginManager().recalculatePermissionDefaults(parentPermission);
        }
    }

    private void addToRootPermission(final String rootPerm, final String permStringChopped) {
        Permission rootPermission = plugin.getServer().getPluginManager().getPermission(rootPerm);
        if (rootPermission == null) {
            rootPermission = new Permission(rootPerm);
            plugin.getServer().getPluginManager().addPermission(rootPermission);
        }
        rootPermission.getChildren().put(permStringChopped + ".*", true);
        plugin.getServer().getPluginManager().recalculatePermissionDefaults(rootPermission);
    }
}
