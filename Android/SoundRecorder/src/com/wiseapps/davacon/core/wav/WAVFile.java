package com.wiseapps.davacon.core.wav;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Model of a .wav file.
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/26/14
 *         Time: 11:29 AM
 */
public class WAVFile extends SoundFile {
    private static final String TAG = WAVFile.class.getSimpleName();

    public static final int RECORDER_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int RECORDER_SAMPLE_RATE_IN_HZ = 8000;
    public static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int RECORDER_BUFFER_SIZE_IN_BYTES =
            AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE_IN_HZ, RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT);

    private static final String DEFAULT_CHUNK_ID = "RIFF";
    private static final int DEFAULT_CHUNK_SIZE = 0;
    private static final String DEFAULT_FORMAT = "WAVE";

    private static final String DEFAULT_SUBCHUNK_1_ID = "fmt ";
    private static final int DEFAULT_SUBCHUNK_1_SIZE = 16;
    private static final short DEFAULT_AUDIO_FORMAT = 1;
    private static final short DEFAULT_NUM_CHANNELS = 1;    // AudioFormat.CHANNEL_IN_MONO
    private static final int DEFAULT_SAMPLE_RATE = 8000;
    private static final int DEFAULT_BYTE_RATE = 8000;
    private static final short DEFAULT_BLOCK_ALIGN = 1;
    private static final short DEFAULT_BITS_PER_SAMPLE = 16;    // Required bits = 8

    private static final String DEFAULT_SUBCHUNK_2_ID = "data";
    private static final int DEFAULT_SUBCHUNK_2_SIZE = 0;

    private final WAVFileWriter writer;
    private final WAVFileReader reader;

    private String chunkID;
    private int chunkSize;
    private String format;

    private String subchunk1ID;
    private int subchunk1Size;
    private short audioFormat;
    private short numChannels;
    private int sampleRate;
    private int byteRate;
    private short blockAlign;
    private short bitsPerSample;

    private String subchunk2ID;
    private int subchunk2Size;
    private transient byte[] data;

    private WAVFile(File file, int audioFormat, int channelConfig, int sampleRateInHz) throws IOException {
        if (audioFormat != AudioFormat.ENCODING_PCM_16BIT) {
            throw new IllegalArgumentException("Incorrect audio format, working with PCM 16 bit only!");
        }

        setChunkID(DEFAULT_CHUNK_ID);
        setChunkSize(DEFAULT_CHUNK_SIZE);
        setFormat(DEFAULT_FORMAT);

        setSubchunk1ID(DEFAULT_SUBCHUNK_1_ID);
        setSubchunk1Size(DEFAULT_SUBCHUNK_1_SIZE);
        setAudioFormat(DEFAULT_AUDIO_FORMAT);
        setNumChannels((short) (channelConfig == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1));
        setSampleRate(sampleRateInHz);
        setByteRate();
        setBlockAlign();
        setBitsPerSample(DEFAULT_BITS_PER_SAMPLE);

        setSubchunk2ID(DEFAULT_SUBCHUNK_2_ID);
        setSubchunk2Size(DEFAULT_SUBCHUNK_2_SIZE);

        setFile(file);

        writer = new WAVFileWriter(this);
        reader = new WAVFileReader(this);
    }

    /**
     * Method to read .wav file.
     */
    public void read() {
        reader.read();
    }

    /**
     * Method to write binary content to .wav file.
     *
     * @param data array of bytes to write to file
     * @throws IOException
     */
    public void write(byte[] data) throws IOException {
        writer.write(data);
    }

    /**
     * Method to consume a .wav file.
     */
    public void consume() throws IOException {
        writer.consume();
    }

    /**
     * Method to return duration.
     *
     * @return duration in millis
     */
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
        return this.chunkID;
    }
    void setChunkID(String chunkID) {
        this.chunkID = chunkID;
    }

    int getChunkSize() {
        return chunkSize;
    }
    void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    String getFormat() {
        return this.format;
    }
    void setFormat(String format) {
        this.format = format;
    }

    String getSubchunk1ID() {
        return this.subchunk1ID;
    }
    void setSubchunk1ID(String subchunk1ID) {
        this.subchunk1ID = subchunk1ID;
    }

    int getSubchunk1Size() {
        return this.subchunk1Size;
    }
    void setSubchunk1Size(int subchunk1Size) {
        this.subchunk1Size = subchunk1Size;
    }

    short getAudioFormat() {
        return this.audioFormat;
    }
    void setAudioFormat(short audioFormat) {
        this.audioFormat = audioFormat;
    }

    short getNumChannels() {
        return this.numChannels;
    }
    void setNumChannels(short numChannels) {
        this.numChannels = numChannels;
    }

    int getSampleRate() {
        return this.sampleRate;
    }
    void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    int getByteRate() {
        return this.byteRate;
    }
    void setByteRate(int byteRate) {
        this.byteRate = byteRate;
    }
    private void setByteRate() {
        if (sampleRate == 0 || numChannels == 0 || bitsPerSample == 0) {
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("Actual byte rate can't be set before either " +
                            "SampleRate(%d) or NumChannels(%d) or BitsPerSample(%d) are set!",
                            sampleRate, numChannels, bitsPerSample));
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("Setting defaults (%s).", DEFAULT_BYTE_RATE));
            this.byteRate = DEFAULT_BYTE_RATE;

            return;
        }

        this.byteRate = sampleRate * numChannels * bitsPerSample / 8;
    }

    short getBlockAlign() {
        return this.blockAlign;
    }
    void setBlockAlign(short blockAlign) {
        this.blockAlign = blockAlign;
    }
    private void setBlockAlign() {
        if (numChannels == 0 || bitsPerSample == 0) {
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("Block align can't be set before either NumChannels(%d) or BitsPerSample(%d) are set!",
                            numChannels, bitsPerSample));
            this.blockAlign = DEFAULT_BLOCK_ALIGN;

            return;
        }

        this.blockAlign = (short) (numChannels * bitsPerSample / 8);
    }

    short getBitsPerSample() {
        return this.bitsPerSample;
    }
    void setBitsPerSample(short bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    String getSubchunk2ID() {
        return this.subchunk2ID;
    }
    void setSubchunk2ID(String subchunk2ID) {
        this.subchunk2ID = subchunk2ID;
    }

    int getSubchunk2Size() {
        return subchunk2Size;
    }
    public void setSubchunk2Size(int subchunk2Size) {
        this.subchunk2Size = subchunk2Size;
    }

    void setData(byte[] data) {
        this.data = data;
    }
    public byte[] getData() {   // TODO hide
        return data;
    }

    /**
     * Checks format of the {@link com.wiseapps.davacon.core.SoundFile SoundFile}.
     *
     * @return true if sound file is of correct format
     */
    public boolean isFormatCorrect() {
        // TODO implement
        return true;
    }

    /**
     * Returns arrays of bytes of both splitted parts.
     *
     * @param wav .wav file which binary content to split
     * @param durationPlayed duration of the first part in millis
     * @return arrays of bytes as of both splitted parts
     */
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
