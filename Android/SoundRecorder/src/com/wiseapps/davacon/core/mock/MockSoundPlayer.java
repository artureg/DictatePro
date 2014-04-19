package com.wiseapps.davacon.core.mock;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.core.mock.MockProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 5:09 PM
 */
public class MockSoundPlayer {
    private static final String TAG = MockSoundPlayer.class.getSimpleName();

    static final int MSG_PLAYING_STARTED = 0;
    static final int MSG_PLAYING_IN_PROGRESS = 1;
    static final int MSG_PLAYING_PAUSED = 2;
    static final int MSG_PLAYING_STOPPED = 3;
    static final int MSG_PLAYING_ERROR = 4;

    private static final int DURATION_SECONDS = 1;

//    private enum PlayerState {
//        PLAYING, PAUSED, STOPPED
//    }

    private PlayingThread thread;

    private final MockAudioStream stream;
    private List<Handler> handlers = new ArrayList<Handler>();

    MockSoundPlayer(MockAudioStream stream) {
        this.stream = stream;
    }

    void start() {
        thread = new PlayingThread(true);
        thread.start();
    }

    void pause() {
        thread.pausePlaying();
    }

    void stop() {
        thread.stopPlaying();
    }

    void addHandler(Handler handler) {
        handlers.add(handler);
    }

    void removeHandler(Handler handler) {
        handlers.remove(handler);
    }

    private void sendMsgStarted() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_STARTED));
        }
    }

    private void sendMsgInProgress() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_IN_PROGRESS));
        }
    }

    private void sendMsgPaused() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_PAUSED));
        }
    }

    private void sendMsgStopped() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_STOPPED));
        }
    }

    private void sendMsgError() {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_ERROR));
        }
    }

    private class PlayingThread extends Thread {
        private boolean running;

        // let player pause operation takes preference upon operation stop
        private boolean paused = true;

        private PlayingThread(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            AudioTrack audioTrack = new AudioTrack(STREAM_TYPE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT,
                    MIN_BUFFER_SIZE, MODE);

            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                handler.sendMessage(handler.obtainMessage(MSG_PLAYING_ERROR));
                return;
            }

            stream.open(MockAudioStream.Mode.READ);
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_STARTED));

            audioTrack.play();

            byte data[] = stream.read(0, DURATION_SECONDS);
            while(running && data != null) {
                audioTrack.write(data, 0, data.length);

                data = stream.read(0, DURATION_SECONDS);

                handler.sendMessage(handler.obtainMessage(MSG_PLAYING_IN_PROGRESS));
            }

            stream.close();

            audioTrack.stop();
            audioTrack.release();

            handler.sendMessage(handler.obtainMessage(
                    paused ? MSG_PLAYING_PAUSED : MSG_PLAYING_STOPPED));
        }

        void stopPlaying() {
            running = false;
            paused = false;
        }

        void pausePlaying() {
            running = false;
            paused = true;
        }

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case MSG_PLAYING_STARTED: {
                        sendMsgStarted();
                        break;
                    }
                    case MSG_PLAYING_IN_PROGRESS: {
                        sendMsgInProgress();
                        break;
                    }
                    case MSG_PLAYING_PAUSED: {
                        sendMsgPaused();
                        break;
                    }
                    case MSG_PLAYING_STOPPED: {
                        sendMsgStopped();
                        break;
                    }
                    case MSG_PLAYING_ERROR: {
                        sendMsgError();
                        break;
                    }
                }
            }
        };
    }
}
