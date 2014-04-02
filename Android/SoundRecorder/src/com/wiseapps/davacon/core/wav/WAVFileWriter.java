package com.wiseapps.davacon.core.wav;

import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class to write .wav file.
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/30/14
 *         Time: 11:12 AM
 */
public class WAVFileWriter {
    private static final String TAG = WAVFileWriter.class.getSimpleName();

    private RandomAccessFile writer;

    private transient boolean prepared;
    private transient boolean consumed;

    private int totalNumberOfBytes;

    private final WAVFile wav;

    public WAVFileWriter(WAVFile wav) {
        this.wav = wav;
    }

    /**
     * Method to write data to .wav file.
     *
     * @param data array of bytes to write to file
     * @throws IOException
     */
    public void write(byte[] data) throws IOException {
        if (consumed) {
            throw new IllegalStateException("File writer already consumed!");
        }

        if (!prepared) {
            prepare();
        }

        writer.write(data);
        totalNumberOfBytes += data.length;

        LoggerFactory.obtainLogger(TAG).
                d("write# " + String.format("Total number of bytes written is %d", totalNumberOfBytes));
    }

    public void consume() throws IOException {
        writer.seek(4); // Write size to RIFF header (ChunkSize)
        writer.writeInt(Integer.reverseBytes(36 + totalNumberOfBytes));

        writer.seek(40); // Write size to Subchunk2Size field
        writer.writeInt(Integer.reverseBytes(totalNumberOfBytes));

        writer.close();

        consumed = true;

        LoggerFactory.obtainLogger(TAG).
                d("consume# " + String.format("File size is %d", wav.getFile().length()));
    }

    private void prepare() throws IOException {
        writer = new RandomAccessFile(wav.getFile(), "rw");
        writer.setLength(0);

        // Set RIFF-header section
        writer.writeBytes(wav.getChunkID());
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# ChunkID = %s", wav.getChunkID()));

        writer.writeInt(Integer.reverseBytes(0));

        writer.writeBytes(wav.getFormat());
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Format = %s", wav.getFormat()));

        // Set fmt-subchunk
        writer.writeBytes(wav.getSubchunk1ID());
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Subchunk1ID = %s", wav.getSubchunk1ID()));

        writer.writeInt(Integer.reverseBytes(wav.getSubchunk1Size()));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Subchunk1Size = %s", wav.getSubchunk1Size()));

        writer.writeShort(Short.reverseBytes(wav.getAudioFormat()));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# AudioFormat = %s", wav.getAudioFormat()));

        writer.writeShort(Short.reverseBytes(wav.getNumChannels()));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# NumChannels = %s", wav.getNumChannels()));

        writer.writeInt(Integer.reverseBytes(wav.getSampleRate()));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# SampleRate = %s", wav.getSampleRate()));

        writer.writeInt(Integer.reverseBytes(wav.getByteRate()));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# ByteRate = %s", wav.getByteRate()));

        writer.writeShort(Short.reverseBytes(wav.getBlockAlign()));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# BlockAlign = %s", wav.getBlockAlign()));

        writer.writeShort(Short.reverseBytes(wav.getBitsPerSample()));
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# BitsPerSample = %s", wav.getBitsPerSample()));

        // Set data-subchunk
        writer.writeBytes(wav.getSubchunk2ID());
        LoggerFactory.obtainLogger(TAG).
                d(String.format("prepare# Subchunk2ID = %s", wav.getSubchunk2ID()));

        writer.writeInt(Integer.reverseBytes(0));

        prepared = true;

        LoggerFactory.obtainLogger(TAG).d("prepare# writer.length = " + writer.length());
    }
}
