#include <QDebug>
#include "Core/SEProject.h"
#include "Core/SEProjectEngine.h"
#include "Core/Internal/SESpeexAudioStream.h"

#include "MainController.hpp"

#include <sys/stat.h>
#include <stdio.h>
#include <unistd.h>
#include <mm/renderer.h>
#include <cmath>


#include "MainController.hpp"

#include <sys/stat.h>
#include <stdio.h>
#include <unistd.h>
#include <mm/renderer.h>
#include <cmath>

#include <bb/cascades/Application>
#include <bb/cascades/QmlDocument>
#include <bb/cascades/AbstractPane>
#include <bb/cascades/LocaleHandler>
#include <bb/multimedia/AudioRecorder>
#include <bb/cascades/GroupDataModel>
#include <bb/multimedia/AudioRecorder>

#include <bb/system/SystemToast>
#include <bb/system/SystemDialog>

using namespace bb::cascades;
using namespace bb::system;

MainController::MainController(bb::cascades::Application *app) :
        QObject(app)
        , m_progressValue(0)
        , m_volumeValue(0)
        , isEnabledPlayButtonValue(true)
        , isEnabledForwardButtonValue(false)
        , isEnabledRewindButtonValue(false)
        , isEnabledToEndButtonValue(false)
        , isEnabledToStartButtonValue(false)
        , isEnabledRecordButtonValue(false)
        , isCheckedPlayButtonValue(false)
        , isCheckedRecordButtonValue(false)
        , isEnabledDeleteButtonValue(false)
        , isEnabledMailButtonValue(false)
        , isEnabledSDButtonValue(false) {

    speexMode = false;
    speexEngine = NULL;
    project = new SEProject("Default", this);
    engine = new SEProjectEngine(project, this);
    reloadUI();

    connect(engine, SIGNAL(startPlaying()), this, SLOT(onStartPlaying()));
    connect(engine, SIGNAL(pausePlaying()), this, SLOT(onPausePlaying()));
    connect(engine, SIGNAL(continuePlaying()), this, SLOT(onContinuePlaying()));
    connect(engine, SIGNAL(stopPlaying()), this, SLOT(onStopPlaying()));
    connect(engine, SIGNAL(updatePlaying(double)), this, SLOT(onUpdatePlaying(double)));
    connect(engine, SIGNAL(startRecording()), this, SLOT(onStartRecording()));
    connect(engine, SIGNAL(stopRecording()), this, SLOT(onStopRecording()));
    connect(engine, SIGNAL(updateRecording(double)), this, SLOT(onUpdateRecording(double)));
    connect(engine, SIGNAL(errorOccurred(QString)), this, SLOT(onErrorOccurred(QString)));

    QmlDocument *qml = QmlDocument::create("asset:///main.qml").parent(this);
    qml->setContextProperty("_mainController", this);

    AbstractPane *root = qml->createRootObject<AbstractPane>();
    app->setScene(root);
}

//TODO Progress bar

/**
 * The current progress value
 */
double MainController::progressValue() const {
    return m_progressValue;
}

void MainController::progressChanged(double value) {
    if (engine) {
        engine->setCurrentPosition(engine->getDuration()*value);
        qDebug() << "Position Changed to " + QString::number(engine->getCurrentPosition());
    }
}

QString MainController::labelText() {
	return labelTextValue;
}

// TODO Slider volume

/**
 * The current Slider volume value
 */
double MainController::volumeValue() const {
    return m_volumeValue;
}

// TODO Button states

/**
 * Return enable state of play button
 */
bool MainController::isEnablePlayButton() {
    return isEnabledPlayButtonValue;
}

bool MainController::isEnableForwardButton() {
    return isEnabledForwardButtonValue;
}

bool MainController::isEnableRewindButton() {
    return isEnabledRewindButtonValue;
}

bool MainController::isEnableToEndButton() {
    return isEnabledToEndButtonValue;
}

bool MainController::isEnableToStartButton() {
    return isEnabledToStartButtonValue;
}

bool MainController::isEnableRecordButton() {
    return isEnabledRecordButtonValue;
}

bool MainController::isCheckedPlayButton() {
    return isCheckedPlayButtonValue;
}

bool MainController::isCheckedRecordButton() {
    return isCheckedRecordButtonValue;
}

bool MainController::isEnableSDButton() {
    return isEnabledSDButtonValue;
}

bool MainController::isEnableDeleteButton() {
    return isEnabledDeleteButtonValue;
}

bool MainController::isEnableMailButton() {
    return isEnabledMailButtonValue;
}

// TODO Button clicks

void MainController::onPlayButtonClick() {
    qDebug() << "Play";
    if (speexMode) {
        speexEngine->play();
    } else {
        engine->play();
    }
}

