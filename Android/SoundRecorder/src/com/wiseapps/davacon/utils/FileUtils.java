package com.wiseapps.davacon.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Helper class to provide file related methods.
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/23/14
 *         Time: 10:21 AM
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    public static final String TMP_SUFFIX = "-tmp";
    public static final String SPEEX_SUFFIX = "-speex";

    private static final String APP_PATH = "Android/data/";

    /**
     * Method to return the root of the tracks hierarchy.
     *
     * @param context Application context
     * @return root of the tracks hierarchy
     */
    public static File getRoot(Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }

        File root = new File(Environment.getExternalStorageDirectory(),
                APP_PATH + context.getApplicationContext().getPackageName());

        if (!root.exists()) {
            root.mkdirs();
        }

        return root;
    }

    /**
     * Method to return file name.
     *
     * @param context Application context
     * @return Name of the file
     */
    public static String getFilename(Context context) {
        return getFilename(context, String.valueOf(System.currentTimeMillis()));
    }

    /**
     * Method to return temporary file name.
     *
     * @param context Application context
     * @param filename filename to build the resultant name from
     * @return Name of the file
     */
    public static String getTempFilename(Context context, String filename) {
        return getFilename(context, filename + TMP_SUFFIX);
    }

    /**
     * Method to return RIFF/SPEEX file name.
     *
     * @param context Application context
     * @param filename filename to build the resultant name from
     * @return Name of the file
     */
    public static String getSpeexFilename(Context context, String filename) {
        return getFilename(context, filename + SPEEX_SUFFIX);
    }

    /**
     * Method to return file name.
     *
     * @param context Application context
     * @param filename filename to build the resultant name from
     * @return Name of the file
     */
    public static String getFilename(Context context, String filename) {
        if (context == null) {
            throw new IllegalArgumentException();
        }

        return getRoot(context).getAbsolutePath() + "/" + filename + ".wav";
    }
}

