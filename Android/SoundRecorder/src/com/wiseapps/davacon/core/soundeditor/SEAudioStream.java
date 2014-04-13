package com.wiseapps.davacon.core.soundeditor;

/**
 * Abstract representation of a stream
 */
public abstract class SEAudioStream {

	/** Audio Duration */
	private long duration;

	/** File Path */
	private String filePathURL;

	/** Last Error */
	private int error;

	/** Stream Length in bytes */
	private int length;

	/** Number of samples including all channels */
	private int numberOfSamples;

	/** Number of samples per channel */
	private int numberOfSamplesPerChannel;
	
	public SEAudioStream() {
		
		// TODO Auto-generated constructor stub
		
	}
	
//	/* Create stream in memory */
//	- (id)init;
//
//	/* Load from server */
//	- (id)initWithURL:(NSString*)url;
//
//	/* Load from Storage */
//	- (id)initWithContentsOfFile:(NSString*)file;

	/** 
	 * Open Stream 
	 */
	protected void open() {};

	/** 
	 * Close Stream 
	 */
	protected void close() {}

	/** 
	 * Delete all information in stream 
	 */
	protected void clear() {};

	/**
	 * Seek to position in samples include all channels
	 * @param position
	 */
	protected void seekToSamplePosition( int position ) {}

	/**
	 * Seek to second
	 * @param second
	 */
	protected void seekToSecond( int second ) {}

	/**
	 * Write Samples using byte array
	 * @param samples
	 */
	protected void writeSamples( byte[] samples ) {}

	/**
	 * Write Samples using data
	 * @param data
	 * @param count
	 */
	protected void writeSamples(byte[] data, int count) {} 

	/**
	 * Read Samples from All Channels
	 * @param count
	 * @return byte[]
	 */
	protected byte[] readSamplesWithCount( int count ) {
		return null;
	};

	/**
	 * Read Samples from one channel
	 * @param channels
	 * @param count
	 * @return byte[]
	 */
	protected byte[] readSamplesFromChannel( int channels, int count ) {
		return null;
	};

	/**
	 * Read Samples data
	 */
	protected void readSamples( byte[] samples,  int count ) {}

}
