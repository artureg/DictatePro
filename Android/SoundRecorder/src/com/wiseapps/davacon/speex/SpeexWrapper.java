package com.wiseapps.davacon.speex;

/**
 * Class provides native methods
 *
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/2/14
 *         Time: 11:58 AM
 */
public class SpeexWrapper {

    static {
        System.loadLibrary("SpeexLib");
    }
    
    public static NativeInputStream getInputStream(String filePath, int format) {
    	NativeInputStream steram = new NativeInputStream(filePath, format);
//    	steram.initNative();
    	return steram;
    }
    
    public static NativeOutputStream getOutputStream(String filePath, int format, int sampleRate, int bitsPerSample, int channel) {
    	NativeOutputStream stream = new NativeOutputStream(filePath, format, sampleRate, bitsPerSample, channel);
//    	steram.initNative();
    	return stream;
    }

//    /**
//     * Returns format of the file.
//     *
//     * @param filePath path to the file
//     * @return file format, 0 for standard wav, 1 for speex
//     */
//    native public static int getFormat(String filePath);
//
//    /**
//     * Returns sample rate of the file.
//     *
//     * @param filePath path to the file
//     * @param format file format
//     * @return sample rate of the file or -1 if file doesn't exist
//     */
//    native public static int getSampleRate(String filePath, int format);
//
//    /**
//     * Returns duration of the file.
//     *
//     * @param filePath path to the file
//     * @param format file format
//     * @return duration in seconds of the file or -1 if file doesn't exist
//     */
//    native public static double getDuration(String filePath, int format);
//
//    /**
//     * Reads data from the file starting from offset to (offset + duration).
//     *
//     * @param filePath path to the file to read data from
//     * @param offset offset in seconds to start reading the data from
//     * @param duration duration in seconds of the data
//     * @param format file format
//     * @return data
//     */
//    native public static byte[] read(String filePath, double offset, double duration, int format);
//
//    /**
//     * Writes data to the end of the file.
//     *
//     * @param filePath path to the file to write data from
//     * @param data data to be written
//     * @param format file format
//     * @return code of the operation, if success return 0
//     */
//    native public static int write(String filePath, byte[] data, int format);
//
//    /**
//     * Encodes .wav file into RIFF/SPEEX
//     *
//     * @param wavFilePathStr name of the source .wav file
//     * @param compressedFilePathStr name of the dest RIFF/SPEEX file
//     * @return error code or 0 if success
//     */
//    native public static int encode(String wavFilePathStr, String compressedFilePathStr);
//
//    /**
//     * Decodes RIFF/SPEEX file to .wav
//     *
//     * @param compressedFilePathStr name of the source RIFF/SPEEX file
//     * @param wavFilePathStr name of the dest .wav file
//     * @return error code or 0 if success
//     */
//    native public static int decode(String compressedFilePathStr, String wavFilePathStr);
//
//    /**
//     * For testing purposes only, adds two digits.
//     *
//     * @param a the first digit to add
//     * @param b the second digit to add
//     * @return the sum
//     */
//    native public static int test(int a, int b);
//
////    /**
////     * Returns format of the file.
////     *
////     * @param filePath path to the file
////     * @return file format, 0 for standard wav, 1 for speex
////     */
////    public static int getFormat(String filePath) {
////        return MockSpeexWrapper.getFormat(filePath);
////    }
////
////    /**
////     * Returns sample rate of the file.
////     *
////     * @param filePath path to the file
////     * @param format file format
////     * @return sample rate of the file or -1 if file doesn't exist
////     */
////    public static int getSampleRate(String filePath, int format) {
////        return MockSpeexWrapper.getSampleRate(filePath, format);
////    }
////
//////    /**
//////     * Returns duration of the file.
//////     *
//////     * @param filePath path to the file
//////     * @param format file format
//////     * @return duration in seconds of the file or -1 if file doesn't exist
//////     */
//////    public static double getDuration(String filePath, int format) {
//////        return MockSpeexWrapper.getDuration(filePath, format);
//////    }
////
////    /**
////     * Reads data from the file starting from offset to (offset + duration).
////     *
////     * @param filePath path to the file to read data from
////     * @param offset offset in seconds to start reading the data from
////     * @param duration duration in seconds of the data
////     * @param format file format
////     * @return data
////     */
////    public static byte[] read(String filePath, double offset, double duration, int format) {
////        return MockSpeexWrapper.read(filePath, offset, duration, format);
////    }
////
////    /**
////     * Writes data to the end of the file.
////     *
////     * @param filePath path to the file to write data from
////     * @param data data to be written
////     * @param format file format
////     * @return code of the operation, if success return 0
////     */
////    public static int write(String filePath, byte[] data, int format) {
////        return MockSpeexWrapper.write(filePath, data, format);
////    }
////
    /**
     * Encodes .wav file into RIFF/SPEEX
     *
     * @param wavFilePathStr name of the source .wav file
     * @param compressedFilePathStr name of the dest RIFF/SPEEX file
     * @return error code or 0 if success
     */
    public static native int encode(String wavFilePathStr, String compressedFilePathStr);

    /**
     * Decodes RIFF/SPEEX file to .wav
     *
     * @param compressedFilePathStr name of the source RIFF/SPEEX file
     * @param wavFilePathStr name of the dest .wav file
     * @return error code or 0 if success
     */
    public static native int decode(String compressedFilePathStr, String wavFilePathStr);
////
////    /**
////     * For testing purposes only, adds two digits.
////     *
////     * @param a the first digit to add
////     * @param b the second digit to add
////     * @return the sum
////     */
////    public static int test(int a, int b);
}
