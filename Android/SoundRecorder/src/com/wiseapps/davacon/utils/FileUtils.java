package com.wiseapps.davacon.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.StringTokenizer;

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
        return getFilename(context, String.valueOf(System.currentTimeMillis()) + ".wav");
    }

    /**
     * Method to return temporary file name.
     *
     * @param context Application context
     * @param filename filename to build the resultant name from
     * @return Name of the file
     */
    public static String getTempFilename(Context context, String filename) {
        int idx = filename.lastIndexOf(".");
        return getFilename(context,
                filename.substring(0, idx) + TMP_SUFFIX + filename.substring(idx, filename.length()));
    }

    /**
     * Method to return RIFF/SPEEX file name.
     *
     * @param context Application context
     * @param filename filename to build the resultant name from
     * @return Name of the file
     */
    public static String getSpeexFilename(Context context, String filename) {
        int idx = filename.lastIndexOf(".");
        return getFilename(context,
                filename.substring(0, idx) + SPEEX_SUFFIX + filename.substring(idx, filename.length()));
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

        if (!filename.toLowerCase().endsWith(".wav")) {
            throw new IllegalArgumentException();
        }

        return getRoot(context).getAbsolutePath() + "/" + filename;
    }

    public static String getFilenameFromSpeex(String speexFilename) {
        int idx = speexFilename.indexOf(SPEEX_SUFFIX + ".wav");
        if (idx == -1) {
            return speexFilename;

        }

        return speexFilename.substring(0, idx);
    }
}