void MainController::onPauseButtonClick() {
    qDebug() << "Pause";
    if (speexMode) {
        speexEngine->pause();
    } else {
        engine->pause();
    }
    reloadUI();
}

void MainController::onRecordButtonClick() {
    qDebug() << "Record";
    engine->record();
}

void MainController::onStopRecordingButtonClick() {
    qDebug() << "Stop Recording";
    engine->stop();
    reloadUI();
}

void MainController::onSDButtonClick() {
    qDebug() << "SD";
    labelTextValue = "Converting to SPEEX";
    reloadUI();
    QString path = project->getProjectPath() + "/" + QString::number(random()%9999999) + ".wav";
    SESpeexAudioStream* stream = new SESpeexAudioStream(path, this);
    stream->setDescription(engine->getStream()->getDescription());
    if (!engine->getStream()->exportToAudioStream(stream)) {
        showToast("Failed to Convert");
        return;
    }
    speexEngine = new SEAudioStreamEngine(new SESpeexAudioStream(path, this), this);
    connect(speexEngine, SIGNAL(startPlaying()), this, SLOT(onStartPlaying()));
    connect(speexEngine, SIGNAL(pausePlaying()), this, SLOT(onPausePlaying()));
    connect(speexEngine, SIGNAL(continuePlaying()), this, SLOT(onContinuePlaying()));
    connect(speexEngine, SIGNAL(stopPlaying()), this, SLOT(onStopPlaying()));
    connect(speexEngine, SIGNAL(updatePlaying(double)), this, SLOT(onUpdatePlaying(double)));
    connect(speexEngine, SIGNAL(errorOccurred(QString)), this, SLOT(onErrorOccurred(QString)));
    labelTextValue = "Ready";
    reloadUI();
}

void MainController::onForwardButtonClick() {
    qDebug() << "Forward";
    if (speexMode) {
        speexEngine->setCurrentPosition(speexEngine->getCurrentPosition() + 1);
    } else {
        engine->setCurrentPosition(engine->getCurrentPosition() + 1);
    }
}

void MainController::onRewindButtonClick() {
    qDebug() << "Rewind";
    if (speexMode) {
        speexEngine->setCurrentPosition(speexEngine->getCurrentPosition() - 1);
    } else {
        engine->setCurrentPosition(engine->getCurrentPosition() - 1);
    }
}

void MainController::onToStartButtonClick() {
    qDebug() << "To Start";
    if (speexMode) {
        speexEngine->setCurrentPosition(0);
    } else {
        engine->setCurrentPosition(0);
    }
}

void MainController::onToEndButtonClick() {
    qDebug() << "To End";
    if (speexMode) {
        speexEngine->setCurrentPosition(speexEngine->getDuration());
    } else {
        engine->setCurrentPosition(engine->getDuration());
    }
}

void MainController::onDeleteButtonClick() {
    qDebug() << "Clear";
    engine->clear();
    reloadUI();
}

void MainController::onMailButtonClick() {
    qDebug() << "Speex Mode";
    if (!speexEngine) {
        return;
    }
    speexMode = !speexMode;
    reloadUI();
}

/**
 * Method show toasts
 */
void MainController::showToast(QString message) {
    SystemToast *toast = new SystemToast(this);
    toast->setBody(message);
    toast->setPosition(SystemUiPosition::MiddleCenter);
    toast->show();
}

