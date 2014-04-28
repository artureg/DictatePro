package com.wiseapps.davacon.core.mock;

import android.content.Context;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 5:24 PM
 */
public abstract class MockAudioStream {

    final Context context;

    public Mode mode;

    public static enum Mode {
        READ,
        WRITE
    }

    protected MockAudioStream(Context context) {
        this.context = context;
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
     * Writes the data to the end of the stream
     *
     * @param data data to be appended
     */
    abstract void write(byte[] data);

    /**
     * Reads data from the stream
     *
     * @param position position to start reading from
     * @param duration duration of the data to be read
     * @return read data
     */
    abstract byte[] read(double position, double duration);

    abstract Mode getMode();
}
