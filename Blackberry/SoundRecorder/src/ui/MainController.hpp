/*
 * Copyright (c) 2014 wise-apps.com
 *
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 20.03.2014
 */

#ifndef MainController_HPP_
#define MainController_HPP_

#include "utils/StatusEventHandler.h"
#include "utils/SoundPlayer.h"
#include "core/se/SEProjectEngine.h"

#include <QObject>
#include <QTimer>

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
   Q_PROPERTY(int progressMinimum READ progressMinimum NOTIFY progressRangeChanged)

   // Makes the maximum of the progress indicator available to the UI
   Q_PROPERTY(int progressMaximum READ progressMaximum NOTIFY progressRangeChanged)

   // Makes the current progress value available to the UI
   Q_PROPERTY(int progressValue READ progressValue NOTIFY progressValueChanged)


   // SLIDER Volume

   // Makes the minimum of the Slider volume indicator available to the UI
   Q_PROPERTY(int volumeMinimum READ volumeMinimum NOTIFY volumeRangeChanged)

   // Makes the maximum of the Slider volume  indicator available to the UI
   Q_PROPERTY(int volumeMaximum READ volumeMaximum NOTIFY volumeRangeChanged)

   // Makes the current Slider volume  value available to the UI
   Q_PROPERTY(int volumeValue READ volumeValue NOTIFY volumeValueChanged)


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

    SEProjectEngine *projectEngine;

    StatusEventHandler *statusEventHandler;
    SoundPlayer *soundPlayer;
	QTimer *timer;

	// PROGRESS BAR

	// The minimum progress value
	int m_progressMinimum;

	// The maximum progress value
	int m_progressMaximum;

	// The current progress value
	int m_progressValue;


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

    int progressMinimum() const;
    int progressMaximum() const;
    int progressValue() const;


    // SLIDER Volume

    int volumeMinimum() const;
    int volumeMaximum() const;
    int volumeValue() const;

    // Buttons
    bool isEnablePlayButton();
    bool isEnableRecordButton();
    bool isCheckedPlayButton();
    bool isCheckedRecordButton();
    bool isEnableSDButton();


private slots:

    void onSystemLanguageChanged();


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

	void audioStatusUpdateHandler();


	// Project engine

	/** the recorder progress has been changed */
	void onRecordingInProgress(unsigned int position, unsigned int duration);

	/** the recorder has been started */
	void onRecordingStarted(unsigned int position, unsigned int duration);

	/** the recorder has been stopped */
	void onRecordingStopped(unsigned int position, unsigned int duration);

	/** he player progress has been changed */
	void onPlayingInProgress(unsigned int position, unsigned int duration);

	/** the player has been started */
	void onPlayingStarted(unsigned int position, unsigned int duration);

	/** the player has been stopped */
	void onPlayingStopped(unsigned int position, unsigned int duration);

	/** an error has been occurred */
	void onError(unsigned int position, unsigned int duration, QString errorMessage);

	// end project engine




Q_SIGNALS:
	// The change notification signal of the property

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

	void progressRangeChanged(int minimum, int maximum);
	void progressValueChanged(int value);
	void progressValueChanged2();

	// SLIDER Volume


	void volumeRangeChanged(int minimum, int maximum);
	void volumeValueChanged(int value);



};

#endif /* MainController_HPP_ */