void MainController::reloadUI() {
    if (speexMode) {
        isEnabledDeleteButtonValue = false;
        isEnabledRecordButtonValue = false;
        isEnabledSDButtonValue = false;
        isEnabledForwardButtonValue = true;
        isEnabledRewindButtonValue = true;
        isEnabledPlayButtonValue = true;
        isEnabledToEndButtonValue = true;
        isEnabledToStartButtonValue = true;
        switch (engine->getState()) {
        case kSEAudioStreamEngineStatePaused:
            labelTextValue = "Speex Paused " + QString::number(speexEngine->getCurrentPosition(), 'f', 1) + "/" + QString::number(speexEngine->getDuration(), 'f', 1);
            break;
        case kSEAudioStreamEngineStatePlaying:
            labelTextValue = "Speex Playing " + QString::number(speexEngine->getCurrentPosition(), 'f', 1) + "/" + QString::number(speexEngine->getDuration(), 'f', 1);
            break;
        case kSEAudioStreamEngineStateReady:
            labelTextValue = "Speex Ready " + QString::number(speexEngine->getCurrentPosition(), 'f', 1) + "/" + QString::number(speexEngine->getDuration(), 'f', 1);
            break;
        default:
            break;
        }
        emit signalButtonsStatesChanged();
        return;
    }
    switch (engine->getState()) {
    case kSEAudioStreamEngineStatePaused:
        isEnabledPlayButtonValue = (engine->getDuration() > 0);
        isEnabledForwardButtonValue = (engine->getDuration() > 0);
        isEnabledRewindButtonValue = (engine->getDuration() > 0);
        isEnabledToEndButtonValue = (engine->getDuration() > 0);
        isEnabledToStartButtonValue = (engine->getDuration() > 0);
        isEnabledSDButtonValue = (engine->getDuration() > 0);
        isEnabledDeleteButtonValue = (engine->getDuration() > 0);
        isEnabledMailButtonValue = (speexEngine != NULL);
        isEnabledRecordButtonValue = true;
        isCheckedRecordButtonValue = false;
        isCheckedPlayButtonValue = false;
        labelTextValue = "Paused " + QString::number(engine->getCurrentPosition(), 'f', 1) + "/" + QString::number(engine->getDuration(), 'f', 1);
        break;
    case kSEAudioStreamEngineStatePlaying:
        isEnabledPlayButtonValue = (engine->getDuration() > 0);
        isEnabledForwardButtonValue = (engine->getDuration() > 0);
        isEnabledRewindButtonValue = (engine->getDuration() > 0);
        isEnabledToEndButtonValue = (engine->getDuration() > 0);
        isEnabledToStartButtonValue = (engine->getDuration() > 0);
        isEnabledDeleteButtonValue = false;
        isEnabledMailButtonValue = false;
        isEnabledSDButtonValue = false;
        isEnabledRecordButtonValue = false;
        isCheckedRecordButtonValue = false;
        isCheckedPlayButtonValue = true;
        labelTextValue = "Playing " + QString::number(engine->getCurrentPosition(), 'f', 1) + "/" + QString::number(engine->getDuration(), 'f', 1);
        break;
    case kSEAudioStreamEngineStateRecording:
        isEnabledDeleteButtonValue = false;
        isEnabledMailButtonValue = false;
        isEnabledPlayButtonValue = false;
        isEnabledForwardButtonValue = false;
        isEnabledRewindButtonValue = false;
        isEnabledToEndButtonValue = false;
        isEnabledToStartButtonValue = false;
        isEnabledRecordButtonValue = true;
        isEnabledSDButtonValue = false;
        isCheckedRecordButtonValue = true;
        isCheckedPlayButtonValue = false;
        labelTextValue = "Recording " + QString::number(engine->getDuration(), 'f', 1);
        break;
    case kSEAudioStreamEngineStateReady:
        isEnabledPlayButtonValue = (engine->getDuration() > 0);
        isEnabledForwardButtonValue = (engine->getDuration() > 0);
        isEnabledRewindButtonValue = (engine->getDuration() > 0);
        isEnabledToEndButtonValue = (engine->getDuration() > 0);
        isEnabledToStartButtonValue = (engine->getDuration() > 0);
        isEnabledSDButtonValue = (engine->getDuration() > 0);
        isEnabledDeleteButtonValue = (engine->getDuration() > 0);
        isEnabledMailButtonValue = (speexEngine != NULL);
        isEnabledRecordButtonValue = true;
        isCheckedRecordButtonValue = false;
        isCheckedPlayButtonValue = false;
        labelTextValue = "Ready " + QString::number(engine->getCurrentPosition(), 'f', 1) + "/" + QString::number(engine->getDuration(), 'f', 1);
        break;
    default:
        break;
    }
    emit signalButtonsStatesChanged();
}

void MainController::onStartPlaying() {
    qDebug() << "Start Playing";
    reloadUI();
}

void MainController::onPausePlaying() {
    qDebug() << "Pause Playing";
    reloadUI();
}

void MainController::onContinuePlaying() {
    qDebug() << "Continue Playing";
    reloadUI();
}

void MainController::onUpdatePlaying(double time) {
    if (speexMode) {
        m_progressValue = time/speexEngine->getDuration();
    } else {
        m_progressValue = time/engine->getDuration();
    }
    reloadUI();
    emit progressValueChanged();
}

void MainController::onStopPlaying() {
    qDebug() << "Stop Playing";
    reloadUI();
}

void MainController::onStartRecording() {
    qDebug() << "Start Recording";
    reloadUI();
}

void MainController::onUpdateRecording(double time) {
	m_progressValue = 0;
    qDebug() << "Record Time: " + QString::number(time);
    emit progressValueChanged();
    reloadUI();
}

void MainController::onStopRecording() {
    qDebug() << "Stop Recording";
    reloadUI();
}

void MainController::onErrorOccurred(QString error) {
    qDebug() << "Got Error: " + error;
    showToast(error);
    reloadUI();
}
