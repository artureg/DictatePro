package com.wiseapps.davacon.core.soundeditor;

/**
 * This class provides audio streaming (playing) capabilities of a record. </br>
 * While streaming (playing) a record this class provides possibility to publish its progress 
 * via an object of  SEAudioStreamPlayerDelegate class. </br>
 * The data to be played is represented via an object of the SEAudioStream class
 */
public class SEAudioStreamPlayer {
	
	private SEPlayerStateListener listener;

    public static enum State {
        PLAYING, PAUSED, STOPPED
    }
	
	private State state;

	/* Current Time of audio track */
	private int currentTime;

    private SEAudioStream stream;

	public SEAudioStreamPlayer() {
        state = State.STOPPED;
	}

    /**
     * Load audio stream to player
     *
     * @param stream
     */
    public void initWithStream(SEAudioStream stream) {
        this.stream = stream;
    }

    /**
	 * Start playing
	 */
	public void start() {
//        stream.open();
//
//        stream.seekToSecond(currentTime);

//        stream.readSamples();
    }

	/**
	 * Pause on current position
	 */
	public void pause() {
    }

	/**
	 * Stop playing and seek to start
	 */
	public void stop() {
    }

    public State getState() {
        return state;
    }

    public void setListener(SEPlayerStateListener listener) {
        this.listener = listener;
    }
}
