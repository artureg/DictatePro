package com.wiseapps.davacon.core.se;

import java.util.ArrayList;
import java.util.List;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 1:16 PM
 *
 * Set of public methods could not be changed!!!
 */
public abstract class SEAudioStreamEngine {

    public static enum State {
        READY,
        PLAYING_IN_PROGRESS,
        RECORDING_IN_PROGRESS,
    }

    static enum Event {
        PLAYING_STARTED,
        PLAYING_PAUSED,
        PLAYING_IN_PROGRESS,
        PLAYING_STOPPED,
        RECORDING_STARTED,
        RECORDING_IN_PROGRESS,
        RECORDING_STOPPED,
        OPERATION_ERROR
    }

    State state = State.READY;

    private List<SEPlayerStateListener> playerStateListeners;
    private List<SERecorderStateListener> recorderStateListeners;

    protected SEAudioStreamEngine() {
        playerStateListeners = new ArrayList<SEPlayerStateListener>();
        recorderStateListeners = new ArrayList<SERecorderStateListener>();
    }

    abstract public void startPlaying();

    abstract public void pausePlaying();

    abstract public void stopPlaying();

    abstract public void startRecording();

    abstract public void stopRecording();

    public State getState() {
        return state;
    }

    /**
     * @param currentTime current time in seconds
     */
    abstract public void setCurrentTime(double currentTime);

    abstract public double getCurrentTime();

    public void addPlayerStateListener(SEPlayerStateListener listener) {
        playerStateListeners.add(listener);
    }

    public void addRecorderStateListener(SERecorderStateListener listener) {
        recorderStateListeners.add(listener);
    }

    protected void notifyPlayerStateChanged(Event event) {
        if (playerStateListeners == null) {
            return;
        }

        switch (event) {
            case PLAYING_STARTED: {
                for (SEPlayerStateListener listener : playerStateListeners) {
                    if (listener != null) {
                        listener.playingStarted();
                    }
                }
                break;
            }
            case PLAYING_PAUSED: {
                for (SEPlayerStateListener listener : playerStateListeners) {
                    if (listener != null) {
                        listener.playingPaused();
                    }
                }
                break;
            }
            case PLAYING_IN_PROGRESS: {
                for (SEPlayerStateListener listener : playerStateListeners) {
                    if (listener != null) {
                        listener.playingInProgress(getCurrentTime());
                    }
                }
                break;
            }
            case PLAYING_STOPPED: {
                for (SEPlayerStateListener listener : playerStateListeners) {
                    if (listener != null) {
                        listener.playingStopped();
                    }
                }
                break;
            }
            default: {
                for (SEPlayerStateListener listener : playerStateListeners) {
                    if (listener != null) {
                        listener.onError("Playing failed");
                    }
                }
            }
        }
    }

    protected void notifyRecorderStateChanged(Event event) {
        if (recorderStateListeners == null) {
            return;
        }

        switch (event) {
            case RECORDING_STARTED: {
                for (SERecorderStateListener listener : recorderStateListeners) {
                    if (listener != null) {
                        listener.recordingStarted();
                    }
                }
                break;
            }
            case RECORDING_IN_PROGRESS: {
                for (SERecorderStateListener listener : recorderStateListeners) {
                    if (listener != null) {
                        listener.recordingInProgress(getCurrentTime());
                    }
                }
                break;
            }
            case RECORDING_STOPPED: {
                for (SERecorderStateListener listener : recorderStateListeners) {
                    if (listener != null) {
                        listener.recordingStopped();
                    }
                }
                break;
            }
            default: {
                for (SERecorderStateListener listener : recorderStateListeners) {
                    if (listener != null) {
                        listener.onError("Recording failed");
                    }
                }
            }
        }
    }
}
