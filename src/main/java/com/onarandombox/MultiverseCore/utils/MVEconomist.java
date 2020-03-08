package com.onarandombox.MultiverseCore.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Multiverse's Friendly Economist. This is used to deal with external economies and also item costs for stuff in MV.
 */
public class MVEconomist {

    private final VaultHandler vaultHandler;

    public MVEconomist(final Plugin plugin) {
        vaultHandler = new VaultHandler(plugin);
    }

    /**
     * Determines if the currency type string given represents an item currency.
     *
     * @param currency the type of currency.
     *
     * @return true if currency string matches a valid material.
     */
    public static boolean isItemCurrency(final Material currency) {
        return currency != null;
    }

    private boolean isUsingVault(final Material currency) {
        return !isItemCurrency(currency) && vaultHandler.hasEconomy();
    }

    /**
     * Checks if an economy plugin is in use.
     *
     * @return true if an economy plugin is detected by Vault.
     */
    public boolean isUsingEconomyPlugin() {
        return vaultHandler.hasEconomy();
    }

    /**
     * Formats the amount to a human readable currency string.
     *
     * @param amount   the amount of currency.
     * @param currency the type of currency. Null indicates a non-item currency is used.
     *
     * @return the human readable currency string.
     */
    public String formatPrice(final double amount, @Nullable final Material currency) {
        if (isUsingVault(currency)) {
            return vaultHandler.getEconomy().format(amount);
        }
        else {
            return ItemEconomy.getFormattedPrice(amount, currency);
        }
    }

    /**
     * Returns the name of the economy in use.
     *
     * @return the name of the economy in use.
     */
    public String getEconomyName() {
        if (vaultHandler.hasEconomy()) {
            return vaultHandler.getEconomy().getName();
        }
        else {
            return ItemEconomy.getName();
        }
    }

    /**
     * Determines if a player has enough of a given currency.
     *
     * @param player   the player to check for currency.
     * @param amount   the amount of currency.
     * @param currency the type of currency. Null indicates non-item currency is used.
     *
     * @return true if the player has enough of the given currency or the amount is 0 or less.
     */
    public boolean isPlayerWealthyEnough(final Player player, final double amount, final Material currency) {
        if (amount <= 0D) {
            return true;
        }
        else if (isUsingVault(currency)) {
            return vaultHandler.getEconomy().has(player, amount);
        }
        else {
            return ItemEconomy.hasEnough(player, amount, currency);
        }
    }

    /**
     * Formats a message for a player indicating they don't have enough currency.
     *
     * @param currency the type of currency. Null indicates a non-item currency is used.
     * @param message  The more specific message to append to the generic message of not having enough.
     *
     * @return the formatted insufficient funds message.
     */
    public String getNSFMessage(final Material currency, final String message) {
        return "Sorry, you don't have enough " + (isItemCurrency(currency) ? "items" : "funds") + ". " + message;
    }

    /**
     * Deposits a given amount of currency either into the player's economy account or inventory if the currency
     * is not null.
     *
     * @param player   the player to give currency to.
     * @param amount   the amount to give.
     * @param currency the type of currency.
     */
    public void deposit(final Player player, final double amount, @Nullable final Material currency) {
        if (isUsingVault(currency)) {
            vaultHandler.getEconomy().depositPlayer(player, amount);
        }
        else {
            ItemEconomy.deposit(player, amount, currency);
        }
    }

    /**
     * Withdraws a given amount of currency either from the player's economy account or inventory if the currency
     * is not null.
     *
     * @param player   the player to take currency from.
     * @param amount   the amount to take.
     * @param currency the type of currency.
     */
    public void withdraw(final Player player, final double amount, @Nullable final Material currency) {
        if (isUsingVault(currency)) {
            vaultHandler.getEconomy().withdrawPlayer(player, amount);
        }
        else {
            ItemEconomy.withdraw(player, amount, currency);
        }
    }

    /**
     * Returns the economy balance of the given player.
     *
     * @param player the player to get the balance for.
     *
     * @return the economy balance of the given player.
     * @throws IllegalStateException thrown if this is used when no economy plugin is available.
     */
    public double getBalance(final Player player) throws IllegalStateException {
        return getBalance(player, null);
    }

