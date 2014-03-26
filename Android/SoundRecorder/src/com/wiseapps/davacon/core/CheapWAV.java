package com.wiseapps.davacon.core;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.FileUtils;

import java.io.*;
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
public class CheapWAV implements Serializable {
    private static final String TAG = CheapWAV.class.getSimpleName();

    static final long serialVersionUID = 5469586988095235790L;

    public static final int RECORDER_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int RECORDER_SAMPLE_RATE_IN_HZ = 44100;
    public static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int RECORDER_BUFFER_SIZE_IN_BYTES =
            AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE_IN_HZ, RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT);

    // The canonical WAVE format starts with the RIFF header
    private final String mChunkID = "RIFF";
    private int mChunkSize;
    private final String mFormat = "WAVE";

    // The "fmt " subchunk describes the sound data's format
    private final String mSubchunk1ID = "fmt ";
    private final int mSubchunk1Size = 16; // 16 for PCM.  This is the size of the rest of the Subchunk which follows this number.
    private final short mAudioFormat = 1; // PCM = 1 (i.e. Linear quantization) Values other than 1 indicate some form of compression.
    private short mNumChannels;
    private int mSampleRate;
    private int mByteRate;
    private short mBlockAlign;
    private final short mBitsPerSample = 16; // 8 bits = 8, 16 bits = 16, etc. TODO we should set in somehow probably

    // The "data" subchunk contains the size of the data and the actual sound
    private final String mSubchunk2ID = "data";
    private int mSubchunk2Size;

    private transient byte[] data;

    public final File file;

    private transient RandomAccessFile writer;

    private transient boolean prepared;
    private transient boolean consumed;

    private int totalNumberOfBytes;

    public CheapWAV(File file, int audioFormat, int channelConfig, int sampleRateInHz) {
        this.file = file;

        if (audioFormat != AudioFormat.ENCODING_PCM_16BIT) {
            throw new IllegalArgumentException("Incorrect audio format, working with PCM 16 bit only!");
        }

        // Initial state
        mChunkSize = 0;

        mNumChannels = (short) (channelConfig == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1);
        mSampleRate = sampleRateInHz;

        setByteRate();
        setBlockAlign();

        // Initial state
        mSubchunk2Size = 0;
    }

    public void write(byte[] data) throws IOException {
        write(data, false);
    }

    public void write(byte[] data, boolean consume) throws IOException {
        if (consumed) {
            throw new IllegalStateException("File writer already consumed!");
        }

        if (!prepared) {
            prepare();
        }

        if (consume) {
            consume();
            return;
        }

        writer.write(data);
        totalNumberOfBytes += data.length;

        LoggerFactory.obtainLogger(TAG).
                d("write# " + String.format("Total number of bytes written is %d", totalNumberOfBytes));
    }

    private void setByteRate() {
        if (mSampleRate == 0 || mNumChannels == 0 || mBitsPerSample == 0) {
            throw new IllegalStateException(
                    String.format("ByteRate can't be set before either SampleRate(%d) or NumChannels(%d) or BitsPerSample(%d) are set!",
                            mSampleRate, mNumChannels, mBitsPerSample));
        }

        mByteRate = mSampleRate * mNumChannels * mBitsPerSample / 8;
    }

    private void setBlockAlign() {
        if (mNumChannels == 0 || mBitsPerSample == 0) {
            throw new IllegalStateException(
                    String.format("ByteRate can't be set before either NumChannels(%d) or BitsPerSample(%d) are set!",
                            mNumChannels, mBitsPerSample));
        }

        mBlockAlign = (short) (mNumChannels * mBitsPerSample / 8);
    }

    private void prepare() throws IOException {
        writer = new RandomAccessFile(file, "rw");

        // Set file length to 0 to prevent unexpected behavior in case file with the same name already exists
        writer.setLength(0);

        // Set RIFF-header section
        writer.writeBytes(mChunkID);
        writer.writeInt(mChunkSize);    // 0
        writer.writeBytes(mFormat);

        // Set fmt-subchunk
        writer.writeBytes(mSubchunk1ID);
        writer.writeInt(Integer.reverseBytes(mSubchunk1Size));
        writer.writeShort(Short.reverseBytes(mAudioFormat));
        writer.writeShort(Short.reverseBytes(mNumChannels));
        writer.writeInt(Integer.reverseBytes(mSampleRate));
        writer.writeInt(Integer.reverseBytes(mByteRate));
        writer.writeShort(Short.reverseBytes(mBlockAlign));
        writer.writeShort(Short.reverseBytes(mBitsPerSample));

        // Set data-subchunk
        writer.writeBytes(mSubchunk2ID);
        writer.writeInt(mSubchunk2Size);    // 0

        prepared = true;

        LoggerFactory.obtainLogger(TAG).d("prepare# writer.length = " + writer.length());
    }

    private void consume() throws IOException {
        writer.seek(4); // Write size to RIFF header
        writer.writeInt(Integer.reverseBytes(36 + totalNumberOfBytes));

        writer.seek(40); // Write size to Subchunk2Size field
        writer.writeInt(Integer.reverseBytes(totalNumberOfBytes));

        writer.close();

        consumed = true;

        // TODO set data

        LoggerFactory.obtainLogger(TAG).
                d("consume# " + String.format("File size is %d", file.length()));
    }

    public static CheapWAV concat(List<CheapWAV> wavs) {
        // TODO implement
        return null;
    }

    public static void split(Context context, CheapWAV wav, int duration) throws Exception {
        wav.read();

        // TODO real duration
        int d1 = duration / 2;
        int d2 = duration - d1;
        byte[] data1 = Arrays.copyOfRange(wav.data, 0, d1);
        byte[] data2 = Arrays.copyOfRange(wav.data, d1, wav.data.length);

        RandomAccessFile writer =
                new RandomAccessFile(FileUtils.getFilename(context), "rw");

        // Set RIFF-header section
        writer.writeBytes(wav.mChunkID);
        writer.writeInt(Integer.reverseBytes(36 + d1));
        writer.writeBytes(wav.mFormat);

        // Set fmt-subchunk
        writer.writeBytes(wav.mSubchunk1ID);
        writer.writeInt(Integer.reverseBytes(wav.mSubchunk1Size));
        writer.writeShort(Short.reverseBytes(wav.mAudioFormat));
        writer.writeShort(Short.reverseBytes(wav.mNumChannels));
        writer.writeInt(Integer.reverseBytes(wav.mSampleRate));
        writer.writeInt(Integer.reverseBytes(wav.mByteRate));
        writer.writeShort(Short.reverseBytes(wav.mBlockAlign));
        writer.writeShort(Short.reverseBytes(wav.mBitsPerSample));

        // Set data-subchunk
        writer.writeBytes(wav.mSubchunk2ID);
        writer.writeInt(Integer.reverseBytes(d1));

        writer.write(data1);

        writer.close();


        writer =
                new RandomAccessFile(FileUtils.getFilename(context), "rw");

        // Set RIFF-header section
        writer.writeBytes(wav.mChunkID);
        writer.writeInt(Integer.reverseBytes(36 + d2));
        writer.writeBytes(wav.mFormat);

        // Set fmt-subchunk
        writer.writeBytes(wav.mSubchunk1ID);
        writer.writeInt(Integer.reverseBytes(wav.mSubchunk1Size));
        writer.writeShort(Short.reverseBytes(wav.mAudioFormat));
        writer.writeShort(Short.reverseBytes(wav.mNumChannels));
        writer.writeInt(Integer.reverseBytes(wav.mSampleRate));
        writer.writeInt(Integer.reverseBytes(wav.mByteRate));
        writer.writeShort(Short.reverseBytes(wav.mBlockAlign));
        writer.writeShort(Short.reverseBytes(wav.mBitsPerSample));

        // Set data-subchunk
        writer.writeBytes(wav.mSubchunk2ID);
        writer.writeInt(Integer.reverseBytes(d2));

        writer.write(data2);

        writer.close();
    }

    private void read() {
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);

            // omit the header and both subchunks
            stream.read(new byte[44], 0, 44);

            int length = (int) file.length() - 44;
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("read# file data lenght is %d", length));

            data = new byte[length];
            stream.read(data, 0, length);
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("read# data lenght is %d", data.length));
        } catch (FileNotFoundException e) {
            LoggerFactory.obtainLogger(TAG).e(
                    String.format("File %s can not be found!", file.getAbsolutePath()),
                    e);
        } catch (IOException e) {
            LoggerFactory.obtainLogger(TAG).e(
                    String.format("Error while reading %s", file.getAbsolutePath()),
                    e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
                }
            }
        }
    }
}
