package com.wiseapps.davacon;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.IOException;

/**
 * Activity that provides track playing capabilities.
 *
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

    /**
     * Called when the screen is no longer visible to the user.
     *
     * <p>Releases the media player.</p>
     */
    @Override
    protected void onStop() {
        mHandler.removeMessages(MSG_PLAYER_IN_PROGRESS);

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        super.onStop();
    }

    /**
     * Method that controls track's playback.
     */
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

    /**
     * Helper method to prepare a media player for playback.
     *
     * <p>In accordance of to the media player state the following callback methods are called:
     * <ul>
     *     <li>{@link #onPlayerPreparedSuccessfully}</li>
     *     <li>{@link #onPlayerPreparationFailed}</li>
     *     <li>{@link #onPlayerStarted}</li>
     *     <li>{@link #onPlayerInProgress}</li>
     *     <li>{@link #onPlayerPaused}</li>
     *     <li>{@link #onPlayerCompleted}</li>
     * </ul>
     * </p>
     */
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
     * Helper method to return player current position in millis.
     *
     * @return media Player current position in millis
     */
    int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    /**
     * Callback method to set the track to play.
     * <p>Should be overriden by a subclass.</p>
     */
    abstract SoundFile getSoundFile();

    private String getTag() {
        return getClass().getSimpleName();
    }

    /**
     * Extension of the {@link android.media.MediaPlayer MediaPlayer} class
     * to explicitly handle the prepared state.
     */
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
