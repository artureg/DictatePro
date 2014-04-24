package com.wiseapps.davacon.core.se;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/15/14
 *         Time: 4:07 PM
 */
class SESoundRecorder {
    private static final String TAG = SESoundRecorder.class.getSimpleName();

    private static final int MIN_BUFFER_SIZE = 1600;
    private static final int MULT = 4;

    private RecordingThread thread;

    private final AudioStream stream;

    private List<SESoundRecorderStateListener> listeners = new ArrayList<SESoundRecorderStateListener>();

    SESoundRecorder(AudioStream stream) {
        this.stream = stream;
    }

    void start() {
        thread = new RecordingThread(true);
        thread.start();
    }

    void stop() {
        thread.stopRecording();
    }

    void addHandler(SESoundRecorderStateListener listener) {
        listeners.add(listener);
    }

    void removeHandler(SESoundRecorderStateListener listener) {
        listeners.remove(listener);
    }

    private void sendMsgStarted() {
        for (SESoundRecorderStateListener listener : listeners) {
            if (listener != null) {
                listener.onRecordingStarted();
            }
        }
    }

    private void sendMsgStopped() {
        for (SESoundRecorderStateListener listener : listeners) {
            if (listener != null) {
                listener.onRecordingStopped();
            }
        }
    }

    private void sendMsgError() {
        for (SESoundRecorderStateListener listener : listeners) {
            if (listener != null) {
                listener.onRecordingError();
            }
        }
    }

    private class RecordingThread extends Thread {
        private boolean running;

        private AudioRecord audioRecord;

        private RecordingThread(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            open();
            work();
            close();
        }

        private void open() {
            int minBufferSize = MIN_BUFFER_SIZE * MULT;

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT, minBufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                sendMsgError();
                return;
            }

            stream.open(AudioStream.Mode.WRITE);
            sendMsgStarted();

            audioRecord.startRecording();
        }

        private void work() {
            int minBufferSize = MIN_BUFFER_SIZE * MULT;

            OutputStream out = null;

            try {
                out = stream.getOutputStream();

                byte[] data = new byte[minBufferSize];
                while(running) {
                    audioRecord.read(data, 0, data.length);
                    out.write(data);

                    LoggerFactory.obtainLogger(TAG).
                            d("run# recorded " + data.length);

                    stream.updatePosition(data.length);
                    stream.updateDuration(data.length);
                }

                sendMsgStopped();
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        e(e.getMessage(), e);

                sendMsgError();
            } finally {
                stream.finalizePosition();
                stream.finalizeDuration();

                LoggerFactory.obtainLogger(TAG).
                        d("work# finally");
                if (out != null) {
                    try {
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        LoggerFactory.obtainLogger(TAG).
                                e(e.getMessage(), e);
                    }
                }
            }
        }

        private void close() {
            stream.close();

            audioRecord.stop();
            audioRecord.release();
        }


        private void stopRecording() {
            running = false;
        }
    }

    interface SESoundRecorderStateListener {
        void onRecordingStarted();
        void onRecordingStopped();
        void onRecordingError();
    }
}
