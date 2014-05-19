package com.wiseapps.davacon.core.se;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.DurationUtils;

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

    private int position, duration;

    private RecordingThread thread;

    private final AudioStream stream;

    private List<SESoundRecorderStateListener> listeners = new ArrayList<SESoundRecorderStateListener>();

    SESoundRecorder(AudioStream stream, int position, int duration) {
        this.stream = stream;

//        this.position = position;
//        this.duration = duration;

        this.position = 0;
        this.duration = 0;
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
                listener.onRecordingStarted(position, duration);
            }
        }
    }

    private void sendMsgInProgress() {
        for (SESoundRecorderStateListener listener : listeners) {
            if (listener != null) {
                listener.onRecordingInProgress(position, duration);
            }
        }
    }

    private void sendMsgStopped() {
        for (SESoundRecorderStateListener listener : listeners) {
            if (listener != null) {
                listener.onRecordingStopped(position, duration);
            }
        }
    }

    private void sendMsgError() {
        for (SESoundRecorderStateListener listener : listeners) {
            if (listener != null) {
                listener.onRecordingError(position, duration);
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
                    SEProjectEngine.sampleRate, CHANNEL_CONFIG_IN, AUDIO_FORMAT, minBufferSize);
            audioRecord.setPositionNotificationPeriod((int) (SEProjectEngine.sampleRate * 0.1)); // notify each 0.1 second
            audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioRecord recorder) {
                }

                @Override
                public void onPeriodicNotification(AudioRecord recorder) {
                    long delta = DurationUtils.secondsToBytes(0.1);

                    stream.updatePosition(delta);
                    stream.updateDuration(delta);

                    position += delta;
                    duration += delta;

                    sendMsgInProgress();

//                    LoggerFactory.obtainLogger(TAG).
//                            d("onPeriodicNotification# position = " + position +
//                                    ", duration = " + duration);
                }
            });

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
                }

                sendMsgStopped();
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        e(e.getMessage(), e);

                sendMsgError();
            } finally {
                stream.finalizePosition();
                stream.finalizeDuration();

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
        void onRecordingStarted(int position, int duration);
        void onRecordingInProgress(int position, int duration);
        void onRecordingStopped(int position, int duration);
        void onRecordingError(int position, int duration);
    }
}
