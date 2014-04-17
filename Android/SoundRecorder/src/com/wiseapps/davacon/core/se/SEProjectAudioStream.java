package com.wiseapps.davacon.core.se;

import android.content.Context;

import java.util.List;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

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

    byte[] data;

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

        data = doRead(duration);
        project.setPosition(project.getPosition() + duration);

        return data;

//        // update project current position
//        project.setPosition(project.getPosition() + duration);
//
//        // read data
//        // TODO handle byte array correctly - for now it's always rewritten, use Arrays
//        data = new byte[calculateDataLengthFromDuration(duration)];
//        return doRead(duration);
    }

    private byte[] doRead(double duration) {
        // check whether the end of project has been reached
        if (currentRecord == null) {
            return data;
        }

        if (duration == (currentRecord.duration - currentRecord.position)) {
            // read the data
            SEAudioStream stream = currentRecord.getAudioStream(context);
            stream.open(Mode.READ);
            data = stream.read(currentRecord.position, duration);
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

        else if (duration < (currentRecord.duration - currentRecord.position)) {
            // read the data
            SEAudioStream stream = currentRecord.getAudioStream(context);
            stream.open(Mode.READ);
            data = stream.read(currentRecord.position, duration);
            stream.close();

            // here current record remains unchanged
            // but the position to read from the next time should be updated
            currentRecord.position = currentRecord.position + duration;

            return data;
        }

        else { // duration > (currentRecord.duration - currentRecord.position)
            // read the data
            SEAudioStream stream = currentRecord.getAudioStream(context);
            stream.open(Mode.READ);
            data = stream.read(currentRecord.position, (currentRecord.duration - currentRecord.position));
            stream.close();

            // reset current record's position before moving to next record
            currentRecord.position = 0;

            // calculate the rest of duration to be read
            double d = duration - (currentRecord.duration - currentRecord.position);

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

//        if (duration == currentRecord.duration) {
//            SEAudioStream stream = currentRecord.getAudioStream(context);
//
//            stream.open(Mode.READ);
//            data = stream.read(currentRecord.position, currentRecord.duration);
//            stream.close();
//
//            currentRecord = currentRecord.nextRecord;
//            if (currentRecord != null) { // handle the last record case
//                currentRecord.position = 0;
//            }
//
//            return data;
//        }
//
//        else if (duration < currentRecord.duration) {
//            SEAudioStream stream = currentRecord.getAudioStream(context);
//
//            stream.open(Mode.READ);
//            data = stream.read(currentRecord.position, duration);
//            stream.close();
//
//            // here current record remains unchanged
//            // but the position to read from the next time should be updated
//            currentRecord.position = currentRecord.duration - duration;
//
//            return data;
//        }
//
//        else  {     // duration > currentRecord.duration
//            SEAudioStream stream = currentRecord.getAudioStream(context);
//
//            stream.open(Mode.READ);
//            data = stream.read(currentRecord.position, currentRecord.duration);
//            stream.close();
//
//            currentRecord = currentRecord.nextRecord;
//            if (currentRecord != null) { // handle the last record case
//                currentRecord.position = 0;
//            }
//
//            if (currentRecord != null) {
//                return doRead(duration - currentRecord.duration);
//            } else {
//                return data;
//            }
//        }
    }

    @Override
    Mode getMode() {
        return mode;
    }

    private int calculateDataLengthFromDuration(double duration) {
        int sampleRate = SAMPLE_RATE_IN_HZ;
        int numChannels = mode == Mode.WRITE ?
                CHANNEL_CONFIG_IN : CHANNEL_CONFIG_OUT;
        int bitsPerSample =  BITS_PER_SAMPLE;

        return (int) (duration / (sampleRate * numChannels * bitsPerSample / 8));
    }
}
