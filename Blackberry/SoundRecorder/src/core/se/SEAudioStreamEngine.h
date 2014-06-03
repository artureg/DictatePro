/*
 * SEAudioStreamEngine.h
 *
 *  Created on: 21.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEAUDIOSTREAMENGINE_H_
#define SEAUDIOSTREAMENGINE_H_

#include <qobject.h>
#include "SEAudioStream.h"

class SEAudioStreamEngine: public QObject {

	Q_OBJECT

public:

	typedef enum {
		stateNotReady,
		stateReady,
		statePlaying,
		statePaused,
		stateRecording,
	} SEAudioStreamEngineState;

	SEAudioStreamEngine();
	virtual ~SEAudioStreamEngine();

	/** current state*/
	SEAudioStreamEngineState state;

	/** audio stream*/
	SEAudioStream *audioStream;

	/** start player*/
	virtual void startPlaying();

	/** stop player*/
	virtual void stopPlaying();

	/** start recorder*/
	virtual void startRecording();

	/** stop recorder*/
	virtual void stopRecording();

	/** set position*/
	virtual void setPosition(unsigned int position);

	/** Current Time of audio track */
	unsigned int currentTimeInMillisecond;

	/** Track duration */
	unsigned int durationInMillisecond;

Q_SIGNALS:

	/** signal is raised when the recorder progress has been changed */
	void signalRecordingInProgress(unsigned int position, unsigned int duration);

	/** signal is raised when the recorder has been started */
	void signalRecordingStarted(unsigned int position, unsigned int duration);

	/** signal is raised when the recorder has been stopped */
	void signalRecordingStopped(unsigned int position, unsigned int duration);

	/** signal is raised when the player progress has been changed */
	void signalPlayingInProgress(unsigned int position, unsigned int duration);

	/** signal is raised when the player has been started */
	void signalPlayingStarted(unsigned int position, unsigned int duration);

	/** signal is raised when the player has been stopped */
	void signalPlayingStopped(unsigned int position, unsigned int duration);

	/** signal is raised when an error has been occurred */
	void signalError(unsigned int position, unsigned int duration, QString errorMessage);

};

#endif /* SEAUDIOSTREAMENGINE_H_ */
