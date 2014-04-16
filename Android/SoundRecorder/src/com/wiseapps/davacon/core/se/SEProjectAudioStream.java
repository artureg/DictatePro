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

    private SERecord curRecord;
    double position, duration;

    byte[] data;

    SEProjectAudioStream(Context context, final SEProject project) {
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

        curRecord = records.iterator().next();
        this.position = 0;
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
     */
    @Override
    public byte[] read(double position, double duration) {
        if (records == null) {
            throw new IllegalStateException();
        }

        this.duration = duration;

        return doRead();
    }

    private byte[] doRead() {
        if (duration < curRecord.duration) {
            SEAudioStream stream = curRecord.getAudioStream(context);

            stream.open(Mode.READ);
            data = stream.read(position, duration);
            stream.close();

            position = curRecord.duration - duration;
            return data;
        } else if (duration == curRecord.duration) {
            SEAudioStream stream = curRecord.getAudioStream(context);

            stream.open(Mode.READ);
            data = stream.read(position, duration);
            stream.close();

            curRecord = records.iterator().next();
            position = 0;

            return data;
        } else {
            // duration > curRecord.duration
            double duration = this.duration - curRecord.duration;

            SEAudioStream stream = curRecord.getAudioStream(context);

            stream.open(Mode.READ);
            data = stream.read(position, duration);
            stream.close();

            curRecord = records.iterator().next();
            this.duration = duration;

            return doRead();
        }
    }

    @Override
    Mode getMode() {
        return mode;
    }
}
