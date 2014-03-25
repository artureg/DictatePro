package com.wiseapps.davacon.core;

import android.media.AudioFormat;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/24/14
 *         Time: 12:20 PM
 *
 * TODO use write() method of RandomAccessFile class and set the positions explicitly
 */
public class WAVFileWriter {
    private static final String TAG = WAVFileWriter.class.getSimpleName();

    private RandomAccessFile writer;
    private final WAVFile wav;

    private int totalNumberOfBytes;

    private boolean prepared, consumed;

    public WAVFileWriter(File file) throws FileNotFoundException, IOException {
        this.wav = new WAVFile.Builder(file).
                setEncoding(AudioFormat.ENCODING_PCM_16BIT).
                setChannelType(AudioFormat.CHANNEL_IN_STEREO).
                setSampleRate(44100).
                build();

        prepare();
    }

    public void prepare() throws FileNotFoundException, IOException {
        writer = new RandomAccessFile(wav.getFile(), "rw");

        // Set file length to 0 to prevent unexpected behavior in case file with the same name already exists
        writer.setLength(0);

        // Set RIFF-header section
        writer.writeBytes(wav.getChunkID());
        writer.writeInt(wav.getChunkSize());    // 0
        writer.writeBytes(wav.getFormat());

        // Set fmt-subchunk
        writer.writeBytes(wav.getSubchunk1ID());
        writer.writeInt(Integer.reverseBytes(wav.getSubchunk1Size()));
        writer.writeShort(Short.reverseBytes(wav.getAudioFormat()));
        writer.writeShort(Short.reverseBytes(wav.getNumChannels()));
        writer.writeInt(Integer.reverseBytes(wav.getSampleRate()));
        writer.writeInt(Integer.reverseBytes(wav.getByteRate()));
        writer.writeShort(Short.reverseBytes(wav.getBlockAlign()));
        writer.writeShort(Short.reverseBytes(wav.getBitsPerSample()));

        // Set data-subchunk
        writer.writeBytes(wav.getSubchunk2ID());
        writer.writeInt(wav.getSubchunk2Size());    // 0

        LoggerFactory.obtainLogger(TAG).d("prepare# writer.length = " + writer.length());

        prepared = true;
    }

    public void write(byte[] data) throws FileNotFoundException, IOException {
        if (!prepared) {
            throw new IllegalStateException("WAV file writer not prepared yet!");
        }

        if (consumed) {
            throw new IllegalStateException("WAV file writer already consumed!");
        }

        writer.write(data);
        totalNumberOfBytes += data.length;

        LoggerFactory.obtainLogger(TAG).
                d("write# " + String.format("Total number of bytes written is %d", totalNumberOfBytes));
    }

    public void consume() throws IOException {
        writer.seek(4); // Write size to RIFF header
        writer.writeInt(Integer.reverseBytes(36 + totalNumberOfBytes));

        writer.seek(40); // Write size to Subchunk2Size field
        writer.writeInt(Integer.reverseBytes(totalNumberOfBytes));

        writer.close();

        consumed = true;

        LoggerFactory.obtainLogger(TAG).
                d("close# " + String.format("File size is %d", wav.getFile().length()));
    }
}
