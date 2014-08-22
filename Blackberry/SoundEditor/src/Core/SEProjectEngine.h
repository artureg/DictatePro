#ifndef SEPROJECTENGINE_H
#define SEPROJECTENGINE_H

#include "SEAudioStreamEngine.h"

class SEProject;

class SEProjectEngine: public SEAudioStreamEngine {
    Q_OBJECT
public:
    explicit SEProjectEngine(SEProject* project, QObject *parent = 0);

    double getDuration();

    void record();
    void stop();
    void clear();

private:
    SEProject*              project;
    SEAudioStreamEngine*    recordEngine;

private Q_SLOTS:
    void onStartRecording();
    void onUpdateRecording(double time);
    void onStopRecording();
    void onErrorOccurred(QString error);
};

#endif // SEPROJECTENGINE_H
