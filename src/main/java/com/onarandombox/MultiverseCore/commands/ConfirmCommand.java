/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * Confirms actions.
 */
public class ConfirmCommand extends MultiverseCommand {

    public ConfirmCommand(final MultiverseCore plugin) {
        super(plugin);
        // Any command that is dangerous should require op
        setName("Confirms a command that could destroy life, the universe and everything.");
        setCommandUsage("/mv confirm");
        setArgRange(0, 0);
        addKey("mvconfirm");
        addKey("mv confirm");
        addCommandExample("/mv confirm");
        setPermission("multiverse.core.confirm", "If you have not been prompted to use this, it will not do anything.", PermissionDefault.OP);

    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        plugin.getCommandHandler().confirmQueuedCommand(sender);
    }

}
