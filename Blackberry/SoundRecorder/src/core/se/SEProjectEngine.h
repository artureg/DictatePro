/*
 * SEProjectEngine.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEPROJECTENGINE_H_
#define SEPROJECTENGINE_H_

//#include <qobject.h>

#include "SEAudioStreamEngine.h"
#include "SEProject.h"
#include "SESoundPlayer.h"
#include "SESoundRecorder.h"

class SEProjectEngine: public SEAudioStreamEngine {

	Q_OBJECT

public:

	SEProjectEngine();
	virtual ~SEProjectEngine();

	void initWithProject(SEProject &project);

	void startPlaying();
	void stopPlaying();
	void startRecording();
	void stopRecording();
	void setPosition(unsigned int position);

private:
	SEProject* project;
	SESoundPlayer *soundPlayer;
	SESoundRecorder *soundRecorder;

public Q_SLOTS:
	void slotError(char *msg);
	void slotStoped();
	void slotStarted();
	void slotPositionChanged(unsigned int position);
Q_SIGNALS:
	void signalStop();
	void signalChangePosition(unsigned int position);

};

#endif /* SEPROJECTENGINE_H_ */
