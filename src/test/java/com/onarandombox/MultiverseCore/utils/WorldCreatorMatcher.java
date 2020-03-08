/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import org.bukkit.WorldCreator;
import org.mockito.ArgumentMatcher;

import java.util.Objects;

public class WorldCreatorMatcher implements ArgumentMatcher<WorldCreator> {
    private final WorldCreator worldCreator;
    private boolean careAboutSeeds;
    private boolean careAboutGenerators;

    public WorldCreatorMatcher(final WorldCreator creator) {
        Util.log("Creating NEW world matcher.(" + creator.name() + ")");
        worldCreator = creator;
    }

    public void careAboutSeeds(final boolean doICare) {
        careAboutSeeds = doICare;
    }

    public void careAboutGenerators(final boolean doICare) {
        careAboutGenerators = doICare;
    }

    public boolean matches(final WorldCreator creator) {
        Util.log("Checking world creators.");
        if (creator == null) {
            Util.log("The given creator was null, but I was checking: " + worldCreator.name());
            return false;
        }
        Util.log("Checking Names...(" + creator.name() + ") vs (" + worldCreator.name() + ")");
        Util.log("Checking Envs...(" + creator.environment() + ") vs (" + worldCreator.environment() + ")");
        if (!creator.name().equals(worldCreator.name())) {
            return false;
        }
        else if (!creator.environment().equals(worldCreator.environment())) {
            Util.log("Checking Environments...");
            return false;
        }
        else if (careAboutSeeds && creator.seed() != worldCreator.seed()) {
            Util.log("Checking Seeds...");
            return false;
        }
        else if (careAboutGenerators && !Objects.equals(creator.generator(), worldCreator.generator())) {
            Util.log("Checking Gens...");
            return false;
        }
        Util.log("Creators matched!!!");
        return true;
    }
}
