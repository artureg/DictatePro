package com.wiseapps.davacon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.wiseapps.davacon.ProcessTrackActivity;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 12:13 PM
 */
public class ActivityNavigator {

    private static final String BUNDLE = "bundle";

    public static void startProcessTrackActivity(Context context) {
        startProcessTrackActivity(context, null);
    }

    public static void startProcessTrackActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ProcessTrackActivity.class);

        if (bundle != null) {
            intent.putExtra(BUNDLE, bundle);
        }

        context.startActivity(intent);
    }
}
