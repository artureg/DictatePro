package com.wiseapps.davacon.core.se;

import java.util.ArrayList;
import java.util.List;

/**
 * Objects of this class provide entry point for operations with objects of
 * {@link com.wiseapps.davacon.core.se.SEProject} class.
 *
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 1:16 PM
 */
public abstract class SEAudioStreamEngine {

    /**
     * <p></>Enum to contain engine's states. The states are as follows -
     * <ul>
     *     <li>READY</li>
     *     <li>PLAYING_IN_PROGRESS</li>
     *     <li>RECORDING_IN_PROGRESS</li>
     * </ul>
     * </p>
     *
     * <p>READY state allows any operation - playback, recording, set current position - for the engine to fulfil.</p>
     * <p>PLAYING_IN_PROGRESS state indicates that engine plays the project, no other operation is available.</p>
     * <p>RECORDING_IN_PROGRESS state indicates that engine records the project, no other operation is available.</p>
     */
    public static enum State {
        READY,
        PLAYING_IN_PROGRESS,
        RECORDING_IN_PROGRESS
    }

    public State state = State.READY;

    List<SEPlayerStateListener> playerStateListeners;
    List<SERecorderStateListener> recorderStateListeners;

    protected SEAudioStreamEngine() {
        playerStateListeners = new ArrayList<SEPlayerStateListener>();
        recorderStateListeners = new ArrayList<SERecorderStateListener>();
    }

    /**
     * Starts playing the object of {@link com.wiseapps.davacon.core.se.SEProject} class
     * right from the current position.
     */
    abstract public void startPlaying();

    /**
     * Pauses playing the object of {@link com.wiseapps.davacon.core.se.SEProject} class.
     * Current position remains unchanged.
     */
    abstract public void pausePlaying();

    /**
     * Stops playing the object of {@link com.wiseapps.davacon.core.se.SEProject} class.
     * Current position is reset to 0.
     */
    abstract public void stopPlaying();

    /**
     * Starts recording the object of {@link com.wiseapps.davacon.core.se.SEProject} class
     * right from the current position.
     */
    abstract public void startRecording();

    /**
     * Stops playing the object of {@link com.wiseapps.davacon.core.se.SEProject} class.
     * Current position remains unchanged.
     */
    abstract public void stopRecording();

    /**
     * Returns current state of the engine.
     * @return engine current state
     */
    public State getState() {
        return state;
    }

    /**
     * Sets engine current position.
     * @param currentPosition current position in bytes
     */
    abstract public void setCurrentTime(long currentPosition);

    /**
     * Returns current position of the engine.
     * @return current position in bytes
     */
    abstract public long getCurrentTime();

    /**
     * Returns duration of the object of {@link com.wiseapps.davacon.core.se.SEProject} class the engine is working with.
     * @return duration in bytes
     */
    abstract public long getDuration();

    /**
     * Adds objects of {@link com.wiseapps.davacon.core.se.SEPlayerStateListener} class as playing state listeners.
     * @param listener listener
     */
    public void addPlayerStateListener(SEPlayerStateListener listener) {
        playerStateListeners.add(listener);
    }

    /**
     * Adds objects of {@link com.wiseapps.davacon.core.se.SERecorderStateListener} class as recording state listeners.
     * @param listener listener
     */
    public void addRecorderStateListener(SERecorderStateListener listener) {
        recorderStateListeners.add(listener);
    }
}
