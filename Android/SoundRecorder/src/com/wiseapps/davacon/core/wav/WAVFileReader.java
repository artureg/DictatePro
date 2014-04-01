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

    private final WAVFile wav;

    public WAVFileReader(WAVFile wav) {
        this.wav = wav;
    }

    /**
     * Reads .wav file.
     */
    public void read() {
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(wav.getFile());

            // omit the header and both subchunks
            stream.read(new byte[44], 0, 44);

            int length = (int) wav.getFile().length() - 44;
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("read# file data lenght is %d", length));

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
}
