/*
 * SERecord.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SERecord.h"

namespace bb {
namespace cascades {

SERecord::SERecord() {
}

SERecord::SERecord(double start, double duration,
		String soundPath) {
	this.start = start;
	this.duration = duration;
	this.soundPath = soundPath;
}

SERecord::~SERecord() {
}

SEAudioStream SERecord::getAudioStream(SEProject project) {
	return new SERecordAudioStream(project);
}

} /* namespace cascades */
} /* namespace bb */


