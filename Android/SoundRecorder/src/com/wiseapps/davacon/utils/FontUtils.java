package com.wiseapps.davacon.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 12:26 PM
 */
public class FontUtils {
    private static Typeface robotoMedium;
    private static Typeface robotoRegular;

    public static Typeface getRobotoMedium(Context context) {
        if (robotoMedium == null) {
            robotoMedium = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
        }

        return robotoMedium;
    }

    public static Typeface getRobotoRegular(Context context) {
        if (robotoRegular == null) {
            robotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        }

        return robotoRegular;
    }
}
