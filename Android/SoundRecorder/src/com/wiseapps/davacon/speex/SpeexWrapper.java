package com.wiseapps.davacon.speex;

/**
 * Class provides native methods
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/2/14
 *         Time: 11:58 AM
 */
public class SpeexWrapper {

    static {
        System.loadLibrary("SpeexLib");
    }

    /**
     * Returns sample rate of the RIFF/SPEEX file
     *
     * @param compressedFilePathStr path to the RIFF/SPEEX file
     * @return sample rate of the RIFF/SPEEX file or -1 if file doesn't exist
     */
    native public static int getSampleRate(String compressedFilePathStr);

    /**
     * Returns duration of the RIFF/SPEEX file
     *
     * @param compressedFilePathStr path to the RIFF/SPEEX file
     * @return duration of the RIFF/SPEEX file or -1 if file doesn't exist
     */
    native public static long getDuration(String compressedFilePathStr);

    /**
     * Returns array of data of the RIFF/SPEEX file
     *
     * @param compressedFilePathStr path to the RIFF/SPEEX file
     * @param offsetMillis offset in millis to start reading the data from
     * @param durationMillis duration in millis of the read data
     * @return array of data or empty array if file doesn't exist
     */
    native public static byte[] read(String compressedFilePathStr, long offsetMillis, long durationMillis);

    /**
     * Encodes .wav file into RIFF/SPEEX
     *
     * @param wavFilePathStr name of the source .wav file
     * @param compressedFilePathStr name of the dest RIFF/SPEEX file
     * @return error code or 0 if success
     */
    native public static int encode(String wavFilePathStr, String compressedFilePathStr);

    /**
     * Decodes RIFF/SPEEX file to .wav
     *
     * @param compressedFilePathStr name of the source RIFF/SPEEX file
     * @param wavFilePathStr name of the dest .wav file
     * @return error code or 0 if success
     */
    native public static int decode(String compressedFilePathStr, String wavFilePathStr);

    /**
     * For testing purposes only, adds two digits.
     *
     * @param a the first digit to add
     * @param b the second digit to add
     * @return the sum
     */
    native public static int test(int a, int b);
}
