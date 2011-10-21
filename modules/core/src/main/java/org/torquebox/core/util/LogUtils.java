package org.torquebox.core.util;

import org.jboss.logging.Logger;

/**
 * Utilities method for making logging (particularly conditional logging) less
 * verbose.
 * 
 * @author mdobozy
 * 
 */
public class LogUtils {

    /**
     * Issues a warning log statement on the specified logger if the condition
     * is met.
     * 
     * @param condition The condition to test.
     * @param log The logger.
     * @param message The logger.
     */
    public static void warnIf(boolean condition, Logger log, String message) {
        if (condition) {
            log.warn( message );
        }
    }

    /**
     * Issues a formatted warning log statement on the specified logger if the condition
     * is met.
     * 
     * @param condition The condition to test.
     * @param log The logger.
     * @param message The logger.
     * @param options The options for the formatted log statement.
     */
    public static void warnIf(boolean condition, Logger log, String message, Object... options) {
        if (condition) {
            log.warnf( message, options );
        }
    }

}
