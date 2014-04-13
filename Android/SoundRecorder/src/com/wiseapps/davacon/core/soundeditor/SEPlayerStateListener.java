package com.wiseapps.davacon.core.soundeditor;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/13/14
 *         Time: 3:20 PM
 */
public interface SEPlayerStateListener {

    /** Notification for begin playing */
    public void audioStreamPlayerDidStartPlaying( SEAudioStreamPlayer player );

    /** Notification for pause playing */
    public void audioStreamPlayerDidPause( SEAudioStreamPlayer player );

    /** Notification for continue playing after pause */
    public void audioStreamPlayerDidContinue( SEAudioStreamPlayer player );

    /** Notification for updating info about play state */
    public void audioStreamPlayer( SEAudioStreamPlayer player, long position, long duration );

    /** Notification for end playing */
    public void audioStreamPlayerDidFinishPlaying( SEAudioStreamPlayer player, boolean stopped );
}
