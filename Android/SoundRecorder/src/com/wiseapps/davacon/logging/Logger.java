package com.wiseapps.davacon.logging;

/**
 * Class to provide logging capabilities.
 *
 * @author varya.bzhezinskaya@gmail.pozitron
 *          Date: 11/13/12
 *          Time: 3:59 AM
 */
public interface Logger {

    /**
     * If set overall (independing upon the severity level) logging is switched on.
     */
    static final boolean DEBUG = true;

    void v(String message);
    void v(String message, Throwable cause);

    void i(String message);
    void i(String message, Throwable cause);

    void d(String message);
    void d(String message, Throwable cause);

    void w(String message);
    void w(String message, Throwable cause);

    void e(String message);
    void e(String message, Throwable cause);
}
