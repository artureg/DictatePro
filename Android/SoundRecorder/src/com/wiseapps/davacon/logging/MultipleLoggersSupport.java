package com.wiseapps.davacon.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to build the logging hierarchy.
 *
 * @author varya.bzhezinskaya@gmail.pozitron
 *          Date: 11/13/12
 *          Time: 4:11 AM
 */
public class MultipleLoggersSupport implements Logger {

    private final List<Logger> subscribedLoggers = new ArrayList<Logger>();

    public MultipleLoggersSupport(Logger... loggers) {
        if (loggers != null && loggers.length > 0) {
            subscribedLoggers.addAll(Arrays.asList(loggers));
        }
    }

    @Override
    public void v(String message) {
        for(Logger logger : subscribedLoggers) {
            logger.v(message);
        }
    }

    @Override
    public void v(String message, Throwable cause) {
        for(Logger logger : subscribedLoggers) {
            logger.v(message, cause);
        }
    }

    @Override
    public void i(String message) {
        for(Logger logger : subscribedLoggers) {
            logger.i(message);
        }
    }

    @Override
    public void i(String message, Throwable cause) {
        for(Logger logger : subscribedLoggers) {
            logger.i(message, cause);
        }
    }

    @Override
    public void d(String message) {
        for(Logger logger : subscribedLoggers) {
            logger.d(message);
        }
    }

    @Override
    public void d(String message, Throwable cause) {
        for(Logger logger : subscribedLoggers) {
            logger.d(message, cause);
        }
    }

    @Override
    public void w(String message) {
        for(Logger logger : subscribedLoggers) {
            logger.w(message);
        }
    }

    @Override
    public void w(String message, Throwable cause) {
        for(Logger logger : subscribedLoggers) {
            logger.w(message, cause);
        }
    }

    @Override
    public void e(String message) {
        for(Logger logger : subscribedLoggers) {
            logger.e(message);
        }
    }

    @Override
    public void e(String message, Throwable cause) {
        for(Logger logger : subscribedLoggers) {
            logger.e(message, cause);
        }
    }
}
