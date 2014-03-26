package com.wiseapps.davacon;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.core.CheapWAV;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.IOException;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/26/14
 *         Time: 6:11 PM
 */
abstract class PlayingCapableActivity extends Activity {

    private static final int MSG_PLAYER_STARTED = 0;
    private static final int MSG_PLAYER_IN_PROGRESS = 1;
    private static final int MSG_PLAYER_PAUSED = 2;

    private StateAwareMediaPlayer mPlayer;

    private CheapWAV wav;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_PLAYER_IN_PROGRESS: {
                    onPlayerInProgress(mPlayer.getCurrentPosition(), mPlayer.getDuration());

                    sendMessageDelayed(
                            obtainMessage(MSG_PLAYER_IN_PROGRESS), 100);

                    break;
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        wav = getWav();

        if (wav != null) {
            preparePlaying();
        }
    }

    @Override
    protected void onStop() {
        mHandler.removeMessages(MSG_PLAYER_IN_PROGRESS);

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        super.onStop();
    }

    void doPlay() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();

            onPlayerPaused();
        } else {
            mPlayer.seekTo(mPlayer.getCurrentPosition());
            mPlayer.start();

            onPlayerStarted();
        }
    }

    private void preparePlaying() {
        try {
            mPlayer = new StateAwareMediaPlayer();

            mPlayer.setDataSource(wav.file.getAbsolutePath());

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // Update the UI correspondently,
                    // i.e. show the play button and the track's duration
                    onPlayerPreparedSuccessfully(mPlayer.getDuration());

                }
            });

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mPlayer.release();
                    mPlayer = null;

                    mHandler.removeMessages(MSG_PLAYER_IN_PROGRESS);

                    preparePlaying();
                }
            });

            mPlayer.prepare();
        } catch (Exception e) {
            LoggerFactory.obtainLogger(getTag()).e(e.getMessage(), e);

            if (mPlayer != null) {
                if (mPlayer.isPrepared()) {
                    mPlayer.release();
                }
                mPlayer = null;
            }

            // Udate the UI correspondently,
            // i.e. make sure neither play button or track's duration are shown
            onPlayerPreparationFailed();
        }
    }

    void onPlayerPreparedSuccessfully(int duration) {
    }

    void onPlayerPreparationFailed() {
    }

    void onPlayerStarted() {
        mHandler.sendEmptyMessage(MSG_PLAYER_IN_PROGRESS);
    }

    void onPlayerInProgress(int currentPosition, int duration) {
    }

    void onPlayerPaused() {
        mHandler.removeMessages(MSG_PLAYER_IN_PROGRESS);
    }

    /**
     *
     * @return media player current position
     */
    int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    abstract CheapWAV getWav();

    private String getTag() {
        return getClass().getSimpleName();
    }

    private class StateAwareMediaPlayer extends MediaPlayer {
        private boolean prepared;

        @Override
        public void prepare() throws IOException, IllegalStateException {
            super.prepare();

            prepared = true;
        }

        @Override
        public void prepareAsync() throws IllegalStateException {
            super.prepareAsync();

            prepared = true;
        }

        public boolean isPrepared() {
            return prepared;
        }
    }
}
