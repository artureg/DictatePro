package com.wiseapps.davacon.core.mock;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.core.mock.MockProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 5:09 PM
 */
public class MockSoundRecorder {
    private static final String TAG = MockSoundRecorder.class.getSimpleName();

    static final int MSG_RECORDING_STARTED = 0;
    static final int MSG_RECORDING_IN_PROGRESS = 1;
    static final int MSG_RECORDING_STOPPED = 2;
    static final int MSG_RECORDING_ERROR = 3;

    private RecordingThread thread;

    private final MockAudioStream stream;
    private List<Handler> handlers = new ArrayList<Handler>();

    MockSoundRecorder(MockAudioStream stream) {
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

    private void sendMsgInProgress() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_IN_PROGRESS));
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

        private RecordingThread(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT, MIN_BUFFER_SIZE);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                handler.sendMessage(handler.obtainMessage(MSG_RECORDING_ERROR));
                return;
            }

            stream.open(MockAudioStream.Mode.WRITE);
            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_STARTED));

            audioRecord.startRecording();

            byte[] data = new byte[MIN_BUFFER_SIZE];
            while(running) {
                LoggerFactory.obtainLogger(TAG).
                        d("running " + data.length);
                audioRecord.read(data, 0, data.length);
                stream.write(data);

                handler.sendMessage(handler.obtainMessage(MSG_RECORDING_IN_PROGRESS));
            }

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
                        sendMsgInProgress();
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
