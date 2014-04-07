package com.wiseapps.davacon.core.wav;

import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class to read .wav file.
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/30/14
 *         Time: 11:35 AM
 */
public class WAVFileReader {
    private static final String TAG = WAVFileReader.class.getSimpleName();

    private FileInputStream stream;

    private final WAVFile wav;

    public WAVFileReader(WAVFile wav) {
        this.wav = wav;
    }

    /**
     * Reads .wav file.
     */
    public void read() {
        try {
            stream = new FileInputStream(wav.getFile());

            if (!prepare()) {
                LoggerFactory.obtainLogger(TAG).
                        d("read# .wav file has incorrect format, returning...");
                return;
            }

//            // omit the header and both subchunks
//            stream.read(new byte[44], 0, 44);

            int length = (int) wav.getFile().length() - 44;
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("read# file data length is %d", length));

            byte[] data = new byte[length];
            stream.read(data, 0, length);

            wav.setData(data);

            LoggerFactory.obtainLogger(TAG).
                    d(String.format("read# data lenght is %d", data.length));
        } catch (FileNotFoundException e) {
            LoggerFactory.obtainLogger(TAG).e(
                    String.format("File %s can not be found!", wav.getFile().getAbsolutePath()),
                    e);
        } catch (IOException e) {
            LoggerFactory.obtainLogger(TAG).e(
                    String.format("Error while reading %s", wav.getFile().getAbsolutePath()),
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

    private boolean prepare()throws IOException {
        long length = wav.getFile().length();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# .wav file length is %s", length));

        // ChunkID
        byte[] data = new byte[4];
        stream.read(data, 0, 4);
        if (data[0] != 'R' ||
                data[1] != 'I' ||
                data[2] != 'F' ||
                data[3] != 'F') {
            LoggerFactory.obtainLogger(TAG).d("prepare# incorrect ChunkID");
            return false;
        }
        wav.setChunkID(new String(data));

        // ChunkSize
        int chunkSize = readInt();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# ChunkSize = %s", chunkSize));
        wav.setChunkSize(chunkSize);

        // Format
        data = new byte[4];
        stream.read(data, 0, 4);
        if (data[0] != 'W' ||
                data[1] != 'A' ||
                data[2] != 'V' ||
                data[3] != 'E') {
            LoggerFactory.obtainLogger(TAG).d("prepare# incorrect Format");
            return false;
        }
        wav.setFormat(new String(data));

        // Subchunk1ID
        data = new byte[4];
        stream.read(data, 0, 4);
        if (data[0] != 'f' ||
                data[1] != 'm' ||
                data[2] != 't' ||
                data[3] != ' ' ) {
            LoggerFactory.obtainLogger(TAG).d("prepare# incorrect Subchunk1ID");
            return false;
        }
        wav.setSubchunk1ID(new String(data));

        // Subchunk1Size
        int subchunk1Size = readInt();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Subchunk1Size = %s", subchunk1Size));
        wav.setSubchunk1Size(subchunk1Size);

        // AudioFormat
        short audioFormat = readShort();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# AudioFormat = %s", audioFormat));
        wav.setAudioFormat(audioFormat);

        // NumChannels
        short numChannels = readShort();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# NumChannels = %s", numChannels));
        wav.setNumChannels(numChannels);

        // SampleRate
        int sampleRate = readInt();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# SampleRate = %s", sampleRate));
        wav.setSampleRate(sampleRate);

        // ByteRate
        int byteRate = readInt();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# ByteRate = %s", byteRate));
        wav.setByteRate(byteRate);

        // BlockAlign
        short blockAlign = readShort();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# BlockAlign = %s", blockAlign));
        wav.setBlockAlign(blockAlign);

        // BitsPerSample
        short bitsPerSample = readShort();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# BitsPerSample = %s", bitsPerSample));
        wav.setBitsPerSample(bitsPerSample);

        // Subchunk2ID
        data = new byte[4];
        stream.read(data, 0, 4);
        if (data[0] != 'd' ||
                data[1] != 'a' ||
                data[2] != 't' ||
                data[3] != 'a' ) {
            LoggerFactory.obtainLogger(TAG).d("prepare# incorrect Subchunk2ID");
            return false;
        }
        wav.setSubchunk2ID(new String(data));

        // Subchunk2Size
        int subchunk2Size = readInt();
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Subchunk2Size = %s", subchunk2Size));
        wav.setSubchunk2Size(subchunk2Size);

        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# chunkSize - subchunk2Size = %s", (chunkSize - subchunk2Size)));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# .wav file length - chunkSize = %s", (length - chunkSize)));

        return wav.isFormatCorrect();
    }

    private short readShort() throws IOException {
        byte[] data = new byte[2];
        stream.read(data, 0, 2);
        return (short)(((0xff & data[1]) << 8) |
                ((0xff & data[0])));
    }

    private int readInt() throws IOException {
        byte[] data = new byte[4];
        stream.read(data, 0, 4);
        return ((0xff & data[3]) << 24) |
                ((0xff & data[2]) << 16) |
                ((0xff & data[1]) << 8) |
                ((0xff & data[0]));
    }
}
