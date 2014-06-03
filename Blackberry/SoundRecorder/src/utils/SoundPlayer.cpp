/*
 * SoundPlayer.cpp
 *
 *  Created on: 04.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
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

	isReady = true;

}

void SoundPlayer::detach() {

	isReady = false;
}

/**
 * Play the track
 */
bool SoundPlayer::play() {

	return true;
}

/**
 * Pause the player
 */
bool SoundPlayer::pause() {

	mp->pause();
	return true;
}

/**
 * Stop the player
 */
bool SoundPlayer::stop() {

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











