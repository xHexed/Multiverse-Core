/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.api.MultiverseMessaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The default-implementation of {@link MultiverseMessaging}.
 */
public class MVMessaging implements MultiverseMessaging {
    private final Map<String, Long> sentList;
    private int cooldown;

    public MVMessaging() {
        sentList = new HashMap<>();
        cooldown = 5000; // SUPPRESS CHECKSTYLE: MagicNumberCheck
    }

    private static void sendMessages(final CommandSender sender, final String[] messages) {
        for (final String s : messages) {
            sender.sendMessage(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCooldown(final int milliseconds) {
        cooldown = milliseconds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendMessage(final CommandSender sender, final String message, final boolean ignoreCooldown) {
        return sendMessages(sender, new String[] {message}, ignoreCooldown);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendMessages(final CommandSender sender, final String[] messages, final boolean ignoreCooldown) {
        if (!(sender instanceof Player) || ignoreCooldown) {

            sendMessages(sender, messages);
            return true;
        }
        if (!sentList.containsKey(sender.getName())) {
            sendMessages(sender, messages);
            sentList.put(sender.getName(), System.currentTimeMillis());
            return true;
        }
        else {
            final long time = System.currentTimeMillis();
            if (time >= sentList.get(sender.getName()) + cooldown) {
                sendMessages(sender, messages);
                sentList.put(sender.getName(), System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendMessages(final CommandSender sender, final Collection<String> messages, final boolean ignoreCooldown) {
        return sendMessages(sender, messages.toArray(new String[0]), ignoreCooldown);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCooldown() {
        return cooldown;
    }
}
