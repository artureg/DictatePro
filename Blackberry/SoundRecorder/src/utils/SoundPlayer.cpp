/*
 * SoundPlayer.cpp
 *
 *  Created on: 04.04.2014
 *      Author: Timofey Kovalenko
 */

#include "SoundPlayer.h"
#include <sys/stat.h> // S_IRUSR

#include <bb/cascades/Application>



SoundPlayer::SoundPlayer():
			isReady(false)
{

}

SoundPlayer::~SoundPlayer()
{
	detach();

}

void SoundPlayer::init(QString path)
{
	mp = new bb::multimedia::MediaPlayer();
	mp->setSourceUrl("file://" + path);


//	QByteArray bstr = path.toLocal8Bit();
//	const char* file = bstr.data();
//
//	connection = mmr_connect(NULL);
//	if( !connection ) {
//		return;
//	}
//
//	context = mmr_context_create(connection, CONTEXT_NAME, 0, S_IRUSR);
//	if( !context ) {
//		return;
//	}
//
//	int audio_oid = mmr_output_attach(context, AUDIO_OUT, "audio");
////	mmr_output_parameters(context, audio_oid, NULL);AUDIO_OUT
//
//	mmr_input_attach(context, file, INPUT_TYPE);
//
	isReady = true;

}

void SoundPlayer::detach() {

//	if( connection ) {
//		if( context ) {
//			mmr_input_detach(context);
//			mmr_context_destroy(context);
//		}
//
//		mmr_disconnect(connection);
//	}
	isReady = false;
}

/**
 * Play the track
 */
bool SoundPlayer::play() {

//	if( isReady && mmr_play(context) == 0 ) {
//		return true;
//	}
//	return false;
	mp->play();
//	mp->reset();

	return true;
}

/**
 * Pause the player
 */
bool SoundPlayer::pause() {
//	if( isReady && mmr_stop(context) == 0 ) {
//		return true;
//	}
//	return false;

	mp->pause();
	return true;
}

/**
 * Stop the player
 */
bool SoundPlayer::stop() {
//	if( isReady && mmr_stop(context) == 0 ) {
//		return true;
//	}
//	return false;

	mp->stop();
	return true;
}

/**
 * Set track position
 */
void SoundPlayer::setPosition(long time) {

	mp->seekTime(time);
}

long SoundPlayer::getPosition() {

	return mp->position();
}

long SoundPlayer::getDuration() {

	return mp->duration();
}

