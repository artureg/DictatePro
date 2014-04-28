/*
 * SEAudioStreamEngine.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEAUDIOSTREAMENGINE_H_
#define SEAUDIOSTREAMENGINE_H_

#include <QObject>

// States
#define READY 0
#define PLAYING_IN_PROGRESS 1
#define RECORDING_IN_PROGRESS 2

// Events
#define PLAYING_STARTED 3
#define PLAYING_PAUSED 4
#define PLAYING_IN_PROGRESS 5
#define PLAYING_STOPPED 6
#define RECORDING_STARTED 7
#define RECORDING_IN_PROGRESS 8
#define RECORDING_STOPPED 9
#define OPERATION_ERROR 10

namespace bb {
namespace cascades {

class SEAudioStreamEngine : public QObject {

	Q_OBJECT

private:
	int state;

public:
	SEAudioStreamEngine();
	virtual ~SEAudioStreamEngine();

	void notifyPlayerStateChanged(int event);
	void notifyRecorderStateChanged(int event);

	int getState();

signals:
	void playerStateChanged(int event);
	void recorderStateChanged(int event);

protected:

    virtual void startPlaying();

    virtual void pausePlaying();

    virtual void stopPlaying();

    virtual void startRecording();

    virtual void stopRecording();

    /**
     * @param currentTime current time in seconds
     */
    virtual void setCurrentTime(double currentTime);

    virtual double getCurrentTime();

};

} /* namespace cascades */
} /* namespace bb */
#endif /* SEAUDIOSTREAMENGINE_H_ */
