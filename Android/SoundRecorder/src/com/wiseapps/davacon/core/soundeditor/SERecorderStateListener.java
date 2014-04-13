package com.wiseapps.davacon.core.soundeditor;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/13/14
 *         Time: 3:20 PM
 */
public interface SERecorderStateListener {

    /** Notification for begin recording */
    void recordingStarted(SERecordAudioStream stream);

    /** Notification for update recording info */
    void dataRecorded(SERecordAudioStream stream, long duration);

    /** Notification for end recording */
    void recordingStopped(SERecordAudioStream stream);

    void errorOccured(String errorMessage);
}
