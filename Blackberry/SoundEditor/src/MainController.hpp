/*
 * Copyright (c) 2014 wise-apps.com
 *
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 20.03.2014
 */

#ifndef MainController_HPP_
#define MainController_HPP_

#include <QObject>
#include <QTimer>

class SEProject;
class SEProjectEngine;
class SEAudioStreamEngine;

namespace bb {
    namespace cascades {
        class Application;
        class LocaleHandler;
    }
}

class QTranslator;

/**
 * Main Controller class
 */
class MainController : public QObject {
    Q_OBJECT

    // PROGRESS BAR
    // Makes the current progress value available to the UI
    Q_PROPERTY(double progressValue READ progressValue NOTIFY progressValueChanged)

    // SLIDER Volume
    // Makes the current Slider volume  value available to the UI
    Q_PROPERTY(double volumeValue READ volumeValue NOTIFY volumeValueChanged)

    Q_PROPERTY(QString labelText READ labelText NOTIFY signalButtonsStatesChanged)

    // Buttons
    Q_PROPERTY(bool isEnablePlayButton READ isEnablePlayButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isEnableRewindButton READ isEnableRewindButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isEnableForwardButton READ isEnableForwardButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isEnableToStartButton READ isEnableToStartButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isEnableToEndButton READ isEnableToEndButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isEnableRecordButton READ isEnableRecordButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isCheckedPlayButton READ isCheckedPlayButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isCheckedRecordButton READ isCheckedRecordButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isEnableSDButton READ isEnableSDButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isEnableDeleteButton READ isEnableDeleteButton NOTIFY signalButtonsStatesChanged)
    Q_PROPERTY(bool isEnableMailButton READ isEnableMailButton NOTIFY signalButtonsStatesChanged)

private:

    QTranslator* m_pTranslator;
    bb::cascades::LocaleHandler* m_pLocaleHandler;

    SEProject* project;
    SEProjectEngine* engine;
    SEAudioStreamEngine* speexEngine;

    bool speexMode;

    // PROGRESS BAR
    // The current progress value
    double m_progressValue;

    // SLIDER Volume
    // The current Slider volume value
    int m_volumeValue;

    QString labelTextValue;

    // Buttons
    bool isEnabledRewindButtonValue;
    bool isEnabledToEndButtonValue;
    bool isEnabledPlayButtonValue;
    bool isEnabledToStartButtonValue;
    bool isEnabledForwardButtonValue;
    bool isEnabledRecordButtonValue;
    bool isCheckedPlayButtonValue;
    bool isCheckedRecordButtonValue;
    bool isEnabledSDButtonValue;
    bool isEnabledDeleteButtonValue;
    bool isEnabledMailButtonValue;

    void showToast(QString message);
    void reloadUI();

public:

    MainController(bb::cascades::Application *app);
    virtual ~MainController() { }

    QString labelText();

    // PROGRESS BAR
    Q_INVOKABLE double progressValue() const;
    Q_INVOKABLE void progressChanged(double value);

    // SLIDER Volume
    Q_INVOKABLE double volumeValue() const;

    // Buttons
    Q_INVOKABLE bool isEnablePlayButton();
    Q_INVOKABLE bool isEnableRewindButton();
    Q_INVOKABLE bool isEnableForwardButton();
    Q_INVOKABLE bool isEnableToStartButton();
    Q_INVOKABLE bool isEnableToEndButton();
    Q_INVOKABLE bool isEnableRecordButton();
    Q_INVOKABLE bool isCheckedPlayButton();
    Q_INVOKABLE bool isCheckedRecordButton();
    Q_INVOKABLE bool isEnableSDButton();
    Q_INVOKABLE bool isEnableDeleteButton();
    Q_INVOKABLE bool isEnableMailButton();


public Q_SLOTS:

    // Buttons clicks
    void onPlayButtonClick();
    void onPauseButtonClick();
    void onRecordButtonClick();
    void onStopRecordingButtonClick();
    void onSDButtonClick();
    void onForwardButtonClick();
    void onRewindButtonClick();
    void onToStartButtonClick();
    void onToEndButtonClick();
    void onDeleteButtonClick();
    void onMailButtonClick();

    void onStartPlaying();
    void onPausePlaying();
    void onContinuePlaying();
    void onUpdatePlaying(double time);
    void onStopPlaying();
    void onStartRecording();
    void onUpdateRecording(double time);
    void onStopRecording();
    void onErrorOccurred(QString error);

Q_SIGNALS:
    // PROGRESS BAR
    void progressValueChanged();

    // SLIDER Volume
    void volumeValueChanged();

    // Buttons
    void signalButtonsStatesChanged();
};

#endif /* MainController_HPP_ */
