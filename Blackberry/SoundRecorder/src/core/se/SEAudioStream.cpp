/*
 * SEAudioStream.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SEAudioStream.h"
#include "utils/Loger.h"

SEAudioStream::SEAudioStream() : mode(modeUnknown),
                                 format(formatUnknown),
                                 currentPositionMills(0),
                                 startPositionInMiils(0),
                                 durationInMills(0){}

SEAudioStream::~SEAudioStream() {}

//int SEAudioStream::getMode() {
//	Loger::Debug(typeid(this).name(), "getMode");
//	return mode;
//}

bool SEAudioStream::open(SEAudioStreamMode mode) {
	Loger::Debug(typeid(this).name(), "open");
	return false;
}

void SEAudioStream::close() {
	Loger::Debug(typeid(this).name(), "close");
}

bool SEAudioStream::clear() {
	Loger::Debug(typeid(this).name(), "clear");
	return false;
}

bool SEAudioStream::write(char *data) {
	Loger::Debug(typeid(this).name(), "write");
	return false;
}

unsigned int SEAudioStream::read(char *data, unsigned int position, unsigned int duration) {
	Loger::Debug(typeid(this).name(), "read");
	return 0;
}
