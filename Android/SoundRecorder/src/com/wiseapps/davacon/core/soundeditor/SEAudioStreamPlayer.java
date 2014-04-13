package com.wiseapps.davacon.core.soundeditor;

/**
 * This class provides audio streaming (playing) capabilities of a record. </br>
 * While streaming (playing) a record this class provides possibility to publish its progress 
 * via an object of  SEAudioStreamPlayerDelegate class. </br>
 * The data to be played is represented via an object of the SEAudioStream class
 */
public class SEAudioStreamPlayer {
	
	private SEAudioStreamPlayerDelegate delegate;
	
	/* Check if stream is playing */
	private boolean isPlaying;

	/* Check if stream is paused */
	private boolean isPaused;

	/* Current Time of audio track */
	private int currentTime;

	public SEAudioStreamPlayer() {
		
		// TODO Auto-generated constructor stub
		
	}
	
	/**
	 * Load audio stream to player 
	 * @param stream
	 */
	public void initWithStream( SEAudioStream stream ) {}

	/**
	 * Start playing
	 */
	public void start() {}

	/**
	 * Pause on current position
	 */
	public void pause() {}

	/**
	 * Stop playing and seek to start
	 */
	public void stop() {}
	
	public interface SEAudioStreamPlayerDelegate {
		
		/** Notification for begin playing */
		public void audioStreamPlayerDidStartPlaying( SEAudioStreamPlayer player );

		/** Notification for pause playing */
		public void audioStreamPlayerDidPause( SEAudioStreamPlayer player );

		/** Notification for continue playing after pause */
		public void audioStreamPlayerDidContinu( SEAudioStreamPlayer player );

		/** Notification for updating info about play state */
		public void audioStreamPlayer( SEAudioStreamPlayer player, long position, long duration );

		/** Notification for end playing */
		public void audioStreamPlayerDidFinishPlaying( SEAudioStreamPlayer player, boolean stopped );

	
	}

}
