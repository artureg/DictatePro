package com.wiseapps.davacon.core.se;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 12:02 PM
 *
 * TODO return recordingInProgress method to its prev state (handle double position type)
 */
public interface SERecorderStateListener {

    /**
     * Notification for begin recording
     */
    void recordingStarted();

    /**
     * Notification for update recording info
     */
//    void recordingInProgress(double position);
    void recordingInProgress(int position);

    /**
     * Notification for end recording
     */
    void recordingStopped();

    void onError(String errorMessage);
}
