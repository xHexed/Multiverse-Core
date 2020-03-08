/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.Teleporter;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.CustomTeleporterDestination;
import com.onarandombox.MultiverseCore.destination.DestinationFactory;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.destination.WorldDestination;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Used to teleport players.
 */
public class TeleportCommand extends MultiverseCommand {
    private static final Pattern CANNON = Pattern.compile("(?i)cannon-[\\d]+(\\.[\\d]+)?");
    private final SafeTTeleporter playerTeleporter;

    public TeleportCommand(final MultiverseCore plugin) {
        super(plugin);
        final Permission menu = new Permission("multiverse.teleport.*", "Allows you to display the teleport menu.", PermissionDefault.OP);

        setName("Teleport");
        setCommandUsage("/mv tp " + ChatColor.GOLD + "[PLAYER]" + ChatColor.GREEN + " {WORLD}");
        setArgRange(1, 2);
        addKey("mvtp");
        addKey("mv tp");
        playerTeleporter = this.plugin.getSafeTTeleporter();
        setPermission(menu);
    }

    private static final int UNSAFE_TELEPORT_EXPIRE_DELAY = 15;

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        final Player teleportee;

        String destinationName;

        if (args.size() == 2) {
            teleportee = plugin.getServer().getPlayer(args.get(0));
            if (teleportee == null) {
                messaging.sendMessage(sender, String.format("Sorry, I couldn't find player: %s%s",
                                                            ChatColor.GOLD, args.get(0)), false);
                return;
            }
            destinationName = args.get(1);

        }
        else {
            destinationName = args.get(0);
            if (!(sender instanceof Player)) {
                messaging.sendMessage(sender, "From the console, you must specify a player to teleport", false);
                return;
            }
            teleportee = (Player) sender;
        }
        // Special case for cannons:
        if (CANNON.matcher(destinationName).matches()) {
            final String[] cannonSpeed = destinationName.split("-");
            try {
                final double speed = Double.parseDouble(cannonSpeed[1]);
                destinationName = "ca:" + teleportee.getWorld().getName() + ":" + teleportee.getLocation().getX()
                        + "," + teleportee.getLocation().getY() + "," + teleportee.getLocation().getZ() + ":"
                        + teleportee.getLocation().getPitch() + ":" + teleportee.getLocation().getYaw() + ":" + speed;
            }
            catch (final Exception e) {
                destinationName = "i:invalid";
            }

        }
        final DestinationFactory df = plugin.getDestFactory();
        final MVDestination d = df.getDestination(destinationName);


        final MVTeleportEvent teleportEvent = new MVTeleportEvent(d, teleportee, sender, true);
        plugin.getServer().getPluginManager().callEvent(teleportEvent);
        if (teleportEvent.isCancelled()) {
            plugin.log(Level.FINE, "Someone else cancelled the MVTeleport Event!!!");
            return;
        }

        if (d instanceof InvalidDestination) {
            messaging.sendMessage(sender, String.format("Multiverse does not know how to take you to %s%s",
                                                        ChatColor.RED, destinationName), false);
            return;
        }

        if (!checkSendPermissions(sender, teleportee, d)) {
            return;
        }

        if (plugin.getMVConfig().getEnforceAccess() && !plugin.getMVPerms().canEnterDestination(sender, d)) {
            if (teleportee.equals(sender)) {
                sender.sendMessage("Doesn't look like you're allowed to go " + ChatColor.RED + "there...");
            }
            else {
                sender.sendMessage("Doesn't look like you're allowed to send " + ChatColor.GOLD
                                           + teleportee.getName() + ChatColor.WHITE + " to " + ChatColor.RED + "there...");
            }
            return;
        }
        else {
            assert d != null;
            if (!plugin.getMVPerms().canTravelFromLocation(sender, d.getLocation(teleportee))) {
                if (teleportee.equals(sender)) {
                    messaging.sendMessage(sender, String.format("DOH! Doesn't look like you can get to %s%s %sfrom where you are...",
                                                                ChatColor.GREEN, d.toString(), ChatColor.WHITE), false);
                }
                else {
                    messaging.sendMessage(sender, String.format("DOH! Doesn't look like %s%s %scan get to %sTHERE from where they are...",
                                                                ChatColor.GREEN, ((Player) sender).getWorld().getName(), ChatColor.WHITE, ChatColor.RED), false);
                }
                return;
            }
        }

