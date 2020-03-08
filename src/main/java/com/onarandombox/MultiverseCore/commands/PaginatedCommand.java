/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.pneumaticraft.commandhandler.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * A generic paginated command.
 * @param <T> The type of items on the page.
 */
public abstract class PaginatedCommand<T> extends Command {
    private static final int DEFAULT_ITEMS_PER_PAGE = 9;
    /**
     * The number of items per page.
     */
    protected int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;

    public PaginatedCommand(final JavaPlugin plugin) {
        super(plugin);
    }

    /**
     * Set the number of items per page.
     *
     * @param items The new number of items per page.
     */
    protected void setItemsPerPage(final int items) {
        itemsPerPage = items;
    }

    /**
     * Gets filtered items.
     * @param availableItems All available items.
     * @param filter The filter-{@link String}.
     * @return A list of items that match the filter.
     */
    protected abstract List<T> getFilteredItems(List<T> availableItems, String filter);

    /**
     * Constructs a single string from a list of strings.
     *
     * @param list The {@link List} of strings.
     *
     * @return A single {@link String}.
     */
    protected String stitchThisString(final List<String> list) {
        final StringBuilder builder = new StringBuilder();
        for (final String s : list) {
            builder.append(s);
            builder.append(' ');
        }
        return builder.toString();
    }

    /**
     * Shows a page.
     *
     * @param page   The number of the page to show.
     * @param sender The {@link CommandSender} that wants to see the page.
     * @param cmds   The items that should be displayed on the page.
     */
    protected void showPage(int page, final CommandSender sender, final List<T> cmds) {
        // Ensure the page is at least 1.
        page = (page <= 0) ? 1 : page;
        final int start = (page - 1) * itemsPerPage;
        final int end = start + itemsPerPage;

        for (int i = start; i < end; i++) {
            // For consistancy, print some extra lines if it's a player:
            if (i < cmds.size()) {
                sender.sendMessage(getItemText(cmds.get(i)));
            }
            else if (sender instanceof Player) {
                sender.sendMessage(" ");
            }
        }
    }

    /**
     * Converts an item into a string.
     *
     * @param item The item.
     * @return A {@link String}.
     */
    protected abstract String getItemText(T item);

    /**
     * Constructs a {@link FilterObject} from a {@link List} of arguments.
     *
     * @param args The {@link List} of arguments.
     *
     * @return The {@link FilterObject}.
     */
    protected FilterObject getPageAndFilter(final List<String> args) {
        int page = 1;

        String filter = "";

        if (args.size() == 0) {
            filter = "";
            page   = 1;
        }
        else if (args.size() == 1) {
            try {
                page = Integer.parseInt(args.get(0));
            }
            catch (final NumberFormatException ex) {
                filter = args.get(0);
                page   = 1;
            }
        } else if (args.size() == 2) {
            filter = args.get(0);
            try {
                page = Integer.parseInt(args.get(1));
            }
            catch (final NumberFormatException ex) {
                page = 1;
            }
        }
        return new FilterObject(page, filter);
    }

    /**
     * "Key-Object" containing information about the page and the filter that were requested.
     */
    protected static class FilterObject {
        private Integer page;
        private final String filter;

        public FilterObject(final Integer page, final String filter) {
            this.page   = page;
            this.filter = filter;
        }

        /**
         * Gets the page.
         * @return The page.
         */
        public Integer getPage() {
            return page;
        }

        /**
         * Sets the page.
         *
         * @param page The new page.
         */
        public void setPage(final int page) {
            this.page = page;
        }

        /**
         * Gets the filter.
         * @return The filter.
         */
        public String getFilter() {
            return filter;
        }
    }
}
