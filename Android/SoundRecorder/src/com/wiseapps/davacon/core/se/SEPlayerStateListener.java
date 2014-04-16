package com.wiseapps.davacon.core.se;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:59 AM
 *
 * Set of public methods could not be changed!!!
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
    void playingInProgress(double position);

    /**
     * Notification for end playing
     */
    void playingStopped();

    void onError(String errorMessage);
}
