package com.wiseapps.davacon.utils;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/20/14
 *         Time: 9:04 AM
 */
public class DurationUtils {

    public static long bytesFromMillis(long millis) {
        return millis / (SAMPLE_RATE_IN_HZ * CHANNEL_CONFIG_IN * BITS_PER_SAMPLE / 8);
    }

    public static long millisFromBytes(long bytes) {
        return bytes * (SAMPLE_RATE_IN_HZ * CHANNEL_CONFIG_IN * BITS_PER_SAMPLE / 8);
    }
}
