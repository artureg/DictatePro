/*
 * SEProjectEngine.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEPROJECTENGINE_H_
#define SEPROJECTENGINE_H_

#include <qobject.h>

#define SAMPLE_RATE_IN_HZ 8000;
#define CHANNEL_CONFIG 1;
#define AUDIO_FORMAT 16;

namespace bb {
namespace cascades {

class SEProjectEngine: public SEProjectAudioStream {
	Q_OBJECT

public:
	SEProjectEngine(SEProject project);
	virtual ~SEProjectEngine();

private:
	SEProject project;

// TODO implementation
//	SESoundRecorder recorder;
//	SESoundPlayer player;
public:
	/**
	 * Plays the stream.
	 * Playing is stream-based. In case the stream is encoded to format we can't play as is,
	 * it is decoded with the native library method.
	 */
	void startPlaying();

	/**
	 * Pauses stream playing
	 */
	void pausePlaying();

	/**
	 * Stops stream playing
	 */
	void stopPlaying();

	/**
	 * Starts recording.
	 * Recording is stream-based. In case the stream should be recorder in some specified format,
	 * it is encoded with the native library method.
	 */
	void startRecording();

	/**
	 * Stops recording
	 */
	void stopRecording();

	@Override
	public void setCurrentTime(double currentTime);

	@Override
	public double getCurrentTime();

};

} /* namespace cascades */
} /* namespace bb */
#endif /* SEPROJECTENGINE_H_ */
