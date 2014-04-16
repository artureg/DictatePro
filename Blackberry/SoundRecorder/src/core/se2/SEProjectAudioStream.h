/*
 * SEProjectAudioStream.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEPROJECTAUDIOSTREAM_H_
#define SEPROJECTAUDIOSTREAM_H_

#include "SEAudioStream.h"

namespace bb {
namespace cascades {

/**
 * Inherited from the SEAudioStream class concrete representation of an audio stream
 */
class SEProjectAudioStream: public SEAudioStream {

	Q_OBJECT

private:
	SEProject project;
	QList<SERecord> records;

protected:
	SEProjectAudioStream initialize(QList<SERecord> records);

public:
	SEProjectAudioStream(SEProject project);
	virtual ~SEProjectAudioStream();

	/**
	 * Opens the stream
	 */
	void open(int mode);

	/**
	 * Closes the stream
	 */
	void close();

	/**
	 * Clears the stream
	 */
	void clear();

	/**
	 * Writes the data to the end of the stream
	 * @param data data to be appended
	 */
	 void write(char* data[]);

	/**
	 * Reads data from the stream
	 * @param data data to be read
	 * @param position position to start reading from
	 * @param duration duration of the data to be read
	 */
	void read(char* data[], double position, double duration) ;

	int getMode();

};

} /* namespace cascades */
} /* namespace bb */
#endif /* SEPROJECTAUDIOSTREAM_H_ */
