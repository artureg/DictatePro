package com.wiseapps.davacon.core.se;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/15/14
 *         Time: 4:07 PM
 */
class SESoundRecorder {
    private static final String TAG = SESoundRecorder.class.getSimpleName();

    static final int MSG_RECORDING_STARTED = 0;
    static final int MSG_RECORDING_IN_PROGRESS = 1;
    static final int MSG_RECORDING_STOPPED = 2;
    static final int MSG_RECORDING_ERROR = 3;

    private RecordingThread thread;

//    private final SEAudioStream stream;
    private final AudioStream stream;
    private List<Handler> handlers = new ArrayList<Handler>();

//    SESoundRecorder(SEAudioStream stream) {
//        this.stream = stream;
//    }
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

    void addHandler(Handler handler) {
        handlers.add(handler);
    }

    void removeHandler(Handler handler) {
        handlers.remove(handler);
    }

    private void sendMsgStarted() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_STARTED));
        }
    }

    private void sendMsgInProgress(Object obj) {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_IN_PROGRESS, obj));
        }
    }

    private void sendMsgStopped() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_STOPPED));
        }
    }

    private void sendMsgError() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_ERROR));
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
            int minBufferSize = 1600;

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT, minBufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                handler.sendMessage(handler.obtainMessage(MSG_RECORDING_ERROR));
                return;
            }

            stream.open(AudioStream.Mode.WRITE);
            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_STARTED));

            audioRecord.startRecording();
        }

        private void work() {
            int minBufferSize = 1600;

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

                    handler.sendMessage(handler.obtainMessage(MSG_RECORDING_IN_PROGRESS, minBufferSize));
                }
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        e(e.getMessage(), e);

                handler.sendMessage(handler.obtainMessage(MSG_RECORDING_ERROR));
            } finally {
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

//            LoggerFactory.obtainLogger(TAG).d("work# record.duration = " +
//                    ((RecordAudioStream) stream).record.duration);
//            LoggerFactory.obtainLogger(TAG).d("work# project.duration = " +
//                    ((RecordAudioStream) stream).record.project.duration);
        }

        private void close() {
            stream.close();

            audioRecord.stop();
            audioRecord.release();

            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_STOPPED));
        }

        private void stopRecording() {
            running = false;
        }

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case MSG_RECORDING_STARTED: {
                        sendMsgStarted();
                        break;
                    }
                    case MSG_RECORDING_IN_PROGRESS: {
                        sendMsgInProgress(msg.obj);
                        break;
                    }
                    case MSG_RECORDING_STOPPED: {
                        sendMsgStopped();
                        break;
                    }
                    case MSG_RECORDING_ERROR: {
                        sendMsgError();
                        break;
                    }
                }
            }
        };
    }
}
