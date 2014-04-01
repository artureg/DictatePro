package com.wiseapps.davacon.core.wav;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/26/14
 *         Time: 11:29 AM
 *
 * File is cheap because we don't parse the file explicitly but assume the required format.
 * The .wav file as described here - https://ccrma.stanford.edu/courses/422/projects/WaveFormat
 */
public class WAVFile extends SoundFile {
    private static final String TAG = WAVFile.class.getSimpleName();

    public static final int RECORDER_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int RECORDER_SAMPLE_RATE_IN_HZ = 44100;
    public static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int RECORDER_BUFFER_SIZE_IN_BYTES =
            AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE_IN_HZ, RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT);

    private final WAVFileWriter writer;
    private final WAVFileReader reader;

    private short numChannels;

    // TODO set 16
    private int sampleRate;

    // 8 bits = 8, 16 bits = 16, etc.
    // TODO we should set it somehow probably, TODO try with 8
    private final short mBitsPerSample = 16;

    private int subchunk2Size;

    private transient byte[] data;

    private WAVFile(File file, int audioFormat, int channelConfig, int sampleRateInHz) throws IOException {
        if (audioFormat != AudioFormat.ENCODING_PCM_16BIT) {
            throw new IllegalArgumentException("Incorrect audio format, working with PCM 16 bit only!");
        }

        numChannels = (short) (channelConfig == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1);
        sampleRate = sampleRateInHz;

        writer = new WAVFileWriter(this);
        reader = new WAVFileReader(this);

        setFile(file);
    }

    public void read() {
        reader.read();
    }

    public void write(byte[] data) throws IOException {
        writer.write(data);
    }

    public void consume() throws IOException {
        writer.consume();
    }

    @Override
    public int getDuration() {
        // http://social.msdn.microsoft.com/Forums/windows/en-US/5a92be69-3b4e-4d92-b1d2-141ef0a50c91/how-to-calculate-duration-of-wave-file-from-its-size?forum=winforms
        double length = (double) getFile().length();
        double sampleRate = (double) this.sampleRate;
        double numChannels = (double) this.numChannels;
        double bitsPerSample = (double) getBitsPerSample();

        double duration = (double) Math.round((length / (sampleRate * numChannels * bitsPerSample /8)) * 1000) / 1000 * 1000;
        LoggerFactory.obtainLogger(TAG).
                d(String.format("getDuration# calculated duration is %s", duration));

        return (int) duration;
    }

    String getChunkID() {
        return "RIFF";
    }

    String getFormat() {
        return "WAVE";
    }

    String getSubchunk1ID() {
        return "fmt ";
    }

    int getSubchunk1Size() {
        // 16 for PCM.  This is the size of the rest of the Subchunk which follows this number.
        return 16;
    }

    short getAudioFormat() {
        // PCM = 1 (i.e. Linear quantization) Values other than 1 indicate some form of compression.
        return 1;
    }

    short getNumChannels() {
        return numChannels;
    }

    int getSampleRate() {
        return sampleRate;
    }

    int getByteRate() {
        if (sampleRate == 0 || numChannels == 0 || mBitsPerSample == 0) {
            throw new IllegalStateException(
                    String.format("ByteRate can't be set before either SampleRate(%d) or NumChannels(%d) or BitsPerSample(%d) are set!",
                            sampleRate, numChannels, mBitsPerSample));
        }

        return sampleRate * numChannels * mBitsPerSample / 8;
    }

    short getBlockAlign() {
        if (numChannels == 0 || mBitsPerSample == 0) {
            throw new IllegalStateException(
                    String.format("ByteRate can't be set before either NumChannels(%d) or BitsPerSample(%d) are set!",
                            numChannels, mBitsPerSample));
        }

        return (short) (numChannels * mBitsPerSample / 8);
    }

    short getBitsPerSample() {
        return mBitsPerSample;
    }

    String getSubchunk2ID() {
        return "data";
    }

    int getSubchunk2Size() {
        return subchunk2Size;
    }

    void setData(byte[] data) {
        this.data = data;
    }
    public byte[] getData() {   // TODO hide
        return data;
    }

    @Override
    public List<byte[]> getDataParts(SoundFile wav, int durationPlayed) {
        double durationTotal = wav.getDuration();

        double percent = durationTotal / 100;
        double alreadyPlayed = (double) Math.round(durationPlayed / percent * 100) / 100;

        wav.read();

        LoggerFactory.obtainLogger(TAG).
                d(String.format("split# data duration = %d", wav.getData().length));

        int d1 = (int) (wav.getData().length / 100 * alreadyPlayed);
        LoggerFactory.obtainLogger(TAG).
                d(String.format("split# d1 = %d", d1));

        int d2 = wav.getData().length - d1;
        LoggerFactory.obtainLogger(TAG).
                d(String.format("split# d2 = %d", d2));

        LoggerFactory.obtainLogger(TAG).
                d(String.format("split# (d1 + d2) = %d", (d1 + d2)));

        List<byte[]> parts = new ArrayList<byte[]>(2);

        parts.add(0, Arrays.copyOfRange(wav.getData(), 0, d1));
        parts.add(1, Arrays.copyOfRange(wav.getData(), d1, wav.getData().length - 1));

        return parts;
    }

    public static Factory getFactory() {
        return new Factory() {
            public SoundFile create(File file) throws IOException {
                return new WAVFile(file,
                        RECORDER_AUDIO_FORMAT, RECORDER_CHANNEL_CONFIG, RECORDER_SAMPLE_RATE_IN_HZ);
            }
            public String[] getSupportedExtensions() {
                return new String[] {"wav"};
            }
        };
    }
}
