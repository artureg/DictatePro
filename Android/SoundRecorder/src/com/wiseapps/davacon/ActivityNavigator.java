package com.wiseapps.davacon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Helper class to provide navigation between the application screens.
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 12:13 PM
 */
public class ActivityNavigator {

    public static final String BUNDLE = "bundle";

    public static final String EXTRA_TRACK = "track";

    public static void startProcessTrackActivityForResult(Activity activity, int requestCode) {
        startProcessTrackActivityForResult(activity, requestCode, null);
    }

    public static void startProcessTrackActivityForResult(Activity activity, int requestCode, Bundle bundle) {
        Intent intent = new Intent(activity, ProcessTrackActivity.class);

        if (bundle != null) {
            intent.putExtra(BUNDLE, bundle);
        }

        activity.startActivityForResult(intent, requestCode);
    }
}
