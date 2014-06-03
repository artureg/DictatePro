/*
 * SEProjectEngine.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SEProjectEngine.h"
#include "utils/Loger.h"

SEProjectEngine::SEProjectEngine() : soundPlayer(NULL) {}

SEProjectEngine::~SEProjectEngine() {}

void SEProjectEngine::initWithProject(SEProject &project) {

	this->project = &project;
	state = stateReady;
}

/**
 * method to return the directory path where
 * temp recorded are stored.
 */
static char* projectLocation()
{
	QString workingDir = QDir::currentPath();
    QByteArray bstrTemp = QString(workingDir +  "/data/devacon/rec_spx.wav").toLocal8Bit();
	char* path = bstrTemp.data();

	return path;
}

void SEProjectEngine::startPlaying() {

	Loger::Debug(typeid(this).name(), "startPlaying");

	if(soundPlayer == NULL) {
		Loger::Debug(typeid(this).name(), "soundPlayer == NULL");
	} else {
		Loger::Debug(typeid(this).name(), "soundPlayer != NULL");
	}

	SERecord *record = new SERecord(); //FIXME remove it, load from xml
	record->soundUrl = projectLocation(); //FIXME
	record->soundRange.start = 0; //FIXME
	record->soundRange.duration = 10; //FIXME


    QThread* thread = new QThread();
	soundPlayer = new SESoundPlayer();
//    soundPlayer->initWithAudioStream(record->audioStream());
	soundPlayer->moveToThread(thread);
    soundPlayer->initWithAudioStream(record->audioStream());

	// start method work when thread is started
	connect(thread, SIGNAL(started()), soundPlayer, SLOT(work()));

	 //automatically delete thread and task object when work is done:
	connect(thread, SIGNAL(finished()), thread, SLOT(deleteLater()));
	connect(thread, SIGNAL(finished()), soundPlayer, SLOT(deleteLater()));
	connect(soundPlayer, SIGNAL(finished()), thread, SLOT(quit()));

	connect(soundPlayer, SIGNAL(finished()), this, SLOT(slotStoped()));
	connect(soundPlayer, SIGNAL(started()), this, SLOT(slotStarted()));
	connect(soundPlayer, SIGNAL(error(const char*)), this, SLOT(slotError(char*)));
	connect(soundPlayer, SIGNAL(positionChanged(unsigned int)), this, SLOT(slotPositionChanged(unsigned int)));

	connect(this, SIGNAL(signalStop()), soundPlayer, SLOT(stop()));//FIXME not work

	state = statePlaying;
	thread->start();
}

void SEProjectEngine::stopPlaying() {
	Loger::Debug(typeid(this).name(), "stopPlaying");
	//emit signalStop();
	soundPlayer->stop();
}

void SEProjectEngine::startRecording() {

	Loger::Debug(typeid(this).name(), "startRecording");

	if(soundRecorder == NULL) {
		Loger::Debug(typeid(this).name(), "soundRecorder == NULL");
	} else {
		Loger::Debug(typeid(this).name(), "soundRecorder != NULL");
	}

	SERecord *record = new SERecord(); //FIXME remove it, load from xml
	record->soundUrl = projectLocation(); //FIXME
	record->soundRange.start = 0; //FIXME
	record->soundRange.duration = 10; //FIXME

	QThread* thread = new QThread;
	soundRecorder = new SESoundRecorder();
	//soundPlayer->initWithAudioStream(record->audioStream());
	soundRecorder->moveToThread(thread);

	// start method work when thread is started
	connect(thread, SIGNAL(started()), soundRecorder, SLOT(work()));

	 //automatically delete thread and task object when work is done:
	connect(thread, SIGNAL(finished()), thread, SLOT(deleteLater()));
	connect(thread, SIGNAL(finished()), soundPlayer, SLOT(deleteLater()));
	connect(soundRecorder, SIGNAL(finished()), thread, SLOT(quit()));

	connect(soundRecorder, SIGNAL(finished()), this, SLOT(slotStoped()));
	connect(soundRecorder, SIGNAL(started()), this, SLOT(slotStarted()));
	connect(soundRecorder, SIGNAL(error(const char*)), this, SLOT(slotError(char*)));
	connect(soundRecorder, SIGNAL(positionChanged(unsigned int)), this, SLOT(slotPositionChanged(unsigned int)));

	connect(this, SIGNAL(signalStop()), soundPlayer, SLOT(stop()));//FIXME not work

	state = stateRecording;
	thread->start();

}

void SEProjectEngine::stopRecording() {
	Loger::Debug(typeid(this).name(), "stopRecording");
	soundRecorder->stop();
}

void SEProjectEngine::setPosition(unsigned int position ) {
	Loger::Debug(typeid(this).name(), "setPosition");
	//TODO implement
}

void SEProjectEngine::slotError(char *msg) {
	Loger::Debug(typeid(this).name(), "slotError");
	state = statePaused;
	state = stateReady;
}

void SEProjectEngine::slotStoped() {
	Loger::Debug(typeid(this).name(), "slotStoped");
	if(state == statePlaying) {
		emit signalPlayingStopped(0, 0);
	} else if (state == stateRecording) {
		emit signalRecordingStopped(0, 0);
	}
	state = statePaused;
	state = stateReady;
}

void SEProjectEngine::slotStarted() {
	Loger::Debug(typeid(this).name(), "slotStarted");
	if(state == statePlaying) {
		emit signalPlayingStarted(0, 0);
	} else if (state == stateRecording) {
		emit signalRecordingStarted(0, 0);
	}
}

void SEProjectEngine::slotPositionChanged(unsigned int position) {

	char c[20];
	sprintf(c, "%d", position);
	Loger::Debug(typeid(this).name(), c);
}
