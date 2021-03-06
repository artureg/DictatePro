package com.wiseapps.davacon.core.se;

import android.media.AudioTrack;

import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.NativeInputStream;
import com.wiseapps.davacon.utils.DurationUtils;

import java.io.InputStream;
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

    private static final int MIN_BUFFER_SIZE = 1600;
    private static final int MULT = 4;

    private int position, duration;

    private PlayingThread thread;

    private final AudioStream stream;

    private List<SESoundPlayerStateListener> listeners = new ArrayList<SESoundPlayerStateListener>();

    SESoundPlayer(AudioStream stream, int position, int duration) {
        this.stream = stream;

        this.position = 0;
        this.duration = 0;
    }

    void start() {
        thread = new PlayingThread(true);
        thread.start();
    }

    void pause() {
        thread.pausePlaying();
    }

    void addHandler(SESoundPlayerStateListener listener) {
        listeners.add(listener);
    }

    void removeHandler(SESoundPlayerStateListener listener) {
        listeners.remove(listener);
    }

    private void sendMsgStarted() {
        for (SESoundPlayerStateListener listener : listeners) {
            if (listener != null) {
                listener.onPlayingStarted(position, duration);
            }
        }
    }

    private void sendMsgInProgress() {
        for (SESoundPlayerStateListener listener : listeners) {
            if (listener != null) {
                listener.onPlayingInProgress(position, duration);
            }
        }
    }

    private void sendMsgPaused() {
        for (SESoundPlayerStateListener listener : listeners) {
            if (listener != null) {
                listener.onPlayingPaused(position, duration);
            }
        }
    }

    private void sendMsgError() {
        for (SESoundPlayerStateListener listener : listeners) {
            if (listener != null) {
                listener.onPlayingError(position, duration);
            }
        }
    }

    private class PlayingThread extends Thread {
        private boolean running;

        private AudioTrack audioTrack;

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
//            if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
//                LoggerFactory.obtainLogger(TAG).d("open# audioTrack is already initialized, returning...");
//                return;
//            }

            int minBufferSize = MIN_BUFFER_SIZE;

            audioTrack = new AudioTrack(STREAM_TYPE, SEProjectEngine.sampleRate, CHANNEL_CONFIG_OUT, AUDIO_FORMAT, minBufferSize, MODE);
            audioTrack.setPositionNotificationPeriod((int)(SEProjectEngine.sampleRate * 0.1));   // notify each 0.1 second
            audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {
                }
            
                @Override
                public void onPeriodicNotification(AudioTrack track) {
                    long delta = DurationUtils.secondsToBytes(0.1);

                    stream.updatePosition(delta);

                    position += delta;
        	
                    sendMsgInProgress();
        	}
            });

            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                sendMsgError();
                return;
            }

            stream.open(AudioStream.Mode.READ);
            sendMsgStarted();

            audioTrack.play();

//            LoggerFactory.obtainLogger(TAG).d("open# audioTrack is successfully initialized!");
        }

        private void work() {
        	int minBufferSize = getMinBufferSize() ;

            InputStream in = null;
            try {
                in = stream.getInputStream();

                byte data[] = new byte[minBufferSize];

                int len;
                while(running && ((len = in.read(data)) != -1)) {
                	audioTrack.write(data, 0, len);

//                    stream.updatePosition(data.length);
//
//                    LoggerFactory.obtainLogger(TAG).d("work# played data.length = " + data.length);
                }
                
                sendMsgPaused();
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        e(e.getMessage(), e);

                sendMsgError();
            } finally {
                stream.finalizePosition();
                stream.finalizeDuration();

                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        LoggerFactory.obtainLogger(TAG).
                                e(e.getMessage(), e);
                    }
                }
            }
        }

        private void close() {
            stream.close();

//            audioTrack.pause();
//
//            long delta = DurationUtils.secondsToBytes(0.1);
//            position += delta;
//
//            sendMsgInProgress();
//
////            audioTrack.flush();

            audioTrack.stop();
            audioTrack.release();
        }

        void pausePlaying() {
            running = false;
        }
    }
    
    public static int getMinBufferSize() {
    	if(SEProjectEngine.fileFormat == FILE_FORMAT_SPEEX) {
    		return MIN_BUFFER_SIZE * 12;
    	} else {
    		return MIN_BUFFER_SIZE * MULT;
    	}
    	
    }

    interface SESoundPlayerStateListener {
        void onPlayingStarted(int position, int duration);
        void onPlayingInProgress(int position, int duration);
        void onPlayingPaused(int position, int duration);
        void onPlayingError(int position, int duration);
    }
}
