#include "SEAudioStreamEngine.h"
#include "SEAudioStream.h"
#include "Internal/SEAudioStreamPlayer.h"
#include "Internal/SEAudioStreamRecorder.h"
#include <QDebug>
#include <QThread>

SEAudioStreamEngine::SEAudioStreamEngine(SEAudioStream* stream, QObject *parent) :
		QObject(parent) {
	this->stream = stream;
    position = 0;
    player = NULL;
    recorder = NULL;
    stream->open(kSEAudioStreamModeRead);
    stream->close();
    state = kSEAudioStreamEngineStateReady;
}

void SEAudioStreamEngine::play() {
    if ((state != kSEAudioStreamEngineStateReady)&&(state != kSEAudioStreamEngineStatePaused)) {
        return;
    }
    state = kSEAudioStreamEngineStatePlaying;
	player = new SEAudioStreamPlayer(stream);
    player->setPosition(position);
    connect(player, SIGNAL(started()), this, SLOT(onPlayBegin()));
    connect(player, SIGNAL(positionChanged(unsigned int)), this, SLOT(onPlayUpdate(unsigned int)));
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
    player->stop();
    player = NULL;
}

void SEAudioStreamEngine::record() {
    if (state != kSEAudioStreamEngineStateReady) {
        return;
    }
    state = kSEAudioStreamEngineStateRecording;
    recorder = new SEAudioStreamRecorder(this->stream, this);
    connect(recorder, SIGNAL(started()), this, SLOT(onRecordBegin()));
    connect(recorder, SIGNAL(finished()), this, SLOT(onRecordStop()));
    connect(recorder, SIGNAL(positionChanged(unsigned int)), this, SLOT(onRecordUpdate(unsigned int)));
    connect(recorder, SIGNAL(errorOccurred(QString)), this, SLOT(onError(QString)));
    recorder->start();
}

void SEAudioStreamEngine::stop() {
    switch (state) {
    case kSEAudioStreamEngineStatePlaying:
        if (player) {
            player->stop();
            player = NULL;
        }
        break;
    case kSEAudioStreamEngineStateRecording:
        if (recorder) {
            if (recorder->isRunning()) {
                recorder->stop();
                recorder = NULL;
            }
        }
        break;
    default:
        break;
    }
    position = 0;
    state = kSEAudioStreamEngineStateReady;
}

double SEAudioStreamEngine::getDuration() {
    return stream->getDuration()/1000.0f;
}

void SEAudioStreamEngine::setCurrentPosition(double position) {
    if (state == kSEAudioStreamEngineStateRecording) {
        return;
    }
    this->position = position*1000;
    if (this->position < 0) {
        this->position = 0;
    }
    if (this->position > stream->getDuration()) {
        this->position = stream->getDuration();
        stop();
    }
    if (player) {
        player->setPosition(this->position);
    }
    emit updatePlaying(this->position / 1000.0f);
}

double SEAudioStreamEngine::getCurrentPosition() {
    return this->position/1000.0f;
}

SEAudioStream* SEAudioStreamEngine::getStream() {
    return stream;
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
    if (state != kSEAudioStreamEngineStatePaused) {
        state = kSEAudioStreamEngineStateReady;
        emit stopPlaying();
    } else {
        position = 0;
    }
}

void SEAudioStreamEngine::onPlayUpdate(unsigned int time) {
    position = time;
	emit updatePlaying(time / 1000.0f);
}

void SEAudioStreamEngine::onRecordBegin() {
	emit startRecording();
}

void SEAudioStreamEngine::onRecordStop() {
	emit stopRecording();
}

void SEAudioStreamEngine::onRecordUpdate(unsigned int time) {
	emit updateRecording(time / 1000.0f);
}

void SEAudioStreamEngine::onError(QString error) {
	emit errorOccurred(error);
}

