package com.wiseapps.davacon.core.se;

import android.content.Context;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.SpeexWrapper;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:56 AM
 *
 * TODO correct start handling, for now it is always 0
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

        record.project.isChanged = true;
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

        if (result == 0) {
            updateDuration(data);
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

    private void updateDuration(byte[] data) {
        double length = (double) data.length;
        double sampleRate = (double) SAMPLE_RATE_IN_HZ;

        double numChannels = mode == Mode.WRITE ?
                (double) CHANNEL_CONFIG_IN : (double) CHANNEL_CONFIG_OUT;

        double bitsPerSample = (double) BITS_PER_SAMPLE;

        record.duration += length / (sampleRate * numChannels * bitsPerSample /8);
    }
}
