package com.wiseapps.davacon.core.se;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

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

    private boolean running;

    private final SEAudioStream stream;
    private List<Handler> handlers = new ArrayList<Handler>();

    SESoundRecorder(SEAudioStream stream) {
        this.stream = stream;
    }

    void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, MIN_BUFFER_SIZE);

                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    sendMsgError();
                    return;
                }

                // TODO here we 1) check the streamRead current position and find the current record
                // TODO 2) split current record R on R1 and R2 (after a record is created, project .plist is auto updated)
                stream.open(SEAudioStream.Mode.WRITE);
                sendMsgStarted();

                byte[] data = new byte[MIN_BUFFER_SIZE];
                while(running) {
                    audioRecord.read(data, 0, data.length);
                    stream.write(data);

                    sendMsgInProgress();
                }

                stream.close();

                audioRecord.stop();
                audioRecord.release();

                sendMsgStopped();
            }
        }).start();

        running = true;
    }

    void stop() {
        running = false;
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
}
