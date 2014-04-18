package com.wiseapps.davacon.core.se;

import android.content.Context;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.SpeexWrapper;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:56 AM
 */
class SERecordAudioStream extends SEAudioStream {
    private static final String TAG = SERecordAudioStream.class.getSimpleName();

    private final SERecord record;

    SERecordAudioStream(final SERecord record, Context context) {
        super(context);

        this.record = record;
    }

    @Override
    public void open(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void close() {
        if (mode == Mode.READ) {
            return;
        }

        SDCardUtils.writeProject(record.project);
    }

    @Override
    public void clear() {
    }

    @Override
    public void write(byte[] data) {
//        int format = SpeexWrapper.getFormat(record.soundPath);
        int format = 0; // .wav, 41225 for now speex

        int result = SpeexWrapper.write(record.soundPath, data, format);
        LoggerFactory.obtainLogger(TAG).d("write# result = " + result);

        // update record's and project's durations
        if (result == 0) {
            double duration =
                    calculateDurationFromDataLength(data.length);

            record.position += duration;
            record.duration += duration;

            record.project.duration += duration;
        }
    }

    @Override
    public byte[] read(double position, double duration) {
//        int format = SpeexWrapper.getFormat(record.soundPath);
        int format = 0; // .wav, 41225 for now speex

        return SpeexWrapper.read(record.soundPath, position, duration, format);
    }

    @Override
    Mode getMode() {
        return mode;
    }

    private double calculateDurationFromDataLength(int length) {
        double sampleRate = (double) SAMPLE_RATE_IN_HZ;
        double numChannels = mode == Mode.WRITE ?
                (double) CHANNEL_CONFIG_IN : (double) CHANNEL_CONFIG_OUT;
        double bitsPerSample = (double) BITS_PER_SAMPLE;

        return ((double) length) / (sampleRate * numChannels * bitsPerSample / 8);
    }
}
