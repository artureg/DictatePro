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

namespace bb
{
    namespace cascades
    {
        class Application;
        class LocaleHandler;
    }
}

class QTranslator;

/**
 * Main Controller class
 */
class MainController : public QObject
{
    Q_OBJECT

   // PROGRESS BAR

   // Makes the minimum of the progress indicator available to the UI
   Q_PROPERTY(double progressMinimum READ progressMinimum NOTIFY progressRangeChanged)

   // Makes the maximum of the progress indicator available to the UI
   Q_PROPERTY(double progressMaximum READ progressMaximum NOTIFY progressRangeChanged)

   // Makes the current progress value available to the UI
   Q_PROPERTY(double progressValue READ progressValue NOTIFY progressValueChanged)


   // SLIDER Volume

   // Makes the minimum of the Slider volume indicator available to the UI
   Q_PROPERTY(double volumeMinimum READ volumeMinimum NOTIFY volumeRangeChanged)

   // Makes the maximum of the Slider volume  indicator available to the UI
   Q_PROPERTY(double volumeMaximum READ volumeMaximum NOTIFY volumeRangeChanged)

   // Makes the current Slider volume  value available to the UI
   Q_PROPERTY(double volumeValue READ volumeValue NOTIFY volumeValueChanged)


   // Buttons

  // Q_PROPERTY(QString playButtonImage READ playButtonImage NOTIFY signalButtonsStatesChanged)

   Q_PROPERTY(bool isEnablePlayButton READ isEnablePlayButton NOTIFY signalButtonsStatesChanged)
   Q_PROPERTY(bool isEnableRecordButton READ isEnableRecordButton NOTIFY signalButtonsStatesChanged)
   Q_PROPERTY(bool isCheckedPlayButton READ isCheckedPlayButton NOTIFY signalButtonsStatesChanged)
   Q_PROPERTY(bool isCheckedRecordButton READ isCheckedRecordButton NOTIFY signalButtonsStatesChanged)
   Q_PROPERTY(bool isEnableSDButton READ isEnableSDButton NOTIFY signalButtonsStatesChanged)

private:

    QTranslator* m_pTranslator;
    bb::cascades::LocaleHandler* m_pLocaleHandler;

    SEProject* project;
    SEProjectEngine* engine;

    // PROGRESS BAR

    // The minimum progress value
    double m_progressMinimum;

    // The maximum progress value
    double m_progressMaximum;

    // The current progress value
    double m_progressValue;


    bool testT;

    // SLIDER Volume

    // The minimum Slider volume value
    int m_volumeMinimum;

    // The maximum Slider volume value
    int m_volumeMaximum;

    // The current Slider volume value
    int m_volumeValue;

    // Buttons
    bool is_enable_play_button;
    bool is_enable_record_button;
    bool is_checked_play_button;
    bool is_checked_record_button;
    bool is_enable_sd_button;

    bool start_record_wav_file();
    bool stop_record_wav_file();
    void initWorkspace();

    void showToast(QString message);

public:

    MainController(bb::cascades::Application *app);
    virtual ~MainController() { }

    // PROGRESS BAR
    double progressMinimum() const;
    double progressMaximum() const;
    double progressValue() const;


    // SLIDER Volume
    double volumeMinimum() const;
    double volumeMaximum() const;
    double volumeValue() const;

    // Buttons
    bool isEnablePlayButton();
    bool isEnableRecordButton();
    bool isCheckedPlayButton();
    bool isCheckedRecordButton();
    bool isEnableSDButton();

public Q_SLOTS:

    // Buttons clicks
    void onPlayButtonClick();
    void onRecordButtonClick();
    void onSDButtonClick();
    void onSendButtonClick();
    void onForwardButtonClick();
    void onRewindButtonClick();
    void onToStartButtonClick();
    void onToEndButtonClick();

    void onStartPlaying();
    void onPausePlaying();
    void onContinuePlaying();
    void onUpdatePlaying(double time);
    void onStopPlaying();
    void onStartRecording();
    void onUpdateRecording(double time);
    void onStopRecording();
    void onErrorOccurred(QString error);

    void onTest(double asd);

Q_SIGNALS:
    // PROGRESS BAR
    void progressRangeChanged();
    void progressValueChanged();

    // SLIDER Volume
    void volumeRangeChanged();
    void volumeValueChanged();

    // Buttons
    void signalButtonsStatesChanged();

private Q_SLOTS:

    // PROGRESS BAR
    void progressRangeChanged(double minimum, double maximum);
    void progressValueChanged(double value);

    // SLIDER Volume
    void volumeRangeChanged(double minimum, double maximum);
    void volumeValueChanged(double value);
};

#endif /* MainController_HPP_ */
