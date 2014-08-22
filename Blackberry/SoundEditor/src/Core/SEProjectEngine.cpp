#include "SEProjectEngine.h"
#include "SEProject.h"
#include "Internal/SERecordAudioStream.h"
#include "SEProjectAudioStream.h"
#include <QDebug>

SEProjectEngine::SEProjectEngine(SEProject* project, QObject *parent) :
        SEAudioStreamEngine(project->getAudioStream(), parent) {
	this->project = project;
	recordEngine = NULL;
	TSEAudioStreamDesc desc;
	desc.audioFormat = 0;
	desc.bitsPerSample = 8;
	desc.bytesPerSample = 1;
	desc.numberOfChannels = 1;
	desc.sampleRate = 16000;
	desc.bytesPerSecond = desc.bitsPerSample/8*desc.sampleRate*desc.numberOfChannels;
	this->getStream()->setDescription(desc);
}

void SEProjectEngine::record() {
    if ((this->state != kSEAudioStreamEngineStateReady)&&(this->state != kSEAudioStreamEngineStatePaused)) {
        return;
    }
    this->state = kSEAudioStreamEngineStateRecording;
    recordEngine = new SEAudioStreamEngine(new SERecordAudioStream(project->splitRecordInPosition(position)), this);
    recordEngine->getStream()->setDescription(this->getStream()->getDescription());
    connect(recordEngine, SIGNAL(startRecording()), this, SLOT(onStartRecording()));
    connect(recordEngine, SIGNAL(stopRecording()), this, SLOT(onStopRecording()));
    connect(recordEngine, SIGNAL(updateRecording(double)), this, SLOT(onUpdateRecording(double)));
    connect(recordEngine, SIGNAL(errorOccurred(QString)), this, SLOT(onErrorOccurred(QString)));
    recordEngine->record();
}

double SEProjectEngine::getDuration() {
	if (state == kSEAudioStreamEngineStateRecording) {
		return this->recordEngine->getDuration();
	} else {
		return ((SEProjectAudioStream*)this->stream)->getDuration()/1000.0f;
	}
}

void SEProjectEngine::stop() {
    if (this->state != kSEAudioStreamEngineStateRecording) {
        return;
    }
    this->state = kSEAudioStreamEngineStateReady;
    recordEngine->stop();
    SERecordAudioStream* rStream = (SERecordAudioStream*)recordEngine->getStream();
    SERecord* record = rStream->getRecord();
    record->setRange(SERecordRangeMake(0, rStream->getDuration()));
    this->project->saveProject();
    recordEngine = NULL;
    position += record->getRange().duration;
    emit stopRecording();
    emit updatePlaying(position);
}

void SEProjectEngine::clear() {
	if ((this->state != kSEAudioStreamEngineStateReady)&&(this->state != kSEAudioStreamEngineStatePaused)) {
		return;
	}
	position = 0;
	project->clearProject();
}

void SEProjectEngine::onStartRecording() {
    emit startRecording();
}

void SEProjectEngine::onStopRecording() {
}

void SEProjectEngine::onErrorOccurred(QString error) {
    emit errorOccurred(error);
}

void SEProjectEngine::onUpdateRecording(double time) {
    emit updateRecording(time);
}
