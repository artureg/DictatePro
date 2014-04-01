package com.wiseapps.davacon.logging;

/**
 * Helper class to build the logging hierarchy.
 *
 * @author varya.bzhezinskaya@gmail.pozitron
 *          Date: 11/13/12
 *          Time: 4:10 AM
 */
public class LoggerFactory {

    public static Logger obtainLogger(final String tag) {
        return new MultipleLoggersSupport(new AndroidLogger(tag));
    }
}
