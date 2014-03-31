package com.wiseapps.davacon.utils;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/26/14
 *         Time: 6:34 PM
 */
public class DurationUtils {

    public static double format(int millis) {
        return Double.parseDouble((millis / 1000) + "." + String.valueOf((millis % 1000)).substring(0, 1));
    }
}
