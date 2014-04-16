package com.wiseapps.davacon.core.se;

import android.media.AudioTrack;
import android.os.Handler;

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

    private enum State {
        PLAYING, PAUSED, STOPPED
    }
    private State state;

    private final SEAudioStream stream;
    private List<Handler> handlers = new ArrayList<Handler>();

    SESoundPlayer(SEAudioStream stream) {
        this.stream = stream;
    }

    void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AudioTrack audioTrack = new AudioTrack(STREAM_TYPE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT,
                        MIN_BUFFER_SIZE, MODE);

                if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                    sendMsgError();
                    return;
                }

                stream.open(SEAudioStream.Mode.READ);
                sendMsgStarted();

                int offset = 0;
                byte data[] = stream.read(offset, 1);
                while(state != State.STOPPED) {
                    if (state == State.PAUSED) {
                        // TODO send just once
                        sendMsgPaused();
                    }

                    audioTrack.write(data, 0, data.length);
                    audioTrack.play();

                    data = stream.read(offset, 1);

                    offset++;

                    sendMsgInProgress();
                }

                stream.close();

                audioTrack.stop();
                audioTrack.release();

                sendMsgStopped();
            }
        }).start();

        state = State.PLAYING;
    }

    void pause() {
        state = State.PAUSED;
    }

    void stop() {
        state = State.STOPPED;
    }

    void addHandler(Handler handler) {
        handlers.add(handler);
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
}
