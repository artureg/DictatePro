package com.wiseapps.davacon.core.se;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 9:19 PM
 */
public abstract class AudioStream {

    public Mode mode;

    public static enum Mode {
        READ,
        WRITE
    }

    protected AudioStream() {
    }

    /**
     * Opens the stream
     */
    abstract void open(Mode mode);

    /**
     * Closes the stream
     */
    abstract void close();

    /**
     * Clears the stream
     */
    abstract void clear();

    /**
     * Read
     */
    abstract InputStream getInputStream() throws Exception;

    /**
     * Write
     */
    abstract OutputStream getOutputStream() throws Exception;

    abstract void updatePosition(long position);

    abstract void updateDuration(long duration);

    abstract void finalizePosition();

    abstract void finalizeDuration();

    abstract Mode getMode();
}
