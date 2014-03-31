package com.wiseapps.davacon.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/23/14
 *         Time: 10:21 AM
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    public static final String TMP_SUFFIX = "-tmp";

    private static final String APP_PATH = "Android/data/";

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

    public static String getFilename(Context context) {
        return getFilename(context, false);
    }

    public static String getTempFilename(Context context) {
        return getFilename(context, true);
    }

    private static String getFilename(Context context, boolean temp) {
        if (context == null) {
            throw new IllegalArgumentException();
        }

        return getRoot(context).getAbsolutePath() + "/" + System.currentTimeMillis() +
                (temp ? TMP_SUFFIX : "") + ".wav";
    }
}

