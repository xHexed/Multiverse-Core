/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility-class for permissions.
 */
public class PermissionTools {
    private static final Pattern CHOPPED = Pattern.compile(".*", Pattern.LITERAL);
    private final MultiverseCore plugin;

    public PermissionTools(final MultiverseCore plugin) {
        this.plugin = plugin;
    }

    /**
     * If the given permission was 'multiverse.core.tp.self', this would return 'multiverse.core.tp.*'.
     *
     * @param separatedPermissionString The array of a dot separated perm string.
     *
     * @return The dot separated parent permission string.
     */
    private static String getParentPerm(final String[] separatedPermissionString) {
        if (separatedPermissionString.length == 1) {
            return null;
        }
        final StringBuilder returnString = new StringBuilder();
        for (int i = 0; i < separatedPermissionString.length - 1; i++) {
            returnString.append(separatedPermissionString[i]).append(".");
        }
        return returnString + "*";
    }

    /**
     * Adds a permission to the parent-permissions.
     *
     * @param permString The new permission as {@link String}.
     */
    public void addToParentPerms(final String permString) {
        final String permStringChopped = CHOPPED.matcher(permString).replaceAll(Matcher.quoteReplacement(""));

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

    /**
     * Checks if the given {@link Player} has enough money to enter the specified {@link MultiverseWorld}.
     *
     * @param fromWorld  The {@link MultiverseWorld} the player is coming from.
     * @param toWorld    The {@link MultiverseWorld} the player is going to.
     * @param teleporter The teleporter.
     * @param teleportee The teleportee.
     * @param pay        If the player has to pay the money.
     *
     * @return True if the player can enter the world.
     */
    public boolean playerHasMoneyToEnter(final MultiverseWorld fromWorld, final MultiverseWorld toWorld, CommandSender teleporter, final Player teleportee, final boolean pay) {
        final Player teleporterPlayer;
        if (plugin.getMVConfig().getTeleportIntercept()) {
            if (teleporter instanceof ConsoleCommandSender) {
                return true;
            }

            if (teleporter == null) {
                teleporter = teleportee;
            }

            if (!(teleporter instanceof Player)) {
                return false;
            }
            teleporterPlayer = (Player) teleporter;
        } else {
            if (teleporter instanceof Player) {
                teleporterPlayer = (Player) teleporter;
            } else {
                teleporterPlayer = null;
            }

            // Old-style!
            if (teleporterPlayer == null) {
                return true;
            }
        }

        // If the toWorld isn't controlled by MV,
        // We don't care.
        if (toWorld == null) {
            return true;
        }

        // Only check payments if it's a different world:
        if (!toWorld.equals(fromWorld)) {
            final double price = toWorld.getPrice();
            // Don't bother checking economy stuff if it doesn't even cost to enter.
            if (price == 0D) {
                return true;
            }
            // If the player does not have to pay, return now.
            if (plugin.getMVPerms().hasPermission(teleporter, toWorld.getExemptPermission().getName(), true)) {
                return true;
            }

            final MVEconomist economist = plugin.getEconomist();
            final Material currency = toWorld.getCurrency();
            final String formattedAmount = economist.formatPrice(price, currency);

            if (economist.isPlayerWealthyEnough(teleporterPlayer, price, currency)) {
                if (pay) {
                    if (price < 0D) {
                        economist.deposit(teleporterPlayer, -price, currency);
                    } else {
                        economist.withdraw(teleporterPlayer, price, currency);
                    }
                    sendTeleportPaymentMessage(economist, teleporterPlayer, teleportee, toWorld.getColoredWorldString(), price, currency);
                }
            } else {
                if (teleportee.equals(teleporter)) {
                    teleporterPlayer.sendMessage(economist.getNSFMessage(currency,
                            "You need " + formattedAmount + " to enter " + toWorld.getColoredWorldString()));
                } else {
                    teleporterPlayer.sendMessage(economist.getNSFMessage(currency,
                            "You need " + formattedAmount + " to send " + teleportee.getName() + " to " + toWorld.getColoredWorldString()));
                }
                return false;
            }
        }
        return true;
    }

    private void sendTeleportPaymentMessage(final MVEconomist economist, final Player teleporterPlayer, final Player teleportee, final String toWorld, double price, final Material currency) {
        price = Math.abs(price);
        if (teleporterPlayer.equals(teleportee)) {
            teleporterPlayer.sendMessage("You were " + (price > 0D ? "charged " : "given ") + economist.formatPrice(price, currency) + " for teleporting to " + toWorld);
        }
        else {
            teleporterPlayer.sendMessage("You were " + (price > 0D ? "charged " : "given ") + economist.formatPrice(price, currency) + " for teleporting " + teleportee.getName() + " to " + toWorld);
        }
    }


    /**
     * Checks to see if player can go to a world given their current status.
     * <p>
     * The return is a little backwards, and will return a value safe for event.setCancelled.
     *
     * @param fromWorld  The MultiverseWorld they are in.
     * @param toWorld    The MultiverseWorld they want to go to.
     * @param teleporter The CommandSender that wants to send someone somewhere. If null,
     *                   will be given the same value as teleportee.
     * @param teleportee The player going somewhere.
     * @return True if they can't go to the world, False if they can.
     */
    public boolean playerCanGoFromTo(final MultiverseWorld fromWorld, final MultiverseWorld toWorld, CommandSender teleporter, final Player teleportee) {
        plugin.log(Level.FINEST, "Checking '" + teleporter + "' can send '" + teleportee + "' somewhere");

        final Player teleporterPlayer;
        if (plugin.getMVConfig().getTeleportIntercept()) {
            // The console can send anyone anywhere
            if (teleporter instanceof ConsoleCommandSender) {
                return true;
            }

            // Make sure we have a teleporter of some kind, even if it's inferred to be the teleportee
            if (teleporter == null) {
                teleporter = teleportee;
            }

            // Now make sure we can cast the teleporter to a player, 'cause I'm tired of console things now
            if (!(teleporter instanceof Player)) {
                return false;
            }
            teleporterPlayer = (Player) teleporter;
        } else {
            if (teleporter instanceof Player) {
                teleporterPlayer = (Player) teleporter;
            } else {
                teleporterPlayer = null;
            }

            // Old-style!
            if (teleporterPlayer == null) {
                return true;
            }
        }

        // Actual checks
        if (toWorld != null) {
            if (!plugin.getMVPerms().canEnterWorld(teleporterPlayer, toWorld)) {
                if (teleportee.equals(teleporter)) {
                    teleporter.sendMessage("You don't have access to go here...");
                }
                else {
                    teleporter.sendMessage("You can't send " + teleportee.getName() + " here...");
                }

                return false;
            }
        } else {
            // TODO: Determine if this value is false because a world didn't exist
            // or if it was because a world wasn't imported.
            return true;
        }
        if (fromWorld != null) {
            if (fromWorld.getWorldBlacklist().contains(toWorld.getName())) {
                if (teleportee.equals(teleporter)) {
                    teleporter.sendMessage("You don't have access to go to " + toWorld.getColoredWorldString() + " from " + fromWorld.getColoredWorldString());
                } else {
                    teleporter.sendMessage("You don't have access to send " + teleportee.getName() + " from "
                         + fromWorld.getColoredWorldString() + " to " + toWorld.getColoredWorldString());
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if a player can bypass the player limit.
     *
     * @param toWorld    The world travelling to.
     * @param teleporter The player that initiated the teleport.
     * @param teleportee The player travelling.
     *
     * @return True if they can bypass the player limit.
     */
    public boolean playerCanBypassPlayerLimit(final MultiverseWorld toWorld, CommandSender teleporter, final Player teleportee) {
        if (teleporter == null) {
            teleporter = teleportee;
        }

        if (!(teleporter instanceof Player)) {
            return true;
        }

        final MVPermissions perms = plugin.getMVPerms();
        if (perms.hasPermission(teleportee, "mv.bypass.playerlimit." + toWorld.getName(), false)) {
            return true;
        } else {
            teleporter.sendMessage("The world " + toWorld.getColoredWorldString() + " is full");
            return false;
        }
    }

    /**
     * Checks to see if a player should bypass game mode restrictions.
     *
     * @param toWorld    world travelling to.
     * @param teleportee player travelling.
     *
     * @return True if they should bypass restrictions
     */
    public boolean playerCanIgnoreGameModeRestriction(final MultiverseWorld toWorld, final Player teleportee) {
        if (toWorld != null) {
            return plugin.getMVPerms().canIgnoreGameModeRestriction(teleportee, toWorld);
        }
        else {
            // TODO: Determine if this value is false because a world didn't exist
            // or if it was because a world wasn't imported.
            return true;
        }
    }
}
