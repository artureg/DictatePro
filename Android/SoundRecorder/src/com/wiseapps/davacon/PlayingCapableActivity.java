package com.wiseapps.davacon;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.IOException;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/26/14
 *         Time: 6:11 PM
 */
abstract class PlayingCapableActivity extends Activity {

    private static final int MSG_PLAYER_IN_PROGRESS = 1;

    private StateAwareMediaPlayer mPlayer;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_PLAYER_IN_PROGRESS: {
                    onPlayerInProgress(mPlayer.getCurrentPosition());

                    sendMessageDelayed(
                            obtainMessage(MSG_PLAYER_IN_PROGRESS), 100);

                    break;
                }
            }
        }
    };

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
        if (mPlayer == null) {
            preparePlaying();
        }

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
        SoundFile sf = getSoundFile();
        if (sf == null) {
            LoggerFactory.obtainLogger(getTag()).
                    d("preparePlaying# No file to play");
            return;
        }

        try {
            mPlayer = new StateAwareMediaPlayer();

            mPlayer.setDataSource(sf.getFile().getAbsolutePath());

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // Update the UI correspondently,
                    // i.e. show the play button and the track's duration
                    onPlayerPreparedSuccessfully();

                }
            });

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mPlayer.release();
                    mPlayer = null;

                    onPlayerCompleted();

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

    void onPlayerPreparedSuccessfully() {
    }

    void onPlayerPreparationFailed() {
    }

    void onPlayerStarted() {
        mHandler.sendEmptyMessage(MSG_PLAYER_IN_PROGRESS);
    }

    void onPlayerInProgress(int currentPosition) {
    }

    void onPlayerPaused() {
        mHandler.removeMessages(MSG_PLAYER_IN_PROGRESS);
    }

    void onPlayerCompleted() {
        mHandler.removeMessages(MSG_PLAYER_IN_PROGRESS);
    }

    /**
     *
     * @return media player current position
     */
    int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    abstract SoundFile getSoundFile();

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
