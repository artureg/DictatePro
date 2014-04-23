package com.wiseapps.davacon.core.se;

import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/15/14
 *         Time: 4:07 PM
 *
 *         TODO update project current position!
 */
class SESoundPlayer {
    private static final String TAG = SESoundPlayer.class.getSimpleName();

    static final int MSG_PLAYING_STARTED = 0;
    static final int MSG_PLAYING_IN_PROGRESS = 1;
    static final int MSG_PLAYING_PAUSED = 2;
    static final int MSG_PLAYING_STOPPED = 3;
    static final int MSG_PLAYING_ERROR = 4;

    private PlayingThread thread;

    private final AudioStream stream;

    private List<Handler> handlers = new ArrayList<Handler>();

    SESoundPlayer(AudioStream stream) {
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

    private void sendMsgInProgress(Object obj) {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_IN_PROGRESS, obj));
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

        private AudioTrack audioTrack;

        // let player pause operation takes preference upon operation stop
        private boolean paused = true;

        private PlayingThread(boolean running) {
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

            audioTrack = new AudioTrack(STREAM_TYPE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT,
                    minBufferSize, MODE);

            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                handler.sendMessage(handler.obtainMessage(MSG_PLAYING_ERROR));
                return;
            }

            stream.open(AudioStream.Mode.READ);
            handler.sendMessage(handler.obtainMessage(MSG_PLAYING_STARTED));

            audioTrack.play();
        }

        private void work() {
            int minBufferSize = 1600;

            InputStream in = null;

//            long played = 0;

            try {
                in = stream.getInputStream();

                byte data[] = new byte[minBufferSize];
                while(running && (in.read(data) != -1)) {
                    audioTrack.write(data, 0, data.length);

                    LoggerFactory.obtainLogger(TAG).
                            d("run# played " + data.length);

//                    played += data.length;
//                    LoggerFactory.obtainLogger(TAG).
//                            d("run# played " + played);

                    stream.updatePosition(data.length);
                    stream.updateDuration(data.length);

                    handler.sendMessage(handler.obtainMessage(MSG_PLAYING_IN_PROGRESS, data.length));
//                    LoggerFactory.obtainLogger(TAG).
//                            d("run# running " + running);
                }
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        d("work# catch");
                LoggerFactory.obtainLogger(TAG).
                        e(e.getMessage(), e);

                handler.sendMessage(handler.obtainMessage(MSG_PLAYING_ERROR));
            } finally {
                stream.finalizePosition();
                stream.finalizeDuration();

                LoggerFactory.obtainLogger(TAG).
                        d("work# finally");
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        LoggerFactory.obtainLogger(TAG).
                                e(e.getMessage(), e);
                    }
                }
            }

//            LoggerFactory.obtainLogger(TAG).d("work# played = " + played);
//            LoggerFactory.obtainLogger(TAG).d("work# exited due to EOF = " + running);
        }

        private void close() {
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
                        sendMsgInProgress(msg.obj);
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
