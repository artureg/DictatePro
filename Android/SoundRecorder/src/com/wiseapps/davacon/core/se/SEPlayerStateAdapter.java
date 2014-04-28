package com.wiseapps.davacon.core.se;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/24/14
 *         Time: 12:48 PM
 */
public class SEPlayerStateAdapter implements SEPlayerStateListener {
    @Override
    public void playingStarted(int position, int duration) {
    }

    @Override
    public void playingPaused(int position, int duration) {
    }

    @Override
    public void playingInProgress(int position, int duration) {
    }

    @Override
    public void playingStopped(int position, int duration) {
    }

    @Override
    public void onError(int position, int duration, String errorMessage) {
    }
}
