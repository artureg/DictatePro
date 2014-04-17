package com.wiseapps.davacon.core.se;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/15/14
 *         Time: 4:07 PM
 */
class SESoundPlayer {
    private static final String TAG = SESoundPlayer.class.getSimpleName();

    static final int MSG_PLAYING_STARTED = 0;
    static final int MSG_PLAYING_IN_PROGRESS = 1;
    static final int MSG_PLAYING_PAUSED = 2;
    static final int MSG_PLAYING_STOPPED = 3;
    static final int MSG_PLAYING_ERROR = 4;

    private enum PlayerState {
        PLAYING, PAUSED, STOPPED
    }

    private PlayingThread thread;

    private final SEAudioStream stream;
    private List<Handler> handlers = new ArrayList<Handler>();

    SESoundPlayer(SEAudioStream stream) {
        this.stream = stream;
    }

    void start() {
        thread = new PlayingThread(PlayerState.PLAYING);
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
        private PlayerState state;

        private PlayingThread(PlayerState state) {
            this.state = state;
        }

        @Override
        public void run() {
            int minBufferSize =
                    AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT);

            AudioTrack audioTrack = new AudioTrack(STREAM_TYPE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT,
                    minBufferSize, MODE);

            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                handler.sendMessage(handler.obtainMessage(MSG_PLAYING_ERROR));
                return;
            }

            stream.open(SEAudioStream.Mode.READ);
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_STARTED));

            int offset = 0;
            byte data[] = stream.read(offset, 1);
            while(state != PlayerState.STOPPED) {
                if (state == PlayerState.PAUSED) {
                    // TODO send just once
                    handler.sendMessage(handler.obtainMessage(MSG_PLAYING_PAUSED));
                    continue;
                }

                audioTrack.write(data, 0, data.length);
                audioTrack.play();

                data = stream.read(offset, 1);

                offset++;

                handler.sendMessage(handler.obtainMessage(MSG_PLAYING_IN_PROGRESS));
            }

            stream.close();

            audioTrack.stop();
            audioTrack.release();

            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_STOPPED));
        }

        void pausePlaying() {
            state = PlayerState.PAUSED;
        }

        void stopPlaying() {
            state = PlayerState.STOPPED;
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
