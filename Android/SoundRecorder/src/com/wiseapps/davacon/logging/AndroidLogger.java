package com.wiseapps.davacon.logging;

import android.util.Log;

/**
 * Class to provide Android based logging.
 *
 * <p>Makes extensive use of {@link android.util.Log} class.</p>
 *
 * @author varya.bzhezinskaya@gmail.pozitron
 *          Date: 11/13/12
 *          Time: 4:05 AM
 */
public class AndroidLogger implements Logger {
    private static final String DEV_PREFIX = "DEV_";

    private final String tag;

    public AndroidLogger(String tag) {
        this.tag = DEV_PREFIX + tag;
    }

    @Override
    public void v(String message) {
        if (isLoggingEnabled()) {
            Log.v(tag, message);
        }
    }

    @Override
    public void v(String message, Throwable cause) {
        if (isLoggingEnabled()) {
            Log.v(tag, message, cause);
        }
    }

    @Override
    public void i(String message) {
        if (isLoggingEnabled()) {
            Log.i(tag, message);
        }
    }

    @Override
    public void i(String message, Throwable cause) {
        if (isLoggingEnabled()) {
            Log.i(tag, message, cause);
        }
    }

    @Override
    public void d(String message) {
        if (isLoggingEnabled()) {
            Log.d(tag, message);
        }
    }

    @Override
    public void d(String message, Throwable cause) {
        if (isLoggingEnabled()) {
            Log.d(tag, message, cause);
        }
    }

    @Override
    public void w(String message) {
        if (isLoggingEnabled()) {
            Log.w(tag, message);
        }
    }

    @Override
    public void w(String message, Throwable cause) {
        if (isLoggingEnabled()) {
            Log.w(tag, message, cause);
        }
    }

    @Override
    public void e(String message) {
        if (isLoggingEnabled()) {
            Log.e(tag, message);
        }
    }

    @Override
    public void e(String message, Throwable cause) {
        if (isLoggingEnabled()) {
            Log.e(tag, message, cause);
        }
    }

    private boolean isLoggingEnabled() {
        return true;
    }
}
