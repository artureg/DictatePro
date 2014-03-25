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

    private static final String APP_PATH = "Android/data/";

    public static String getFilename(Context context) {
        File root = new File(Environment.getExternalStorageDirectory(),
                APP_PATH + context.getApplicationContext().getPackageName());

        if (!root.exists()) {
            root.mkdirs();
        }

        return root.getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav";
    }
}

