package com.wiseapps.davacon.core.se;

import android.content.Context;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:56 AM
 *
 *
 */
class SEProjectAudioStream extends SEAudioStream {

    private final SEProject project;

    private SERecord currentRecord;
    private byte[] data;

    SEProjectAudioStream(Context context, final SEProject project) {
        super(context);

        this.project = project;
    }

    @Override
    public void open(Mode mode) {
        if (project.getRecords() == null) {
            throw new IllegalStateException();
        }

        this.mode = mode;

        // according to project current position define current record here
        currentRecord = project.getCurrentRecord();
    }

    @Override
    public void close() {
        if (project.getRecords() == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void clear() {
        if (project.getRecords() == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void write(byte[] data) {
        // SEProjectAudioStream class is used for reading purposes only
        // This method is intentionally left blanc.
    }

    /**
     *
     * @param position position to start reading from (not used)
     * @param duration duration of the data to be read
     * @return read data
     */
    @Override
    public byte[] read(double position, double duration) {
        if (project.getRecords() == null) {
            throw new IllegalStateException();
        }

        this.data = new byte[0];

        byte[] data = doRead(duration);
        project.position += data.length;

        return data;
    }

    private byte[] doRead(double duration) {
        // check whether the end of project has been reached
        if (currentRecord == null) {
            return data;
        }

        if (duration == (currentRecord.duration - (currentRecord.start + currentRecord.position))) {
            // read the data
            SEAudioStream stream = currentRecord.getAudioStream(context);
            stream.open(Mode.READ);
            data = combine(data, stream.read(currentRecord.start + currentRecord.position, duration));
            stream.close();

            // reset current record's position before moving to next record
            currentRecord.position = 0;

            // move to next record
            currentRecord = currentRecord.nextRecord;

            // check for null in case stream end has been reached;
            // if not yet, update the next record's position to 0
            if (currentRecord != null) {
                currentRecord.position = 0;
            }

            return data;
        }

        else if (duration < (currentRecord.duration - (currentRecord.start + currentRecord.position))) {
            // read the data
            SEAudioStream stream = currentRecord.getAudioStream(context);
            stream.open(Mode.READ);
            data = combine(data, stream.read(currentRecord.start + currentRecord.position, duration));
            stream.close();

            // here current record remains unchanged
            // but the position to read from the next time should be updated
            currentRecord.position += duration;

            return data;
        }

        else { // duration > (currentRecord.duration - (currentRecord.start + currentRecord.position))
            // read the data
            SEAudioStream stream = currentRecord.getAudioStream(context);
            stream.open(Mode.READ);
            data = combine(data, stream.read(currentRecord.start + currentRecord.position,
                    (currentRecord.duration - (currentRecord.start + currentRecord.position))));
            stream.close();

            // calculate the rest of duration to be read
            double d = duration - (currentRecord.duration - (currentRecord.start + currentRecord.position));

            // reset current record's position before moving to next record
            currentRecord.position = 0;

            // move to next record
            currentRecord = currentRecord.nextRecord;

            // check for null in case stream end has been reached;
            // if not yet, update the next record's position to 0
            // call doRead method again with the rest of duration
            if (currentRecord != null) {
                currentRecord.position = 0;
                return doRead(d);
            } else {
                return data;
            }
        }
    }

    private byte[] combine(byte[] one, byte[] two) {
        byte[] combined = new byte[one.length + two.length];

        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);

        return combined;
    }

    @Override
    Mode getMode() {
        return mode;
    }

//    private int calculateDataLengthFromDuration(double duration) {
//        int sampleRate = SAMPLE_RATE_IN_HZ;
//        int numChannels = mode == Mode.WRITE ?
//                CHANNEL_CONFIG_IN : CHANNEL_CONFIG_OUT;
//        int bitsPerSample =  BITS_PER_SAMPLE;
//
//        return (int) (duration / (sampleRate * numChannels * bitsPerSample / 8));
//    }
}
