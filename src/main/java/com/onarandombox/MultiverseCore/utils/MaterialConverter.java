package com.onarandombox.MultiverseCore.utils;

import de.themoep.idconverter.IdMappings;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A tool for converting values which may be an old type ID to a Material.
 */
public class MaterialConverter {

    /**
     * Converts the value in the given config at the given path from a numeric id or flattened material name to a
     * Material.
     *
     * @param config The config with the value to convert.
     * @param path   The path of the value in the config.
     *
     * @return The converted Material type or null if no matching type.
     */
    @Nullable
    public static Material convertConfigType(@NotNull final ConfigurationSection config, @NotNull final String path) {
        return convertTypeString(config.getString(path));
    }

    /**
     * Converts a string representing a numeric id or flattened material name to a Material.
     *
     * @param value The value to convert.
     *
     * @return The converted Material type or null if no matching type.
     */
    @Nullable
    public static Material convertTypeString(@Nullable final String value) {
        final IdMappings.Mapping mapping = IdMappings.getById(value != null ? value : "");
        if (mapping != null) {
            return Material.matchMaterial(mapping.getFlatteningType());
        }
        else {
            return Material.matchMaterial(value != null ? value : "");
        }
    }
}
