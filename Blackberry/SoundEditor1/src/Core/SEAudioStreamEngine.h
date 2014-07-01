#ifndef SEAUDIOSTREAMENGINE_H
#define SEAUDIOSTREAMENGINE_H

#include <QObject>

class SEAudioStream;
class SEAudioStreamPlayer;

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

	void play();
	void pause();
	void record();
	void stop();

private:
    SEAudioStream*              stream;
    SEAudioStreamPlayer*        player;
    TSEAudioStreamEngineState   state;

private Q_SLOTS:
	void onPlayBegin();
	void onPlayPause();
	void onPlayContinue();
	void onPlayStop();
    void onPlayUpdate(uint time);
	void onRecordBegin();
	void onRecordStop();
	void onRecordUpdate(int time);
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
