package com.wiseapps.davacon.speex;

import java.io.IOException;
import java.io.InputStream;

import com.wiseapps.davacon.logging.LoggerFactory;

/**
 * InputStream wrapper for native methods
 * @author Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */
public class NativeInputStream extends InputStream {
	
	private static final String TAG = NativeInputStream.class.getSimpleName();
	
	static {
        System.loadLibrary("SpeexLib");
    }
	
	private long nativeObject;
	private String filePath;
	private int format;
	
	private native long open(String filePath, int format);
	private native int close(long nativeID);
	private native byte[] readOne(long nativeId, int length);
	private native byte[] read(long nativeId, int offset, int duration);
	private native byte[] skip(long nativeId, long byteCount);
	
	public NativeInputStream(String filePath, int format) {
		super();
		this.filePath = filePath;
		this.format = format;
		
		nativeObject = open(filePath, format);
		
		LoggerFactory.obtainLogger(TAG).d("open " + nativeObject);
		
	}
	
	@Override
	public void close() throws IOException {
		LoggerFactory.obtainLogger(TAG).d("close " + nativeObject);
		this.close(nativeObject);
		super.close();
	}

	@Override
	public int read() throws IOException {
		byte[] bufferT = new byte[1];
		read(bufferT);
		return bufferT[0];
	}
	
	@Override
	public int read(byte[] buffer) throws IOException {
//		LoggerFactory.obtainLogger(TAG).d("read()  nativeObject" + nativeObject);
		
		byte[] bufferT = this.readOne(nativeObject, buffer.length);
		System.arraycopy(bufferT, 0, buffer, 0, bufferT.length);
		
//		LoggerFactory.obtainLogger(TAG).d("read()   buffer.length =" + buffer.length);
//		LoggerFactory.obtainLogger(TAG).d("read()  = bytes =" + bytArrayToHex(buffer));
		
		return buffer.length;
	}
	
	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount)
			throws IOException {
		
//		LoggerFactory.obtainLogger(TAG).d("read(...)  nativeObject" + nativeObject);
//		
//		LoggerFactory.obtainLogger(TAG).d("read(...)  byteOffset = " + byteOffset);
//		LoggerFactory.obtainLogger(TAG).d("read(...)   byteCount =" + byteCount);
		 
		byte[] bufferT = this.read(nativeObject, byteOffset, byteCount);
		System.arraycopy(bufferT, 0, buffer, 0, bufferT.length);
		
//		LoggerFactory.obtainLogger(TAG).d("read(...)   buffer.length =" + buffer.length);
//		LoggerFactory.obtainLogger(TAG).d("read(...)  = bytes =" + bytArrayToHex(buffer));
		
		return buffer.length;
	}
	
	public long skip(long byteCount) throws IOException {
		
		LoggerFactory.obtainLogger(TAG).d("INPUT skip =" + byteCount);
		skip(nativeObject, byteCount);
		return byteCount;

	};
	
	String bytArrayToHex(byte[] a) {
 	   StringBuilder sb = new StringBuilder();
 	   for(byte b: a) {
 	      sb.append(String.format("%02x", b&0xff));
 	      sb.append(" ");
 	   }
 	   return sb.toString();
 	}
	
}
