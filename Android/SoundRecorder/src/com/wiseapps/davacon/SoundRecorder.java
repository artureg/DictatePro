package com.wiseapps.davacon;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/13/14
 *         Time: 11:05 AM
 */
public class SoundRecorder {
    private static final String TAG = SoundRecorder.class.getSimpleName();

    public static final int MSG_RECORDING_STARTED = 0;
    public static final int MSG_DATA_RECORDED = 1;
    public static final int MSG_RECORDING_STOPPED = 2;
    public static final int MSG_RECORDING_ERROR = 3;

    private List<Handler> handlers;

    private boolean running;

    public SoundRecorder() {
        handlers = new ArrayList<Handler>();
    }

    public void start() {
        new Thread(new Runnable() {
            private static final int RECORDER_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
            private static final int RECORDER_SAMPLE_RATE_IN_HZ = 8000;
            private static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
            private static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

            private static final int BUFF_COUNT = 10;

            private AudioRecord mRecorder;

            @Override
            public void run() {
                int minBufferSize = AudioRecord.getMinBufferSize(
                        RECORDER_SAMPLE_RATE_IN_HZ, RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT);

                if (minBufferSize == AudioRecord.ERROR) {
                    sendMsg(MSG_RECORDING_ERROR, "getMinBufferSize returned ERROR");
                    return;
                }

                if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    sendMsg(MSG_RECORDING_ERROR, "getMinBufferSize returned ERROR_BAD_VALUE");
                    return;
                }

                mRecorder = new AudioRecord(RECORDER_AUDIO_SOURCE,
                        RECORDER_SAMPLE_RATE_IN_HZ, RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT, minBufferSize);
                if (mRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                    sendMsg(MSG_RECORDING_ERROR, "audio recorder state != STATE_INITIALIZED");
                    return;
                }

                mRecorder.startRecording();
                sendMsg(MSG_RECORDING_STARTED);

                byte[] data = new byte[minBufferSize];
                while (running) {
                    int samplesRead = mRecorder.read(data, 0, data.length);

                    if (samplesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                        sendMsg(MSG_RECORDING_ERROR, "read returned ERROR_INVALID_OPERATION");
                        return;
                    }

                    if (samplesRead == AudioRecord.ERROR_BAD_VALUE) {
                        sendMsg(MSG_RECORDING_ERROR, "read returned ERROR_BAD_VALUE");
                        return;
                    }

                    sendMsg(MSG_DATA_RECORDED, data);
                }

                mRecorder.stop();
                sendMsg(MSG_RECORDING_STOPPED);

                mRecorder.release();
                mRecorder = null;
            }
        }).start();

        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean isRecording() {
        return running;
    }

    public void addHandler(Handler handler) {
        handlers.add(handler);
    }

    private void sendMsg(int what) {
        sendMsg(what, null);
    }

    private void sendMsg(int what, Object data) {
        for (Handler handler : handlers) {
            if (data == null) {
                handler.sendMessage(handler.obtainMessage(what));
                return;
            }

            handler.sendMessage(handler.obtainMessage(what, data));
        }
    }
}
