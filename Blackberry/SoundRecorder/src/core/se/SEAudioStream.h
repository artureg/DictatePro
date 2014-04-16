/*
 * SEAudioStream.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEAUDIOSTREAM_H_
#define SEAUDIOSTREAM_H_

#include <QObject>

#define READ  0
#define WRITE 1

namespace bb {
namespace cascades {

/**
 * Representation of a stream
 */
class SEAudioStream : public QObject {

	Q_OBJECT

public:

	int mode;
	SEAudioStream();
	virtual ~SEAudioStream();

	/**
	 * Opens the stream
	 */
	virtual void open(int mode);

	/**
	 * Closes the stream
	 */
	virtual void close();

	/**
	 * Clears the stream
	 */
	virtual void clear();

	/**
	 * Writes the data to the end of the stream
	 *
	 * @param data data to be appended
	 */
	virtual void write(char data[]);

	/**
	 * Reads data from the stream
	 *
	 * @param data data to be read
	 * @param position position to start reading from
	 * @param duration duration of the data to be read
	 */
	virtual void read(char data[], double position, double duration) ;

	int getMode();
};

} /* namespace cascades */
} /* namespace bb */
#endif /* SEAUDIOSTREAM_H_ */
