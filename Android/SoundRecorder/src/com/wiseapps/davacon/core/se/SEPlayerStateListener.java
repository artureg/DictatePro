package com.wiseapps.davacon.core.se;

/**
 * Interface all interested in a playback status parts should implement.
 *
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:59 AM
 */
public interface SEPlayerStateListener {

    /**
     * Notifies that playback has started.
     */
    void playingStarted(int position, int duration);

    /**
     * Notifies that playback has been paused.
     */
    void playingPaused(int position, int duration);

    /**
     * Notifies that playback is in progress.
     */
    void playingInProgress(int position, int duration);

    /**
     * Notifies that playback has stopped.
     */
    void playingStopped(int position, int duration);

    /**
     * Notifies that during playback some kind of error has occured.
     *
     * @param errorMessage error message
     */
    void onError(int position, int duration, String errorMessage);
}
