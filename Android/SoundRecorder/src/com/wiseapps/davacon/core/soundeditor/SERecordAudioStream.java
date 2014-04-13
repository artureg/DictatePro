package com.wiseapps.davacon.core.soundeditor;
/**
 * Stream with record 
 */
public class SERecordAudioStream extends SEAudioStream {

    private SERecorderStateListener listener;

	public SERecordAudioStream() {
	}

	/** Start recording sound */
	public void startRecording() {

    }

    public boolean isRecording() {
        // TODO implement with SoundRecorder and output to file
        return true;
    }

	/** Stop recording sound */
	public void stopRecording() {

//        record.project.addRecord(record);
    }

    public void setListener(SERecorderStateListener listener) {
        this.listener = listener;
    }
}








