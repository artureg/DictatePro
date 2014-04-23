package com.wiseapps.davacon.core.se;

/**
 * Interface all interested in a recording status parts should implement.
 *
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 12:02 PM
 */
public interface SERecorderStateListener {

    /**
     * Notifies that recording has started.
     */
    void recordingStarted();

    /**
     * Notifies that recording is in progress.
     *
     * @param progress recording progress in bytes
     */
    void recordingInProgress(int progress);

    /**
     * Notifies that recording has stopped.
     */
    void recordingStopped();

    /**
     * Notifies that during recording some kind of error has occured.
     *
     * @param errorMessage error message
     */
    void onError(String errorMessage);
}
