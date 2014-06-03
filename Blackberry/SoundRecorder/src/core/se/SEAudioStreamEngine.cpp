/*
 * SEAudioStreamEngine.cpp
 *
 *  Created on: 21.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SEAudioStreamEngine.h"
#include "utils/Loger.h"

SEAudioStreamEngine::SEAudioStreamEngine() :
											state(stateNotReady),
											currentTimeInMillisecond(0),
											durationInMillisecond(0)
{}

SEAudioStreamEngine::~SEAudioStreamEngine() {}

void SEAudioStreamEngine::startPlaying() {
	Loger::Debug(typeid(this).name(), "startPlaying");
}

void SEAudioStreamEngine::stopPlaying() {
	Loger::Debug(typeid(this).name(), "stopPlaying");
}

void SEAudioStreamEngine::startRecording() {
	Loger::Debug(typeid(this).name(), "startRecording");
}

void SEAudioStreamEngine::stopRecording() {
	Loger::Debug(typeid(this).name(), "stopRecording");
}

void SEAudioStreamEngine::setPosition(unsigned int position) {
	Loger::Debug(typeid(this).name(), "setPosition");
}