    /**
     * Returns the economy balance of the given player in the given world. If the economy plugin does not have world
     * specific balances then the global balance will be returned.
     *
     * @param player the player to get the balance for.
     * @param world  the world to get the balance for.
     *
     * @return the economy balance of the given player in the given world.
     * @throws IllegalStateException thrown if this is used when no economy plugin is available.
     */
    public double getBalance(final Player player, final World world) throws IllegalStateException {
        if (!isUsingEconomyPlugin()) {
            throw new IllegalStateException("getBalance is only available when using an economy plugin with Vault");
        }
        if (world != null) {
            return vaultHandler.getEconomy().getBalance(player, world.getName());
        }
        else {
            return vaultHandler.getEconomy().getBalance(player);
        }
    }

    /**
     * Sets the economy balance for the given player.
     *
     * @param player the player to set the balance for.
     * @param amount the amount to set the player's balance to.
     *
     * @throws IllegalStateException thrown if this is used when no economy plugin is available.
     */
    public void setBalance(final Player player, final double amount) throws IllegalStateException {
        setBalance(player, null, amount);
    }

    /**
     * This method is public for backwards compatibility.
     *
     * @return the old VaultHandler.
     * @deprecated just use the other methods in this class for economy stuff.
     */
    // TODO make private
    @Deprecated
    public VaultHandler getVaultHandler() {
        return vaultHandler;
    }

    /**
     * Sets the economy balance for the given player in the given world. If the economy plugin does not have world
     * specific balances then the global balance will be set.
     *
     * @param player the player to set the balance for.
     * @param world  the world to get the balance for.
     * @param amount the amount to set the player's balance to.
     *
     * @throws IllegalStateException thrown if this is used when no economy plugin is available.
     */
    public void setBalance(final Player player, final World world, final double amount) throws IllegalStateException {
        if (!isUsingEconomyPlugin()) {
            throw new IllegalStateException("getBalance is only available when using an economy plugin with Vault");
        }
        if (world != null) {
            vaultHandler.getEconomy().withdrawPlayer(player, world.getName(), getBalance(player, world));
            vaultHandler.getEconomy().depositPlayer(player, world.getName(), amount);
        }
        else {
            vaultHandler.getEconomy().withdrawPlayer(player, getBalance(player));
            vaultHandler.getEconomy().depositPlayer(player, amount);
        }
    }

    private static class ItemEconomy {

        private static final String ECONOMY_NAME = "Simple Item Economy";

        private static String getFormattedPrice(final double amount, final Material currency) {
            if (isItemCurrency(currency)) {
                return amount + " " + currency.toString();
            } else {
                return "";
            }
        }

        private static String getName() {
            return ECONOMY_NAME;
        }

        private static boolean hasEnough(final Player player, final double amount, final Material currency) {
            if (currency != null) {
                return player.getInventory().contains(currency, (int) amount);
            }
            else {
                return true;
            }
        }

        private static void deposit(final Player player, final double amount, final Material currency) {
            if (isItemCurrency(currency)) {
                giveItem(player, amount, currency);
            }
        }

        private static void withdraw(final Player player, final double amount, final Material currency) {
            if (isItemCurrency(currency)) {
                takeItem(player, amount, currency);
            }
        }

        private static void giveItem(final Player player, final double amount, final Material type) {
            final ItemStack item = new ItemStack(type, (int) amount);
            player.getInventory().addItem(item);
            showReceipt(player, (amount * -1), type);
        }

        private static void takeItem(final Player player, final double amount, final Material type) {
            int removed = 0;
            final HashMap<Integer, ItemStack> items = (HashMap<Integer, ItemStack>) player.getInventory().all(type);
            for (final int i : items.keySet()) {
                if (removed >= amount) {
                    break;
                }
                final int diff = (int) (amount - removed);
                final int amt = player.getInventory().getItem(i).getAmount();
                if (amt - diff > 0) {
                    player.getInventory().getItem(i).setAmount(amt - diff);
                    break;
                }
                else {
                    removed += amt;
                    player.getInventory().clear(i);
                }
            }
            showReceipt(player, amount, type);
        }

        private static void showReceipt(final Player player, final double price, final Material item) {
            if (price > 0D) {
                player.sendMessage(String.format("%s%s%s %s",
                                                 ChatColor.WHITE, "You have been charged", ChatColor.GREEN, getFormattedPrice(price, item)));
            }
            else if (price < 0D) {
                player.sendMessage(String.format("%s%s%s %s",
                                                 ChatColor.DARK_GREEN, getFormattedPrice((price * -1), item),
                                                 ChatColor.WHITE, "has been added to your inventory."));
            }
        }
    }
}
