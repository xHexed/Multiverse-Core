/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2012.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.enums;

import org.bukkit.PortalType;

/**
 * Custom enum that adds all/none for allowing portal creation.
 */
public enum AllowedPortalType {
    /**
     * No portals are allowed.
     */
    NONE(PortalType.CUSTOM),
    /**
     * All portal types are allowed.
     */
    ALL(PortalType.CUSTOM),
    /**
     * Only Nether style portals are allowed.
     */
    NETHER(PortalType.NETHER),
    /**
     * Only Ender style portals are allowed.
     */
    END(PortalType.ENDER);

    private final PortalType type;

    AllowedPortalType(final PortalType type) {
        this.type = type;
    }

    /**
     * Gets the text.
     *
     * @return The text.
     */
    public PortalType getActualPortalType() {
        return type;
    }

    public boolean isPortalAllowed(final PortalType portalType) {
        return this != NONE && (type == portalType || this == ALL);
    }
}
