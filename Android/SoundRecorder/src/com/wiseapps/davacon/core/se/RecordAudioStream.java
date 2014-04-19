package com.wiseapps.davacon.core.se;

import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 9:23 PM
 */
public class RecordAudioStream extends AudioStream {
    private static final String TAG = RecordAudioStream.class.getSimpleName();

    final SERecord record;

    private OutputStream mockOut;
    private InputStream mockIn;

    RecordAudioStream(final SERecord record) {
        this.record = record;
    }

    @Override
    void open(Mode mode) {
        this.mode = mode;
    }

    @Override
    void close() {
        File file = new File(record.soundPath);
        LoggerFactory.obtainLogger(TAG).
                d("work# file.length = " + file.length());

//        mockClose();
    }

    @Override
    void clear() {
    }

    @Override
    InputStream getInputStream() throws Exception {
        // TODO implement offsets
//        return SpeexWrapper.getInputStream(record.soundPath, 0);

        return mockGetInputStream();
    }

    @Override
    OutputStream getOutputStream() throws Exception{
//        return SpeexWrapper.getOutputStream(record.soundPath, 0);

        return mockGetOutputStream();
    }

//    private void mockClose() {
//        if (mode == Mode.READ) {
//            if (mockIn != null) {
//                try {
//                    mockIn.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            return;
//        }
//
//        if (mode == Mode.WRITE) {
//            if (mockOut != null) {
//                try {
//                    mockOut.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private InputStream mockGetInputStream() throws Exception {
        File file = new File(record.soundPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        mockIn = new FileInputStream(file);
        return mockIn;
    }

    private OutputStream mockGetOutputStream() throws Exception {
        File file = new File(record.soundPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        mockOut = new FileOutputStream(file);
        return mockOut;
    }

    @Override
    void updatePosition(long position) {
        record.position += position;
        record.project.position += position;
    }

    @Override
    void updateDuration(long duration) {
        record.duration += duration;
        record.project.duration += duration;
    }

    @Override
    Mode getMode() {
        return mode;
    }
}
