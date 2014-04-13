package com.wiseapps.davacon.core.soundeditor;
/**
 * Stream with record 
 */
class SERecordAudioStream extends SEAudioStream {

	public SERecordAudioStream() {
		// TODO Auto-generated constructor stub
	}
	
	private SERecordAudioStreamDelegate delegate;

	/** Pointer to record */
	private SERecord record;

	/** Initialize stream with record */
	private void initWithRecord( SERecord record ){};

	/** Start recording sound */
	private void startRecording(){};

	/** Stop recording sound */
	private void stopRecording(){};

	/**
	 * Helper class to provide information of audio streaming (playing) progress of a record
	 */
	interface SERecordAudioStreamDelegate {
		
		/** Notification for begin recording */
		void recordAudioStreamDidStartRecording( SERecordAudioStream stream );

		/** Notification for update recording info */
		void recordAudioStream( SERecordAudioStream stream, long duration);

		/** Notification for end recording */
		void recordAudioStreamDidFinishRecording( SERecordAudioStream stream );
	}

}








