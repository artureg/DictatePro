package com.wiseapps.davacon.core.se;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.logging.LoggerFactory;

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

    static final int MSG_RECORDING_STARTED = 0;
    static final int MSG_RECORDING_IN_PROGRESS = 1;
    static final int MSG_RECORDING_STOPPED = 2;
    static final int MSG_RECORDING_ERROR = 3;

    private RecordingThread thread;

    private final SEAudioStream stream;
    private List<Handler> handlers = new ArrayList<Handler>();

    SESoundRecorder(SEAudioStream stream) {
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
            int minBufferSize =
                    AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT);

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT, minBufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                handler.sendMessage(handler.obtainMessage(MSG_RECORDING_ERROR));
                return;
            }

            // TODO here we 1) check the streamRead current position and find the current record
            // TODO 2) split current record R on R1 and R2 (after a record is created, project .plist is auto updated)
            stream.open(SEAudioStream.Mode.WRITE);
            handler.sendMessage(handler.obtainMessage(MSG_RECORDING_STARTED));

            byte[] data = new byte[minBufferSize];
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
