#ifndef SEPROJECTENGINE_H
#define SEPROJECTENGINE_H

#include "SEAudioStreamEngine.h"
#include "SEProject.h"

class SEProjectEngine: public SEAudioStreamEngine {
    Q_OBJECT
public:
    explicit SEProjectEngine(SEProject* project, QObject *parent = 0);

private:
    SEProject* project;
};

#endif // SEPROJECTENGINE_H
