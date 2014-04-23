package com.wiseapps.davacon.speex;

import java.io.IOException;
import java.io.InputStream;

import com.wiseapps.davacon.logging.LoggerFactory;

import android.util.Log;

/**
 * InputStream wrapper for native methods
 * @author Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */
public class NativeInputStream extends InputStream {
	
	static {
        System.loadLibrary("SpeexLib");
    }
	
	private long nativeObject;
	private String filePath;
	private int format;
	
	private native long open(String filePath, int format);
	private native int close(long nativeID, int format);
	private native byte[] read(long nativeId, int offset, int duration, int format);
	
	public NativeInputStream(String filePath, int format) {
		super();
		this.filePath = filePath;
		this.format = format;
		
		nativeObject = open(filePath, format);
		
		Log.e("InputStream open id ", nativeObject + "");
		Log.e("InputStream open filePath ", filePath);
	}
	
	@Override
	public void close() throws IOException {
		Log.e("InputStream close id ", "" + nativeObject);
		this.close(nativeObject, format);
		super.close();
	}

	@Override
	public int read() throws IOException {
		// TODO implement
		return 0;
	}
	
	@Override
	public int read(byte[] buffer) throws IOException {
		// TODO implement
		return 0;
	}
	
	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount)
			throws IOException {
		
		Log.e("InputStream reade !!! nativeObject ", "" + nativeObject);
		Log.e("InputStream reade !!! byteOffset ", "" + byteOffset);
		Log.e("InputStream reade !!! byteCount ", "" + byteCount);
		
		buffer = this.read(nativeObject, byteOffset, byteCount, format);
		
		LoggerFactory.obtainLogger("hhh").
        d("===" +  bytArrayToHex(buffer));
		
		return buffer.length;
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