        // Special check to verify if players are tryint to teleport to the same
        // WORLDDestination as the world they're in, that they ALSO have multiverse.core.spawn.self

        if (d instanceof WorldDestination) {
            final World w = d.getLocation(teleportee).getWorld();
            if (teleportee.getWorld().equals(w)) {
                if (sender.equals(teleportee)) {
                    if (!plugin.getMVPerms().hasPermission(sender, "multiverse.core.spawn.self", true)) {
                        messaging.sendMessages(sender, new String[] {
                                "Sorry you don't have permission to go to the world spawn!",
                                String.format("%s  (multiverse.core.spawn.self)",
                                              ChatColor.RED)}, false);
                        return;
                    }
                }
                else {
                    if (!plugin.getMVPerms().hasPermission(sender, "multiverse.core.spawn.other", true)) {
                        messaging.sendMessages(sender, new String[] {
                                String.format("Sorry you don't have permission to send %s to the world spawn!",
                                              teleportee.getDisplayName()),
                                String.format("%s  (multiverse.core.spawn.other)",
                                              ChatColor.RED)}, false);
                        return;
                    }
                }
            }
        }

        if (d.getLocation(teleportee) == null) {
            messaging.sendMessage(sender, "Sorry Boss, I tried everything, but just couldn't teleport ya there!", false);
            return;
        }
        final Teleporter teleportObject = (d instanceof CustomTeleporterDestination) ?
                ((CustomTeleporterDestination) d).getTeleporter() : playerTeleporter;
        final TeleportResult result = teleportObject.teleport(sender, teleportee, d);
        if (result == TeleportResult.FAIL_UNSAFE) {
            plugin.log(Level.FINE, "Could not teleport " + teleportee.getName()
                    + " to " + plugin.getLocationManipulation().strCoordsRaw(d.getLocation(teleportee)));
            plugin.log(Level.FINE, "Queueing Command");
            final Class<?>[] paramTypes = {CommandSender.class, Player.class, Location.class};
            final List<Object> items = new ArrayList<>();
            items.add(sender);
            items.add(teleportee);
            items.add(d.getLocation(teleportee));
            String player = "you";
            if (!teleportee.equals(sender)) {
                player = teleportee.getName();
            }
            final String message = String.format("%sMultiverse %sdid not teleport %s%s %sto %s%s %sbecause it was unsafe.",
                                                 ChatColor.GREEN, ChatColor.WHITE, ChatColor.AQUA, player, ChatColor.WHITE, ChatColor.DARK_AQUA, d.getName(), ChatColor.WHITE);
            plugin.getCommandHandler().queueCommand(sender, "mvteleport", "teleportPlayer", items,
                                                    paramTypes, message, "Would you like to try anyway?", "", "", UNSAFE_TELEPORT_EXPIRE_DELAY);
        }
        // else: Player was teleported successfully (or the tp event was fired I should say)
    }

    private boolean checkSendPermissions(final CommandSender teleporter, final Player teleportee, final MVDestination destination) {
        if (teleporter.equals(teleportee)) {
            if (!plugin.getMVPerms().hasPermission(teleporter, "multiverse.teleport.self." + destination.getIdentifier(), true)) {
                messaging.sendMessages(teleporter, new String[] {
                        String.format("%sYou don't have permission to teleport %syourself %sto a %s%s %sDestination",
                                      ChatColor.WHITE, ChatColor.AQUA, ChatColor.WHITE, ChatColor.RED, destination.getType(), ChatColor.WHITE),
                        String.format("%s   (multiverse.teleport.self.%s)",
                                      ChatColor.RED, destination.getIdentifier())}, false);
                return false;
            }
        }
        else {
            if (!plugin.getMVPerms().hasPermission(teleporter, "multiverse.teleport.other." + destination.getIdentifier(), true)) {
                messaging.sendMessages(teleporter, new String[] {
                        String.format("You don't have permission to teleport another player to a %s%s Destination.",
                                      ChatColor.GREEN, destination.getType()),
                        String.format("%s(multiverse.teleport.other.%s)",
                                      ChatColor.RED, destination.getIdentifier())}, false);
                return false;
            }
        }
        return true;
    }
}
