package com.wiseapps.davacon.core.se;

import android.media.AudioTrack;

import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.NativeInputStream;

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

    private PlayingThread thread;

    private final AudioStream stream;

    private List<SESoundPlayerStateListener> listeners = new ArrayList<SESoundPlayerStateListener>();

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

    void addHandler(SESoundPlayerStateListener listener) {
        listeners.add(listener);
    }

    void removeHandler(SESoundPlayerStateListener listener) {
        listeners.remove(listener);
    }

    private void sendMsgStarted() {
        for (SESoundPlayerStateListener listener : listeners) {
            if (listener != null) {
                listener.onPlayingStarted();
            }
        }
    }

    private void sendMsgPaused() {
        for (SESoundPlayerStateListener listener : listeners) {
            if (listener != null) {
                listener.onPlayingPaused();
            }
        }
    }

    private void sendMsgError() {
        for (SESoundPlayerStateListener listener : listeners) {
            if (listener != null) {
                listener.onPlayingError();
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
//            int minBufferSize = MIN_BUFFER_SIZE * MULT;
//            audioTrack = new AudioTrack(STREAM_TYPE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT,
//                    minBufferSize, MODE);
//           
//            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
//                sendMsgError();
//                return;
//            }

            if(!openAudioTrack(SAMPLE_RATE_IN_HZ)) return;
            
            stream.open(AudioStream.Mode.READ);
            sendMsgStarted();

            audioTrack.play();
        }
        
        private boolean openAudioTrack(int sampleRate) {
        	
//        	int minBufferSize = MIN_BUFFER_SIZE * MULT;
        	int minBufferSize;
        	if(FILE_FORMAT == FILE_FORMAT_SPEEX) {
        		minBufferSize = MIN_BUFFER_SIZE * 6;
        	} else {
        		minBufferSize = MIN_BUFFER_SIZE * MULT;
        	}
        	audioTrack = new AudioTrack(STREAM_TYPE, (int)sampleRate, CHANNEL_CONFIG_OUT, AUDIO_FORMAT, minBufferSize, MODE);
            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                sendMsgError();
                return false;
            }

            audioTrack.play();
            return true;
        }

        private void work() {
        	int minBufferSize;
        	if(FILE_FORMAT == FILE_FORMAT_SPEEX) {
        		minBufferSize = MIN_BUFFER_SIZE * 6;
        	} else {
        		minBufferSize = MIN_BUFFER_SIZE * MULT;
        	}

            InputStream in = null;

            try {
                in = stream.getInputStream();
                
                int sampleRate = NativeInputStream.getSampleRate();
                if(sampleRate != SAMPLE_RATE_IN_HZ) {
                	if(!openAudioTrack(sampleRate)) {
                		return;
                	}
                }

                byte data[] = new byte[minBufferSize];

                long time = System.currentTimeMillis();
                long time_max = 0;
                int len = 0;
                while(running && ((len = in.read(data)) != -1)) {
                	audioTrack.write(data, 0, len);

                    LoggerFactory.obtainLogger(TAG).
                            d("run# played " + data.length);

                    stream.updatePosition(data.length);
                    stream.updateDuration(data.length);
                    
                    long time_cur = System.currentTimeMillis();
                    LoggerFactory.obtainLogger(TAG).
                    d("PLAYE read TIME SPENT =  " + (time_cur - time));
                    if((time_cur - time) > time_max) {
                    	time_max = time_cur - time;
                    }
                    
                }
                
                LoggerFactory.obtainLogger(TAG).
                d("PLAYE read TIME MAX SPENT =  " + time_max);
                

                sendMsgPaused();
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        d("work# catch");
                LoggerFactory.obtainLogger(TAG).
                        e(e.getMessage(), e);

                sendMsgError();
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
        }

        private void close() {
            stream.close();

            audioTrack.stop();
            audioTrack.release();
        }

        void pausePlaying() {
            running = false;
        }
    }

    interface SESoundPlayerStateListener {
        void onPlayingStarted();
        void onPlayingPaused();
        void onPlayingError();
    }
}
