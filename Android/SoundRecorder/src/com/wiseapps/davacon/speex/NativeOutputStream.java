package com.wiseapps.davacon.speex;

import java.io.IOException;
import java.io.OutputStream;

import com.wiseapps.davacon.logging.LoggerFactory;

/**
 * OutputStream wrapper for native methods
 * @author Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */
public class NativeOutputStream extends OutputStream {
	
	private static final String TAG = NativeOutputStream.class.getSimpleName();

	static {
        System.loadLibrary("SpeexLib");
    }
	
	private long nativeObject;
	private String filePath;
	private int format;
	
	private native long open(String filePath, int format, int sampleRate, int bitsPerSample, int channel);
	private native long close(long nativeID);
	private native int write(long nativeID, byte[] data);
	
	public NativeOutputStream(String filePath, int format) {
		super();
		this.filePath = filePath;
		this.format = format;
		
		nativeObject = open(filePath, format, 8000, 16, 1); 
	}
	
	public NativeOutputStream(String filePath, int format, int sampleRate, int bitsPerSample, int channel) {
		super();
		this.filePath = filePath;
		this.format = format;
		
		nativeObject = this.open(filePath, format, sampleRate, bitsPerSample, channel);
	}
	
	@Override
	public void close() throws IOException {
		LoggerFactory.obtainLogger(TAG).d("close " + nativeObject);
		this.close(nativeObject);
		super.close();
	}

	@Override
	public void write(int oneByte) throws IOException {
		
		byte[] buf = new byte[]{(byte)oneByte};
		int	result = this.write(nativeObject, buf);
		if(result == -1) {
			throw new IOException("Error occurred during writing");
		}

	}

	@Override
	public void write(byte[] buffer) throws IOException {
		
		
		//LoggerFactory.obtainLogger(TAG).d("write()  = bytes =" + bytArrayToHex(buffer));
		
		int	result = this.write(nativeObject, buffer);
		if(result == -1) {
			throw new IOException("Error occurred during writing");
		}

	}
	
	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {

		if(count < 1) {
			throw new IllegalArgumentException("Illegal argumant - count");
		}
		
//		LoggerFactory.obtainLogger(TAG).d("INPUT offset =" + offset);
//		LoggerFactory.obtainLogger(TAG).d("INPUT count =" + count);
		
		byte[] buf = new byte[count];
		System.arraycopy(buffer, offset, buf, 0, count);
		int	result = this.write(nativeObject, buffer);
		if(result == -1) {
			throw new IOException("Error occurred during writing");
		}
		
	}
	
	String bytArrayToHex(byte[] a) {
 	   StringBuilder sb = new StringBuilder();
 	   for(byte b: a) {
 	      sb.append(String.format("%02x", b&0xff));
 	      sb.append(" ");
 	   }
 	   return sb.toString();
 	}

}
