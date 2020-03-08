/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Util {
    private Util() {}

    public static final Logger logger = Logger.getLogger("MV-Test");

    static {
        logger.setUseParentHandlers(false);

        final Handler handler = new ConsoleHandler();
        handler.setFormatter(new MVTestLogFormatter());
        final Handler[] handlers = logger.getHandlers();

        for (final Handler h : handlers)
            logger.removeHandler(h);

        logger.addHandler(handler);
    }

    public static void log(final Throwable t) {
        log(Level.WARNING, t.getLocalizedMessage(), t);
    }

    public static void log(final Level level, final Throwable t) {
        log(level, t.getLocalizedMessage(), t);
    }

    public static void log(final String message, final Throwable t) {
        log(Level.WARNING, message, t);
    }

    public static void log(final Level level, final String message, final Throwable t) {
        final LogRecord record = new LogRecord(level, message);
        record.setThrown(t);
        logger.log(record);
    }

    public static void log(final String message) {
        log(Level.INFO, message);
    }

    public static void log(final Level level, final String message) {
        logger.log(level, message);
    }
}
