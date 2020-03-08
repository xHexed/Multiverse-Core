/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.event;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired *before* the property is actually changed.
 * <p>
 * If it is cancelled, no change will happen.
 * <p>
 * If you want to get the values of the world before the change, query the world.
 * To get the name of the property that was changed, use {@link #getPropertyName()}.
 * To get the new value, use {@link #getTheNewValue()}. To change it, use {@link #setTheNewValue(Object)}.
 * @param <T> The type of the property that was set.
 */
public class MVWorldPropertyChangeEvent<T> extends Event implements Cancellable {
    private final MultiverseWorld world;
    private final CommandSender changer;
    private final String name;
    private boolean isCancelled;
    private T value;

    public MVWorldPropertyChangeEvent(final MultiverseWorld world, final CommandSender changer, final String name, final T value) {
        this.world   = world;
        this.changer = changer;
        this.name    = name;
        this.value   = value;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the handler list. This is required by the event system.
     * @return A list of handlers.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Gets the changed world property's name.
     * @return The changed world property's name.
     */
    public String getPropertyName() {
        return name;
    }

    /**
     * Gets the new value.
     * @return The new value.
     * @deprecated Use {@link #getTheNewValue()} instead.
     */
    @Deprecated
    public String getNewValue() {
        return value.toString();
    }

    /**
     * Sets the new value.
     * <p>
     * This method is only a stub, it'll <b>always</b> throw an {@link UnsupportedOperationException}!
     *
     * @param value The new new value.
     *
     * @deprecated Use {@link #setTheNewValue(Object)} instead.
     */
    @Deprecated
    public void setNewValue(final String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the new value.
     *
     * @return The new value.
     */
    public T getTheNewValue() {
        return value;
    }

    /**
     * Sets the new value.
     *
     * @param value The new value.
     */
    public void setTheNewValue(final T value) {
        this.value = value;
    }

    /**
     * Get the world targeted because of this change.
     *
     * @return A valid MultiverseWorld.
     */
    public MultiverseWorld getWorld() {
        return world;
    }

    /**
     * Gets the person (or console) who was responsible for the change.
     * <p>
     * This may be null!
     *
     * @return The person (or console) who was responsible for the change.
     */
    public CommandSender getCommandSender() {
        return changer;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        isCancelled = cancelled;
    }
}
