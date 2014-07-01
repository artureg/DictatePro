#include "SEProjectEngine.h"

SEProjectEngine::SEProjectEngine(SEProject* project, QObject *parent) :
		SEAudioStreamEngine(project->getAudioStream()) {
	this->project = project;
}

