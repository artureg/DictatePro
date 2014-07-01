#include "SEAudioStreamEngine.h"
#include "SEAudioStream.h"
#include "Internal/SEAudioStreamPlayer.h"
#include <QDebug>
#include <QThread>

SEAudioStreamEngine::SEAudioStreamEngine(SEAudioStream* stream, QObject *parent) :
		QObject(parent) {
	this->stream = stream;
    state = kSEAudioStreamEngineStateReady;
}

void SEAudioStreamEngine::play() {
    if (state != kSEAudioStreamEngineStateReady) {
        return;
    }
    state = kSEAudioStreamEngineStatePlaying;
	player = new SEAudioStreamPlayer(stream);
    connect(player, SIGNAL(started()), this, SLOT(onPlayBegin()));
    connect(player, SIGNAL(positionChanged(uint)), this, SLOT(onPlayUpdate(uint)));
    connect(player, SIGNAL(finished()), this, SLOT(onPlayStop()));
    connect(player, SIGNAL(errorOccurred(QString)), this, SLOT(onError(QString)));
	player->start();
}

TSEAudioStreamEngineState SEAudioStreamEngine::getState() {
    return state;
}

void SEAudioStreamEngine::pause() {
    if (state != kSEAudioStreamEngineStatePlaying) {
        return;
    }
    state = kSEAudioStreamEngineStatePaused;
}

void SEAudioStreamEngine::record() {
    if (state != kSEAudioStreamEngineStateReady) {
        return;
    }
    state = kSEAudioStreamEngineStateRecording;
}

void SEAudioStreamEngine::stop() {
    switch (state) {
    case kSEAudioStreamEngineStatePlaying:
        player->terminate();
        break;
    default:
        break;
    }
    state = kSEAudioStreamEngineStateReady;
}

double SEAudioStreamEngine::getDuration() {
    return stream->getDuration()/1000.0f;
}

void SEAudioStreamEngine::setCurrentPosition(double position) {
    if (state == kSEAudioStreamEngineStateRecording) {
        return;
    }
    player->setPosition(position*1000);
}

double SEAudioStreamEngine::getCurrentPosition() {
    return player->getPosition()/1000.0f;
}

void SEAudioStreamEngine::onPlayBegin() {
	emit startPlaying();
}

void SEAudioStreamEngine::onPlayPause() {
	emit pausePlaying();
}

void SEAudioStreamEngine::onPlayContinue() {
	emit continuePlaying();
}

void SEAudioStreamEngine::onPlayStop() {
    state = kSEAudioStreamEngineStateReady;
	emit stopPlaying();
}

void SEAudioStreamEngine::onPlayUpdate(uint time) {
	emit updatePlaying(time / 1000.0f);
}

void SEAudioStreamEngine::onRecordBegin() {
	emit startRecording();
}

void SEAudioStreamEngine::onRecordStop() {
	emit stopRecording();
}

void SEAudioStreamEngine::onRecordUpdate(int time) {
	emit updateRecording(time / 1000.0f);
}

void SEAudioStreamEngine::onError(QString error) {
	emit errorOccurred(error);
}

