package com.wiseapps.davacon.core.soundeditor;

/**
 * This class is a helper class that represents a sub-record
 */
public class SERecord {

	 /** Sound start position */
    private long start; 

    /** Sound duration from start position */
    private long duration;    

//	/** Pointer to parent project */
//	@property(nonatomic,weak) SEProject* project;

	/** URL for source sound location */
	private String soundURL;

	/** Range in sound for current record */
//	private SERecordSoundRange soundRange;

    final SEProject project;

	/** Record audio stream */
	private final SERecordAudioStream audioStream;

	public SERecord(SEProject project) {
        this.project = project;

        audioStream = new SERecordAudioStream();
	}

	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}

	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getSoundURL() {
		return soundURL;
	}
	public void setSoundURL(String soundURL) {
		this.soundURL = soundURL;
	}

    public SERecordAudioStream getAudioStream() {
        return audioStream;
    }
}
