package com.wiseapps.davacon.speex;

import android.media.AudioFormat;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/17/14
 *         Time: 4:43 PM
 */
public class MockSpeexWrapper {
    private static final String TAG = MockSpeexWrapper.class.getSimpleName();

    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final short BITS_PER_SAMPLE = 8;

    public static int getFormat(String filePath) {
        return 0;
    }

    public static int getSampleRate(String filePath, int format) {
        return SAMPLE_RATE_IN_HZ;
    }

    public static byte[] read(String filePath, double offset, double duration, int format) {
        RandomAccessFile raf = null;

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            raf = new RandomAccessFile(file, "r");

            int iOffset = calculateDataLengthFromDuration(offset, true);
            int iDuration = calculateDataLengthFromDuration(duration, true);

            byte[] data = new byte[iDuration];
            raf.read(data, 44 + iOffset, iDuration);

            return data;
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).
                    e("read# ", e);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception e) {
                    LoggerFactory.obtainLogger(TAG).
                            e("read# ", e);
                }
            }
        }

        return null;
    }

    /**
     * @return 0 if operation returned successfully, -1 otherwise
     */
    public static int write(String filePath, byte[] data, int format) {
        RandomAccessFile raf = null;

        try {
            boolean isNew = false;

            File file = new File(filePath);
            if (!file.exists()) {
                isNew = true;

                if (!file.createNewFile()) {
                    return -1;
                }
            }

            int totalNumberOfBytes = 0;

            raf = new RandomAccessFile(file, "rw");
            LoggerFactory.obtainLogger(TAG).
                    d("write# isNew = " + isNew);
            if (isNew) {
                createHeader(raf);
            } else {
                totalNumberOfBytes = getTotalNumberOfBytes(raf);
                LoggerFactory.obtainLogger(TAG).d("write# totalNumberOfBytes = " + totalNumberOfBytes);
            }

            LoggerFactory.obtainLogger(TAG).
                    d("write# before file.length = " + file.length());
            raf.seek(file.length());
            raf.write(data);
            LoggerFactory.obtainLogger(TAG).
                    d("write# after file.length = " + file.length());

            updateHeader(raf, totalNumberOfBytes + data.length);
            raf.close();

            return 0;
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).
                    e("read# ", e);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception e) {
                    LoggerFactory.obtainLogger(TAG).
                            e("read# ", e);
                }
            }
        }

        return -1;
    }

    private static void createHeader(RandomAccessFile raf) throws Exception {
        raf.setLength(0);

        // Set RIFF-header section
        raf.writeBytes(DEFAULT_CHUNK_ID);
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# ChunkID = %s", DEFAULT_CHUNK_ID));

        raf.writeInt(Integer.reverseBytes(DEFAULT_CHUNK_SIZE));

        raf.writeBytes(DEFAULT_FORMAT);
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Format = %s", DEFAULT_FORMAT));

        // Set fmt-subchunk
        raf.writeBytes(DEFAULT_SUBCHUNK_1_ID);
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Subchunk1ID = %s", DEFAULT_SUBCHUNK_1_ID));

        raf.writeInt(Integer.reverseBytes(DEFAULT_SUBCHUNK_1_SIZE));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Subchunk1Size = %s", DEFAULT_SUBCHUNK_1_SIZE));

        raf.writeShort(Short.reverseBytes(DEFAULT_AUDIO_FORMAT));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# AudioFormat = %s", DEFAULT_AUDIO_FORMAT));

        raf.writeShort(Short.reverseBytes(DEFAULT_NUM_CHANNELS));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# NumChannels = %s", DEFAULT_NUM_CHANNELS));

        raf.writeInt(Integer.reverseBytes(DEFAULT_SAMPLE_RATE));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# SampleRate = %s", DEFAULT_SAMPLE_RATE));

        raf.writeInt(Integer.reverseBytes(DEFAULT_BYTE_RATE));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# ByteRate = %s", DEFAULT_BYTE_RATE));

        raf.writeShort(Short.reverseBytes(DEFAULT_BLOCK_ALIGN));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# BlockAlign = %s", DEFAULT_BLOCK_ALIGN));

        raf.writeShort(Short.reverseBytes(DEFAULT_BITS_PER_SAMPLE));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# BitsPerSample = %s", DEFAULT_BITS_PER_SAMPLE));

        // Set data-subchunk
        raf.writeBytes(DEFAULT_SUBCHUNK_2_ID);
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Subchunk2ID = %s", DEFAULT_SUBCHUNK_2_ID));

        raf.writeInt(Integer.reverseBytes(DEFAULT_SUBCHUNK_2_SIZE));
    }

    private static void updateHeader(RandomAccessFile raf, int totalNumberOfBytes) throws Exception {
        raf.seek(4); // Write size to RIFF header (ChunkSize)
        raf.writeInt(Integer.reverseBytes(36 + totalNumberOfBytes));

        raf.seek(40); // Write size to Subchunk2Size field
        raf.writeInt(Integer.reverseBytes(totalNumberOfBytes));
    }

    private static int getTotalNumberOfBytes(RandomAccessFile raf) throws Exception {
        byte[] data = new byte[4];

        raf.seek(40);
        raf.read(data, 0, 4);
        return ((0xff & data[3]) << 24) |
                ((0xff & data[2]) << 16) |
                ((0xff & data[1]) << 8) |
                ((0xff & data[0]));
    }

    private static int calculateDataLengthFromDuration(double duration, boolean read) {
        int sampleRate = SAMPLE_RATE_IN_HZ;
        int numChannels = read ? CHANNEL_CONFIG_OUT : CHANNEL_CONFIG_IN;
        int bitsPerSample = BITS_PER_SAMPLE;

        return (int) (duration / (sampleRate * numChannels * bitsPerSample / 8));
    }

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
    private static final short DEFAULT_BITS_PER_SAMPLE = 8;

    private static final String DEFAULT_SUBCHUNK_2_ID = "data";
    private static final int DEFAULT_SUBCHUNK_2_SIZE = 0;
}
