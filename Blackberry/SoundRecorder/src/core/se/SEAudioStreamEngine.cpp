/*
 * SEAudioStreamEngine.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SEAudioStreamEngine.h"

namespace bb {
namespace cascades {

SEAudioStreamEngine::SEAudioStreamEngine() {}

SEAudioStreamEngine::~SEAudioStreamEngine() {}

void SEAudioStreamEngine::startPlaying() {
}

void SEAudioStreamEngine::pausePlaying() {
}

void SEAudioStreamEngine::stopPlaying() {
}

void SEAudioStreamEngine::startRecording() {
}

void SEAudioStreamEngine::stopRecording() {
}

void SEAudioStreamEngine::setCurrentTime(double currentTime) {
}

int SEAudioStreamEngine::getState() {
}

double SEAudioStreamEngine::getCurrentTime() {
}

void SEAudioStreamEngine::notifyPlayerStateChanged(int event) {

	emit playerStateChanged(event);
}

void SEAudioStreamEngine::notifyRecorderStateChanged(int event) {

	emit recorderStateChanged(event);
}

} /* namespace cascades */
} /* namespace bb */
