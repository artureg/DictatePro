#include <QDebug>
#include "Core/SEProject.h"
#include "Core/SEProjectEngine.h"

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
        , m_progressMinimum(0)
        , m_progressMaximum(0)
        , m_progressValue(0)
        , m_volumeMinimum(0)
        , m_volumeMaximum(0)
        , m_volumeValue(0)
        , is_checked_play_button(false)
        , is_checked_record_button(false)
        , is_enable_play_button(false)
        , is_enable_record_button(false)
        , is_enable_sd_button(false) {

    project = new SEProject("", this);
    engine = new SEProjectEngine(project, this);
    connect(engine, SIGNAL(startPlaying()), this, SLOT(onStartPlaying()));
    connect(engine, SIGNAL(pausePlaying()), this, SLOT(onPausePlaying()));
    connect(engine, SIGNAL(continuePlaying()), this, SLOT(onContinuePlaying()));
    connect(engine, SIGNAL(stopPlaying()), this, SLOT(onStopPlaying()));
    connect(engine, SIGNAL(updatePlaying(double)), this, SLOT(onUpdatePlaying(double)));
    connect(engine, SIGNAL(startRecording()), this, SLOT(onStartRecording()));
    connect(engine, SIGNAL(stopRecording()), this, SLOT(onStopRecording()));
    connect(engine, SIGNAL(updateRecording(double)), this, SLOT(onUpdateRecording(double)));
    connect(engine, SIGNAL(errorOccurred(QString)), this, SLOT(onErrorOccurred(QString)));

    // Create scene document from main.qml asset, the parent is set
    // to ensure the document gets destroyed properly at shut down.
    QmlDocument *qml = QmlDocument::create("asset:///main_page.qml").parent(this);

    // Make the Main view object available to the UI as context property
    qml->setContextProperty("_mainController", this);

    // Create root object for the UI
    AbstractPane *root = qml->createRootObject<AbstractPane>();

    // Set created root object as the application scene
    app->setScene(root);
}

//TODO Progress bar

/**
 * The minimum progress value
 */
double MainController::progressMinimum() const {
    return m_progressMinimum;
}

/**
 * The maximum progress value
 */
double MainController::progressMaximum() const {
    return m_progressMaximum;
}

/**
 * The current progress value
 */
double MainController::progressValue() const {
    return m_progressValue;
}

void MainController::progressRangeChanged(double minimum, double maximum) {
    m_progressMinimum = minimum;
    m_progressMaximum = maximum;

    emit progressRangeChanged();
}

void MainController::progressValueChanged(double value) {
    qDebug() << "Progress Changed: " + QString::number(value);
    if (m_progressValue == value) {
        return;
    }

    m_progressValue = value;
    emit progressValueChanged();
}

// TODO Slider volume

/**
 * The minimum Slider volume value
 */
double MainController::volumeMinimum() const {
    return m_volumeMinimum;
}

/**
 * The maximum Slider volume value
 */
double MainController::volumeMaximum() const {
    return m_volumeMaximum;
}

/**
 * The current Slider volume value
 */
double MainController::volumeValue() const {
    return m_volumeValue;
}

void MainController::volumeRangeChanged(double minimum, double maximum) {
    m_volumeMinimum = minimum;
    m_volumeMaximum = maximum;

    emit volumeRangeChanged();
}

//TODO end project engine

void MainController::volumeValueChanged(double value) {
    if (m_volumeValue == value)
        return;

    m_volumeValue = value;
    emit volumeValueChanged();
}

// TODO Button states

/**
 * Return enable state of play button
 */
bool MainController::isEnablePlayButton() {
    return is_enable_play_button;
}

bool MainController::isEnableRecordButton() {
    return is_enable_record_button;
}

bool MainController::isCheckedPlayButton() {
    return is_checked_play_button;
}

bool MainController::isCheckedRecordButton() {
    return is_checked_record_button;
}

bool MainController::isEnableSDButton() {
    return is_enable_sd_button;
}

// TODO Button clicks

/**
 * This method is invoked when the user clicks on the 'Play' button
 */
void MainController::onPlayButtonClick() {
    emit signalButtonsStatesChanged();
}

/**
 * This method is invoked when the user clicks on the 'Record' button
 */
void MainController::onRecordButtonClick() {
    emit signalButtonsStatesChanged();
}

/**
 * This method is invoked when the user clicks on the 'SD' button
 */
void MainController::onSDButtonClick() {
    emit signalButtonsStatesChanged();
}

/**
 * This method is invoked when the user clicks on the 'Send' button
 */
void MainController::onSendButtonClick() {
    emit signalButtonsStatesChanged();
}

/**
 * This method is invoked when the user clicks on the 'Forward' button
 * per-second rewind to end
 */
void MainController::onForwardButtonClick() {
    emit signalButtonsStatesChanged();
}

/**
 * This method is invoked when the user clicks on the 'Rewind' button
 * per-second rewind to start
 */
void MainController::onRewindButtonClick() {
    emit signalButtonsStatesChanged();
}

/**
 * This method is invoked when the user clicks on the 'To Start' button
 */
void MainController::onToStartButtonClick() {
    emit signalButtonsStatesChanged();
}

/**
 * This method is invoked when the user clicks on the 'To End' button
 */
void MainController::onToEndButtonClick() {
    emit signalButtonsStatesChanged();
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

void MainController::onStartPlaying() {
    qDebug() << "Start Playing";
}

void MainController::onPausePlaying() {
    qDebug() << "Pause Playing";
}

void MainController::onContinuePlaying() {
    qDebug() << "Continue Playing";
}

void MainController::onUpdatePlaying(double time) {
    m_progressValue = time*1000;
    qDebug() << "Play Time: " + QString::number(time);
    emit progressValueChanged();
}

void MainController::onStopPlaying() {
    qDebug() << "Stop Playing";
}

void MainController::onStartRecording() {
    qDebug() << "Start Recording";
}

void MainController::onUpdateRecording(double time) {
    qDebug() << "Record Time: " + QString::number(time);
}

void MainController::onStopRecording() {
    qDebug() << "Stop Recording";
}

void MainController::onErrorOccurred(QString error) {
    qDebug() << "Got Error: " + error;
}

void MainController::onTest(double asd) {
    qDebug() << "test" + QString::number(asd);
    m_progressMaximum = 500;
    emit progressRangeChanged();
}
