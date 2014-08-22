#ifndef SEAUDIOSTREAMRECORDER_H
#define SEAUDIOSTREAMRECORDER_H

#include <QThread>
#include <sys/asoundlib.h>

class SEAudioStream;

class SEAudioStreamRecorder : public QThread {
    Q_OBJECT
public:
    explicit SEAudioStreamRecorder(SEAudioStream* stream, QObject *parent = 0);

    void run();
    void stop();

private:
    SEAudioStream* stream;

    snd_pcm_t*                  pcm_handle;
    snd_pcm_info_t              info;
    snd_pcm_channel_params_t    channel_params;
    snd_pcm_channel_setup_t     channel_setup;
    snd_pcm_channel_info_t      channel_info;
    snd_mixer_t*                mixer_handle;
    snd_mixer_group_t           group;
    int                         card;

    bool is_running;

    /** general setup of the libasound audio mixer and pcm components */
    bool setup_snd();

    /** close pcm components */
    bool close_snd();

public slots:

Q_SIGNALS:

    /** signal is raised when the player has been terminated */
    void started();

    /** signal is raised when the player has been terminated */
    void finished();

    /** signal is raised when an error occurred */
    void errorOccurred(QString);

    /** signal is raised when an position is changed */
    void positionChanged(unsigned int);
};

#endif // SEAUDIOSTREAMRECORDER_H
