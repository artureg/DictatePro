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
    void playingStarted();

    /**
     * Notifies that playback has been paused.
     */
    void playingPaused();

    /**
     * Notifies that playback is in progress.
     *
     * @param progress playback progress in bytes
     */
    void playingInProgress(int progress);

    /**
     * Notifies that playback has stopped.
     */
    void playingStopped();

    /**
     * Notifies that during playback some kind of error has occured.
     *
     * @param errorMessage error message
     */
    void onError(String errorMessage);
}
