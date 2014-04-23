package com.wiseapps.davacon.speex;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream wrapper for native methods
 * @author Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */
public class NativeOutputStream extends OutputStream {

	static {
        System.loadLibrary("SpeexLib");
    }
	
	private long nativeObject;
	private String filePath;
	private int format;
	
	private native long open(String filePath, int format, int sampleRate, int bitsPerSample, int channel);
	private native long close(long nativeID, int format);
	private native int write(long nativeID, byte[] data, int format);
	
	public NativeOutputStream(String filePath, int format) {
		super();
		this.filePath = filePath;
		this.format = format;
		
		nativeObject = open(filePath, format, 8000, 16, 1); // FIXME 
	}
	
	public NativeOutputStream(String filePath, int format, int sampleRate, int bitsPerSample, int channel) {
		super();
		this.filePath = filePath;
		this.format = format;
		
		nativeObject = this.open(filePath, format, sampleRate, bitsPerSample, channel);
	}
	
	@Override
	public void close() throws IOException {
		this.close(nativeObject, format);
		super.close();
	}

	@Override
	public void write(int oneByte) throws IOException {
		
		byte[] buf = new byte[]{(byte)oneByte};
		int	result = this.write(nativeObject, buf, format);
		if(result == -1) {
			throw new IOException("Error occurred during writing");
		}

	}

	@Override
	public void write(byte[] buffer) throws IOException {
		
		int	result = this.write(nativeObject, buffer, format);
		if(result == -1) {
			throw new IOException("Error occurred during writing");
		}

	}
	
	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {

		if(count < 1) {
			throw new IllegalArgumentException("Illegal argumant - count");
		}
		
		byte[] buf = new byte[count];
		System.arraycopy(buffer, offset, buf, 0, count);
		int	result = this.write(nativeObject, buffer, format);
		if(result == -1) {
			throw new IOException("Error occurred during writing");
		}
		
	}

}
