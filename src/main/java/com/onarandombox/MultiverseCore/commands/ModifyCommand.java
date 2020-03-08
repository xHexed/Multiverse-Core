/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.enums.Action;
import com.onarandombox.MultiverseCore.enums.AddProperties;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to modify various aspects of worlds.
 */
public class ModifyCommand extends MultiverseCommand {

    public ModifyCommand(final MultiverseCore plugin) {
        super(plugin);
        setName("Modify a World");
        setCommandUsage("/mv modify" + ChatColor.GREEN + " {set|add|remove|clear} ...");
        setArgRange(2, 3);
        addKey("mvm");
        addKey("mvmodify");
        addKey("mv modify");
        final Map<String, Boolean> children = new HashMap<>();
        children.put("multiverse.core.modify.add", true);
        children.put("multiverse.core.modify.modify", true);
        children.put("multiverse.core.modify.clear", true);
        children.put("multiverse.core.modify.remove", true);
        final Permission modify = new Permission("multiverse.core.modify",
                                                 "Modify various aspects of worlds. It requires add/set/clear/remove. See the examples below", PermissionDefault.OP, children);
        addCommandExample(ChatColor.AQUA + "/mv modify set ?");
        addCommandExample(ChatColor.GREEN + "/mv modify add ?");
        addCommandExample(ChatColor.BLUE + "/mv modify clear ?");
        addCommandExample(ChatColor.RED + "/mv modify remove ?");
        setPermission(modify);
    }

    /**
     * Validates the specified action.
     *
     * @param action   The {@link Action}.
     * @param property The property.
     *
     * @return Whether this action is valid.
     */
    protected static boolean validateAction(final Action action, final String property) {
        if (action != Action.Set) {
            try {
                AddProperties.valueOf(property);
                return true;
            }
            catch (final IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        // This is just a place holder. The real commands are in:
        // ModifyAddCommand
        // ModifyRemoveCommand
        // ModifySetCommand
        // ModifyClearCommand
    }
}
