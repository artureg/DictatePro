package com.wiseapps.davacon.core.se;

import android.content.Context;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:55 AM
 */
class SERecord {
    long start;
    long duration;
    String soundPath;

    SEAudioStream getAudioStream(SEProject project, Context context) {
//        if (this instanceof SPEEXRecord) {
//            return new SPEEXRecordAudioStream(context);
//        }
//
//        return new PCMRecordAudioStream(context);

        return new SERecordAudioStream(project, context);
    }
}
