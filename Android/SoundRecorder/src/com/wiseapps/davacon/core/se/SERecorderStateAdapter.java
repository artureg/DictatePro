package com.wiseapps.davacon.core.se;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/24/14
 *         Time: 12:50 PM
 */
public class SERecorderStateAdapter implements SERecorderStateListener {
    @Override
    public void recordingStarted(int position, int duration) {
    }

    @Override
    public void recordingInProgress(int position, int duration) {
    }

    @Override
    public void recordingStopped(int position, int duration) {
    }

    @Override
    public void onError(int position, int duration, String errorMessage) {
    }
}
