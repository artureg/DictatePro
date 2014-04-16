package com.wiseapps.davacon.core.se;

import android.content.Context;

import java.util.List;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:56 AM
 */
class SEProjectAudioStream extends SEAudioStream {

    private final SEProject project;
    private List<SERecord> records;

    SEProjectAudioStream(final SEProject project, Context context) {
        super(context);

        this.project = project;
    }

    SEProjectAudioStream initialize(List<SERecord> records) {
        this.records = records;
        return this;
    }

    @Override
    public void open(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void close() {
    }

    @Override
    public void clear() {
        this.records.clear();
    }

    @Override
    public void write(byte[] data) {
        // SEProjectAudioStream class is used for reading purposes only
        // This method is intentionally left blanc.
    }

    /**
     *
     * @param position position to start reading from
     * @param duration duration of the data to be read
     * @return read data
     *
     * TODO START FROM HERE!!!
     *
     * TODO to correctly implement do not cycle through the records every time
     * TODO but keep a reference to the current record and indices
     * TODO to the previous and the next (as object fields) records in the list
     */
    @Override
    public byte[] read(double position, double duration) {
        if (records == null) {
            throw new IllegalStateException();
        }

        SEAudioStream stream;
        for (SERecord record : records) {
            stream = record.getAudioStream(project, context);
            stream.open(Mode.READ);

            if (true /* TODO check the condition for the record to be what we expect */) {
                return stream.read(position, duration);
            }
        }

        return null;
    }

    @Override
    Mode getMode() {
        return mode;
    }
}
