package com.wiseapps.davacon.core;

import android.media.AudioFormat;

import java.io.File;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/25/14
 *         Time: 4:46 PM
 */
public class WAVFileReader {
    private static final String TAG = WAVFileReader.class.getSimpleName();

    public final WAVFile wav;

    private int totalNumberOfBytes;

    private boolean consumed;

    public WAVFileReader(File file) {
        this.wav = new WAVFile.Builder(file).
                setEncoding(AudioFormat.ENCODING_PCM_16BIT).
                setChannelType(AudioFormat.CHANNEL_IN_STEREO).
                setSampleRate(44100).
                build();
    }

    private void read(byte[] data) {
        if (consumed) {
            throw new IllegalStateException("WAV file writer already consumed!");
        }
    }

    public void consume() {
        // TODO implement
        consumed = true;
    }
}
