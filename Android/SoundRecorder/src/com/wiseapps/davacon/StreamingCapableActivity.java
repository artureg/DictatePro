package com.wiseapps.davacon;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.FileInputStream;

import static com.wiseapps.davacon.core.wav.WAVFile.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/8/14
 *         Time: 10:31 AM
 */
abstract class StreamingCapableActivity extends Activity {
    private static final String TAG = StreamingCapableActivity.class.getSimpleName();

    private static final int MSG_PLAYER_IN_PROGRESS = 1;

    final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    final int SAMPLE_RATE_IN_HZ = 8000;
    final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    final int MODE = AudioTrack.MODE_STREAM;

    private AudioTrack track;

    private PlayTask mTask;

    long offsetMillis;
    long durationMillis;

    @Override
    public void onBackPressed() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    void doPlay() {
        if (track == null) {
            preparePlaying();

            mTask = new PlayTask();
            mTask.execute();
        }

        if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mTask.pause();

            onPlayerPaused();
        } else {
            mTask.resume();

            onPlayerStarted();
        }
    }

    private void preparePlaying() {
        int minBuffSize = RECORDER_BUFFER_SIZE_IN_BYTES;

        track = new AudioTrack(STREAM_TYPE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT,
                minBuffSize, MODE);

        if (track.getState() != AudioTrack.STATE_INITIALIZED) {
            onPlayerPreparationFailed();
            return;
        }

        offsetMillis = 0;
        durationMillis = getSoundFile().getDuration(minBuffSize);

        onPlayerPreparedSuccessfully();
    }

    /**
     * Callback method to update the screens data and UI after the media player has been prepared.
     * <p>Should be overriden by a subclass.</p>
     */
    void onPlayerPreparedSuccessfully() {
    }

    /**
     * Callback method to update the screens data and UI in case an error occured
     * during the media player preparation.
     * <p>Should be overriden by a subclass.</p>
     */
    void onPlayerPreparationFailed() {
    }

    /**
     * Callback method to update the screens data and UI after the track playback has started.
     * <p>Should be overriden by a subclass.</p>
     */
    void onPlayerStarted() {
        mHandler.sendEmptyMessage(MSG_PLAYER_IN_PROGRESS);
    }

    /**
     * Callback method to update the screens data and UI after the track playback is in progress.
     * <p>Should be overriden by a subclass.</p>
     */
    void onPlayerInProgress(int currentPosition) {
    }

    /**
     * Callback method to update the screens data and UI after the track playback has been paused.
     * <p>Should be overriden by a subclass.</p>
     */
    void onPlayerPaused() {
        mHandler.removeMessages(MSG_PLAYER_IN_PROGRESS);
    }

    /**
     * Callback method to update the screens data and UI after the track playback has completed.
     * <p>Should be overriden by a subclass.</p>
     */
    void onPlayerCompleted() {
        mHandler.removeMessages(MSG_PLAYER_IN_PROGRESS);
    }

    /**
     * Callback method to set the track to play.
     * <p>Should be overriden by a subclass.</p>
     */
    abstract SoundFile getSoundFile();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_PLAYER_IN_PROGRESS: {
                    onPlayerInProgress((int) offsetMillis);

                    sendMessageDelayed(
                            obtainMessage(MSG_PLAYER_IN_PROGRESS), 100);

                    break;
                }
            }
        }
    };

    private class PlayTask extends AsyncTask<Void, Integer, Void> {
        private boolean isPaused = false;

        private FileInputStream in;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                LoggerFactory.obtainLogger(TAG).d("PlayTask.doInBackground# started");

                byte data[] = new byte[RECORDER_BUFFER_SIZE_IN_BYTES / 2];

                if (in == null) {
                    in = new FileInputStream(getSoundFile().getFile());
                }

                while (!isPaused && in.read(data) > -1) {
                    LoggerFactory.obtainLogger(TAG).
                            d("doPlay# offsetMillis = " + offsetMillis);
                    LoggerFactory.obtainLogger(TAG).
                            d("doPlay# durationMillis = " + durationMillis);

                    track.write(data, 0, data.length);
                    track.play();

                    offsetMillis += durationMillis;
                    durationMillis = getSoundFile().getDuration(RECORDER_BUFFER_SIZE_IN_BYTES / 2);
                }

                track.release();
                track = null;

//                LoggerFactory.obtainLogger(TAG).d("PlayTask.doInBackground# started");
//
//                int minBuffSize = RECORDER_BUFFER_SIZE_IN_BYTES;
//
//                byte data[] = new byte[minBuffSize / 2];
//
//                offsetMillis = 0;
//                durationMillis = getSoundFile().getDuration(minBuffSize / 2);
//
//                in = new FileInputStream(getSoundFile().getFile());
//
//                while (!isPaused && in.read(data) > -1) {
//                    LoggerFactory.obtainLogger(TAG).
//                            d("doPlay# offsetMillis = " + offsetMillis);
//                    LoggerFactory.obtainLogger(TAG).
//                            d("doPlay# durationMillis = " + durationMillis);
//
//                    track.write(data, 0, data.length);
//                    track.play();
//
//                    offsetMillis += durationMillis;
//                    durationMillis = getSoundFile().getDuration(minBuffSize / 2);
//                }
//
//                track.release();
//                track = null;
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        e("doPlay# ", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        LoggerFactory.obtainLogger(TAG).
                                e("doPlay# ", e);
                    }
                }
            }

            LoggerFactory.obtainLogger(TAG).d("PlayTask.doInBackground# finished");
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (track != null) {
                track.stop();
                track.release();
                track = null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            onPlayerCompleted();
        }

        void pause() {
            isPaused = true;
        }

        void resume() {
            isPaused = false;
        }
    }

//        if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
//            onPlayerPaused();
//
//            track.pause();
//        } else {
//            onPlayerStarted();
//
//            FileInputStream in = null;
//
//            try {
//                int minBuffSize = RECORDER_BUFFER_SIZE_IN_BYTES;
//
//                byte data[] = new byte[minBuffSize / 2];
//
//                in = new FileInputStream(getSoundFile().getFile());
//                while (in.read(data) > -1) {
//                    LoggerFactory.obtainLogger(TAG).
//                            d("doPlay# offsetMillis = " + offsetMillis);
//                    LoggerFactory.obtainLogger(TAG).
//                            d("doPlay# durationMillis = " + durationMillis);
//
//                    track.write(data, 0, data.length);
//                    track.play();
//
//                    offsetMillis += durationMillis;
//                    durationMillis = getSoundFile().getDuration(minBuffSize / 2);
//                }
//
//                track.release();
//                track = null;
//
//                onPlayerCompleted();
//            } catch (Exception e) {
//                LoggerFactory.obtainLogger(TAG).
//                        e("doPlay# ", e);
//            } finally {
//                if (in != null) {
//                    try {
//                        in.close();
//                    } catch (Exception e) {
//                        LoggerFactory.obtainLogger(TAG).
//                                e("doPlay# ", e);
//                    }
//                }
//            }
//        }
}
