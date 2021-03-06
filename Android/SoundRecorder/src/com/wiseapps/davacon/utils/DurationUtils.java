package com.wiseapps.davacon.utils;

import com.wiseapps.davacon.core.se.SEProjectEngine;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/20/14
 *         Time: 9:04 AM
 */
public class DurationUtils {

    public static double secondsFromBytes(long bytes) {
        double seconds = ((double) bytes) / (double) (SEProjectEngine.sampleRate * NUM_CHANNELS * BITS_PER_SAMPLE / 8);
        return (double) (Math.round(seconds * 10)) / 10;
    }

    public static long secondsToBytes(double seconds) {
        return Math.round(seconds * (SEProjectEngine.sampleRate * NUM_CHANNELS * BITS_PER_SAMPLE / 8));
    }
}
