package com.wiseapps.davacon.core.mock;

import android.content.Context;
import com.wiseapps.davacon.utils.ByteArrayUtils;

import java.util.Arrays;

import static com.wiseapps.davacon.core.mock.MockProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 5:08 PM
 */
public class MockRecordAudioStream extends MockAudioStream {
    private static final String TAG = MockRecordAudioStream.class.getSimpleName();

    private final MockRecord record;

    MockRecordAudioStream(final MockRecord record, Context context) {
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

        MockSDCardUtils.writeProject(record.project);
    }

    @Override
    public void clear() {
    }

    @Override
    public void write(byte[] data) {
        record.mockData = ByteArrayUtils.combine(record.mockData, data);

        // update record's and project's durations
        double duration =
                calculateDurationFromDataLength(data.length);

        record.position += duration;
        record.duration += duration;

        record.project.duration += duration;
    }

    @Override
    public byte[] read(double position, double duration) {

//    int bOffset = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*offset;
//    int bDuration = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*duration;

        int bytesPerSample = 16;

        int start = (int) (8000 * bytesPerSample * position);
        int end = start + (int) (8000 * bytesPerSample * duration);

        // Arrays.copyOfRange (byte[] original, int start, int end)

        return Arrays.copyOfRange(record.mockData, start, end);
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

    private int calculateDataLengthFromDuration(double duration) {
        double sampleRate = (double) SAMPLE_RATE_IN_HZ;
        double numChannels = mode == Mode.WRITE ?
                (double) CHANNEL_CONFIG_IN : (double) CHANNEL_CONFIG_OUT;
        double bitsPerSample = (double) BITS_PER_SAMPLE;

        return (int) (duration * (sampleRate * numChannels * bitsPerSample / 8));
    }
}
