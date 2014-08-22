#ifndef SEAUDIOSTREAMENGINE_H
#define SEAUDIOSTREAMENGINE_H

#include <QObject>

class SEAudioStream;
class SEAudioStreamPlayer;
class SEAudioStreamRecorder;

typedef enum {
	kSEAudioStreamEngineStateNotReady,
	kSEAudioStreamEngineStateReady,
	kSEAudioStreamEngineStatePlaying,
	kSEAudioStreamEngineStatePaused,
	kSEAudioStreamEngineStateRecording,
} TSEAudioStreamEngineState;

class SEAudioStreamEngine: public QObject {
	Q_OBJECT
public:
	explicit SEAudioStreamEngine(SEAudioStream* stream, QObject *parent = 0);

    TSEAudioStreamEngineState getState();

	double getDuration();

	void setCurrentPosition(double position);
	double getCurrentPosition();

    SEAudioStream* getStream();

	void play();
	void pause();
	void record();
	void stop();

protected:
    SEAudioStream*              stream;
    SEAudioStreamPlayer*        player;
    SEAudioStreamRecorder*      recorder;
    TSEAudioStreamEngineState   state;
    long                        position;

private Q_SLOTS:
	void onPlayBegin();
	void onPlayPause();
	void onPlayContinue();
	void onPlayStop();
    void onPlayUpdate(unsigned int time);
	void onRecordBegin();
	void onRecordStop();
	void onRecordUpdate(unsigned int time);
	void onError(QString error);

	Q_SIGNALS:
	void startPlaying();
	void pausePlaying();
	void continuePlaying();
	void updatePlaying(double time);
	void stopPlaying();
	void startRecording();
	void updateRecording(double time);
	void stopRecording();
	void errorOccurred(QString error);
};

#endif // SEAUDIOSTREAMENGINE_H
