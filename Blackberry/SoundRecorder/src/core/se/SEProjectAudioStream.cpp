/*
 * SEProjectAudioStream.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SEProjectAudioStream.h"

namespace bb {
namespace cascades {

SEProjectAudioStream::SEProjectAudioStream(SEProject project) {
	this.project = project;
}

SEProjectAudioStream::~SEProjectAudioStream() {
}

SEProjectAudioStream SEProjectAudioStream::initialize(
		QList<SERecord> records) {
	this.records = records;
	return this;
}

void SEProjectAudioStream::open(int mode) {
	this->mode = mode;
}

void SEProjectAudioStream::close() {
}

void SEProjectAudioStream::clear() {
	 records.clear();
}

void SEProjectAudioStream::write(char* data[]) {
}

void SEProjectAudioStream::read(char* data[], double position,
		double duration) {

	 if (records == NULL) {
		return;
	}
	char* data[];
	SEAudioStream stream;
	for (SERecord record : records) {
		stream = record.getAudioStream(project);
		stream.open(Mode.READ);

		if (true) {
			stream.read(data, position, duration);
		}
	}

}

int SEProjectAudioStream::getMode() {
	return mode;
}

} /* namespace cascades */
} /* namespace bb */


