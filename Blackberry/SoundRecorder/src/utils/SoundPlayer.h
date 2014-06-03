/*
 * SoundPlayer.h
 *
 *  Created on: 04.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SOUNDPLAYER_H_
#define SOUNDPLAYER_H_

#include <stdio.h>
#include <unistd.h>
#include <mm/renderer.h>
#include <sys/asoundlib.h>

#include <bb/multimedia/MediaPlayer>

#define CONTEXT_NAME "wise-apps-player"
#define AUDIO_OUT "audio:default"
#define INPUT_TYPE "track"

#define SUCCESS  0
#define FAILURE -1

class SoundPlayer : public QObject {

//Q_OBJECT

private:
	bool isReady;
	mmr_connection_t *connection;
	mmr_context_t *context;
	bb::multimedia::MediaPlayer *mp;



public:
	SoundPlayer();
	virtual ~SoundPlayer();
	void init(QString path);
	void detach();
	bool play();
	bool pause();
	bool stop();
    void setPosition(long time);
    long getPosition();
    long getDuration();



signals:
    void finished();
};

#endif /* SOUNDPLAYER_H_ */
