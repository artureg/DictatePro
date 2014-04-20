package com.wiseapps.davacon.core.se;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:59 AM
 *
 * TODO return playingInProgress method to its prev state (handle double position type)
 */
public interface SEPlayerStateListener {

    /**
     * Notification for begin playing
     */
    void playingStarted();

    /**
     * Notification for pause playing
     */
    void playingPaused();

    /**
     * Notification for continue playing after pause
     */
//    void playingInProgress(double position);
    void playingInProgress(int progress);

    /**
     * Notification for end playing
     */
    void playingStopped();

    void onError(String errorMessage);
}
