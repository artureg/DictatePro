package com.wiseapps.davacon.utils;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/26/14
 *         Time: 6:34 PM
 */
public class DurationUtils {

    public static double format(int millis) {
        return format((double) millis);
    }

    public static double format(double millis) {
        double seconds = millis / 1000;
        return (double) Math.round(seconds * 10) / 10;
    }
}
