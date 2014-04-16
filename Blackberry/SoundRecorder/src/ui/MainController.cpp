/*
 * Copyright (c) 2014 wise-apps.com
 *
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 20.03.2014
 */

#include "MainController.hpp"

#include <sys/stat.h>
#include <stdio.h>
#include <unistd.h>
#include <mm/renderer.h>
#include <cmath>
#include "core/SPEEXConverter/WaveFile.h"

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

#define TAG "MainController"


// recording wav
int record_output = 0;
mmr_connection_t *record_connection;
mmr_context_t *record_context;

/**
 * Method to return the directory path where
 * the recorded tracks are stored.
 */
static QString trackStorageLocation()
{
	QString workingDir = QDir::currentPath();
	return QString(workingDir +  "/data/devacon/");
}

/**
 * Method to initialize the directory where
 * all the recorded tracks are stored.
 */
static void initializeTrackStorage()
{
    QDir trackStorage(trackStorageLocation());
    if (!trackStorage.exists()){
        QDir::root().mkpath(trackStorage.absolutePath());
    }
}

/**
 * Method to return the path of speex file
 */
static QString getPathSpeexTrack()
{
	return trackStorageLocation() + "/track_speex.wav";
}

/**
 * Method to return the path of temp wav file, it is last recorded fragment
 */
static QString getPathTempTrack()
{
	return trackStorageLocation() + "/_tmp_track.wav";
}

/**
 * Method to return the path of track wav file, file for playing
 */
static QString getPathTrack()
{
	return trackStorageLocation() + "/track.wav";
}

///**
// * Method to return the path of temp wav file
// */
//static QString isTempFileExist()
//{
//	return trackStorageLocation() + "/track_temp.wav";
//	return
//}
//
///**
// * Method to return the path of temp wav file
// */
//static QString isSpeexFile()
//{
//	return trackStorageLocation() + "/track_temp.wav";
//}

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
		, is_enable_sd_button(false)
{
    // prepare the localization
    m_pTranslator = new QTranslator(this);
    m_pLocaleHandler = new LocaleHandler(this);

	qDebug() << TAG << " start main controller";

	const char* record_context_name = "wise-apps.com";
	record_connection = mmr_connect(NULL);

	record_context = mmr_context_create( record_connection,
									record_context_name,
									0, S_IRUSR
									);

	initializeTrackStorage();
	initWorkspace();

	timer = new QTimer(this);
	connect(timer, SIGNAL(timeout()), this, SLOT(progressValueChanged2()));

    bool res = QObject::connect(m_pLocaleHandler, SIGNAL(systemLanguageChanged()), this, SLOT(onSystemLanguageChanged()));
    // This is only available in Debug builds
    Q_ASSERT(res);
    // Since the variable is not used in the app, this is added to avoid a
    // compiler warning
    Q_UNUSED(res);

    // initial load
    onSystemLanguageChanged();

    // Create scene document from main.qml asset, the parent is set
    // to ensure the document gets destroyed properly at shut down.
    QmlDocument *qml = QmlDocument::create("asset:///page_main.qml").parent(this);

    // Make the Main view object available to the UI as context property
    qml->setContextProperty("_mainController", this);

    // Create root object for the UI
    AbstractPane *root = qml->createRootObject<AbstractPane>();

    // Set created root object as the application scene
    app->setScene(root);
}

/**
 * Method is invoked when locale is changed
 */
void MainController::onSystemLanguageChanged()
{
    QCoreApplication::instance()->removeTranslator(m_pTranslator);
    /// Initiate, load and install the application translation files.
    QString locale_string = QLocale().name();
    QString file_name = QString("SoundRecorder_%1").arg(locale_string);
    if (m_pTranslator->load(file_name, "app/native/qm")) {
        QCoreApplication::instance()->installTranslator(m_pTranslator);
    }
}

void MainController::initWorkspace() {

	if( QFile::exists( getPathTempTrack() ) ) {
		// FIXME there is unfinished session
	}

	if( QFile::exists( getPathTrack()) ) {
		is_enable_play_button = true;
	}

	if( QFile::exists( getPathSpeexTrack() ) ) {
		is_enable_sd_button = true;
	}

    statusEventHandler = new StatusEventHandler(); // init BPS
    connect(statusEventHandler, SIGNAL(audioStatusUpdate()), this, SLOT(audioStatusUpdateHandler()));

    soundPlayer = new SoundPlayer();
    soundPlayer->init(getPathTrack());

    progressRangeChanged(0, soundPlayer->getDuration());

	emit buttonsStatesChanged();

}

//TODO Progress bar

/**
 * The minimum progress value
 */
int MainController::progressMinimum() const
{
    return m_progressMinimum;
}

/**
 * The maximum progress value
 */
int MainController::progressMaximum() const
{
    return m_progressMaximum;
}

/**
 * The current progress value
 */
int MainController::progressValue() const
{
    return m_progressValue;
}

void MainController::progressRangeChanged(int minimum, int maximum)
{
    m_progressMinimum = minimum;
    m_progressMaximum = maximum;

    emit progressRangeChanged();
}

void MainController::progressValueChanged(int value)
{
    if (m_progressValue == value)
        return;

    m_progressValue = value;
    emit progressValueChanged();
}

void MainController::progressValueChanged2()
{

    m_progressValue = soundPlayer->getPosition();
    qDebug() << "EEEEEE";
    emit progressValueChanged();
}

// TODO Slider volume

/**
 * The minimum Slider volume value
 */
int MainController::volumeMinimum() const
{
    return m_volumeMinimum;
}

/**
 * The maximum Slider volume value
 */
int MainController::volumeMaximum() const
{
    return m_volumeMaximum;
}

/**
 * The current Slider volume value
 */
int MainController::volumeValue() const
{
    return m_volumeValue;
}

void MainController::volumeRangeChanged(int minimum, int maximum)
{
    m_volumeMinimum = minimum;
    m_volumeMaximum = maximum;

    emit volumeRangeChanged();
}

void MainController::volumeValueChanged(int value)
{
    if (m_volumeValue == value)
        return;

    m_volumeValue = value;
    emit volumeValueChanged();
}

// TODO Button states

/**
 * Return enable state of play button
 */
bool MainController::isEnablePlayButton()
{
	return is_enable_play_button;
}

bool MainController::isEnableRecordButton()
{
	return is_enable_record_button;
}

bool MainController::isCheckedPlayButton()
{
	return is_checked_play_button;
}

bool MainController::isCheckedRecordButton()
{
	return is_checked_record_button;
}

//void MainController::buttonsStatesChanged()
//{
//
//	emit buttonsStatesChanged();
//}

bool MainController::isEnableSDButton()
{
	return is_enable_sd_button;
}

// TODO RECORDING WAV

/**
 * Record audio via Multimedia Renderer API
 */
bool MainController::start_record_wav_file()
{

	QString str = getPathTempTrack();
    QByteArray bstr = str.toLocal8Bit();
	const char* outputFile = bstr.data();

	int input = 0;

	if (record_connection) {

	  qDebug() << TAG << "start_record_wav_file() output url: " << outputFile;

      if (record_context) {

    	  qDebug() << TAG << "start_record_wav_file() context ready ";

    	  // Point the output URL to the default device
    	  char audio_URL[100];
    	  snprintf( audio_URL, 100, "audio:default" );

    	  // Check if the speaker device is supported and
    	  // connected to the system; if so, point the
    	  // output URL to the speaker device
//    	  bool supported, connected;
//
//    	  if ( audio_manager_is_device_supported(
//    	                          AUDIO_DEVICE_VOICE,
//    	                          &supported ) == 1
//    	                                    && supported )
//    	  {
//    	      if ( audio_manager_is_device_connected(
//    	    		  	  AUDIO_DEVICE_VOICE,
//    	                          &connected ) == 1
//    	                                    && connected )
//    	      {
//    	          sprintf( audio_URL, "audio:%s",
//    	              audio_manager_get_device_name(
//    	            		  AUDIO_DEVICE_VOICE ) );
//    	      } else {
//    	    	  qDebug() << "EYYYYYY! no no no AUDIO_DEVICE_VOICE";
//    	      }
//    	  }
//    	  else {
//    		  qDebug() << "EYYYYYY! no  AUDIO_DEVICE_VOICE";
//    	  }

          // specify a file output so the audio content is
          // not played but recorded in a file
          record_output = mmr_output_attach( record_context,
                                      outputFile,
                                      "file" );

          // in this case, we use a sampling rate of 16000 Hz and
          // 1 channel for mono (not stereo) recording
          input = mmr_input_attach( record_context,
                  "snd:/dev/snd/pcmPreferredc?nchan=1&frate=16000",
                                   "track" );

          // start recording
          mmr_play(record_context);

      } else {
    	  return false;
      }
  } else {
	  return false;
  }

	return true;
}

/**
 * Stop recording audio via Multimedia Renderer API
 */
bool MainController::stop_record_wav_file()
{

	if (record_connection) {
		if (record_context) {

			// stop recording
			mmr_stop (record_context);

			// clean up the context
//			mmr_input_detach(record_context);
//			mmr_output_detach(record_context, record_output);
//			mmr_context_destroy(record_context);

		}

//		mmr_disconnect (record_connection);

		//return true;
	} else {
		//return false;
	}

	return true;

}

// TODO Button clicks

/**
 * This method is invoked when the user clicks on the 'Play' button
 */
void MainController::onPlayButtonClick()
{
	if( is_checked_play_button ) { //stop

		if( soundPlayer->pause() ) {
			is_checked_play_button = false;
			timer->stop();
		} else {
			showToast("Track can not be paused");
		}

		is_checked_play_button = false;

	} else { // play

		if( soundPlayer->play() ) {
			is_checked_play_button = true;
			timer->start(100);
		} else {
			showToast("Track can not be played");
		}
	}

	emit buttonsStatesChanged();

}

/**
 * This method is invoked when the user clicks on the 'Record' button
 */
void MainController::onRecordButtonClick()
{
	qDebug() << TAG << " onRecordButtonClick()";

	if( is_checked_record_button ) { // stop the recorder

		if( stop_record_wav_file() ) {
			is_checked_record_button = false;

			// FIXME process tracks



		} else {
			showToast("Sorry, the microphone is not available, it cannot be stopped");
		}
	} else { // start record

		if( start_record_wav_file() ) {
			is_checked_record_button = true;
		} else {
			showToast("Sorry, the microphone is not available");
		}
	}

	emit buttonsStatesChanged();

}

/**
 * This method is invoked when the user clicks on the 'SD' button
 */
void MainController::onSDButtonClick()
{
 // FIXME Playing speex file from stream
}

/**
 * This method is invoked when the user clicks on the 'Send' button
 */
void MainController::onSendButtonClick()
{
// FIXME convert wav file to speex
}

/**
 * This method is invoked when the user clicks on the 'Forward' button
 * per-second rewind to end
 */
void MainController::onForwardButtonClick()
{
	soundPlayer->setPosition( progressValue() + 1000 );
}

/**
 * This method is invoked when the user clicks on the 'Rearward' button
 * per-second rewind to start
 */
void MainController::onRewindButtonClick()
{
	soundPlayer->setPosition( progressValue() + 1000 );
}

/**
 * This method is invoked when the user clicks on the 'To Start' button
 */
void MainController::onToStartButtonClick()
{
	soundPlayer->setPosition( progressMinimum() );
}

/**
 * This method is invoked when the user clicks on the 'To End' button
 */
void MainController::onToEndButtonClick()
{
	soundPlayer->setPosition( progressMaximum() );
}


// TODO Processing wav

///// This method is invoked when the user clicks on the 'Add record' button
//void MainController::split(int duration)
//{
//
//	int nSampl = duration * 16;
//
//	qDebug() << "PROC nSampl = " << nSampl;
//
//	QByteArray bstr1 = nextTrackUrl().path().toLocal8Bit();
//	const char* writePath1 = bstr1.data();
//	WaveFile tempWaveFile1;
//	bool successfully = tempWaveFile1.openWrite(writePath1);
//	tempWaveFile1.setupInfo(16000, 16, 1);
//
//	QByteArray bstr2 = nextTrackUrl().path().toLocal8Bit();
//	const char* writePath2 = bstr2.data();
//	WaveFile tempWaveFile2;
//	successfully = tempWaveFile2.openWrite(writePath2);
//	tempWaveFile2.setupInfo(16000, 16, 1);
//
//	QByteArray bstr = m_choosed_track.toLocal8Bit();
//	const char* outputPath = bstr.data();
//	WaveFile cWaveFile;
//	successfully = cWaveFile.openRead(outputPath);
//
//	int var = 0;
//	int sumSampl = tempWaveFile1.getNumberOfSamples() + tempWaveFile2.getNumberOfSamples();
//	for (var = 0; var < sumSampl; ++var) {
//		short sample;
//		if(var >= nSampl) {
//			cWaveFile.readSample(sample);
//			tempWaveFile1.writeSample(sample);
//		} else {
//
//		}
//
//	}
//
//	cWaveFile.close();
//	tempWaveFile2.setupInfo(16000, 16, 1);
//	tempWaveFile2.close();
//	tempWaveFile1.setupInfo(16000, 16, 1);
//	tempWaveFile1.close();
//
//}


///**
// * This method is invoked when the user clicks on the 'Clear All' button
// */
//void MainController::onClearAllTracks()
//{
//	qDebug("onActionClearAll clicked");
//
//    // Iterate over all files in the track directory and delete them
//    QDirIterator it(trackStorageLocation(), QDir::Files | QDir::NoDotAndDotDot);
//    while (it.hasNext()) {
//        it.next();
//        QFile::remove(it.fileInfo().absoluteFilePath());
//    }
//
//    updateTrackInformation();
//
//}
//
///**
// * This method is invoked when the user clicks on the 'Delete' button
// */
//void MainController::deleteTrack(QString path)
//{
//	qDebug("deleteTrack button clicked ");
//	QFile::remove(path);
//
//	updateTrackInformation();
//
//}

void MainController::audioStatusUpdateHandler()
{

}

/**
 * Method show toasts
 */
void MainController::showToast(QString message)
{
	SystemToast *toast = new SystemToast(this);
    toast->setBody(message);
    toast->setPosition(SystemUiPosition::MiddleCenter);
    toast->show();
}


///**
// * Set up the desired format and Return Audio format object
// */
//QAudioFormat ApplicationUI::getAudioFormat() {
//
//		QAudioFormat format;
//		// Set up the desired format
//	//		8000
//	//		11025
//	//		22050
//	//		44100
//	//		48000
//		format.setSampleRate(16000);
//	//		1
//	//		2
//		format.setChannelCount(1);
//	//		8
//	//		16
//	//		32
//		format.setSampleSize(16);
//		// Qt supports only "audio/pcm"
//		format.setCodec("audio/pcm");
//		format.setByteOrder(QAudioFormat::LittleEndian);
//		format.setSampleType(QAudioFormat::UnSignedInt);
//
//		QAudioDeviceInfo info = QAudioDeviceInfo::defaultOutputDevice();
//		if (!info.isFormatSupported(format)) {
//			qWarning() << "Default format not supported.";
//			format = info.nearestFormat(format);
//		}
//
//		QStringList listCodecs;
//		listCodecs = info.supportedCodecs();
//		qDebug() << "  RRR SUPPORT codecs size = [" << listCodecs.size() << "]";
//		for (int var = 0; var < listCodecs.size(); ++var) {
//			qDebug() << "  RRR SUPPORT codecs = " << listCodecs[var];
//		}
//
//	    QList<int> listSupport;
//	    listSupport = info.supportedSampleRates();
//	    qDebug() << "  RRR SUPPORT Sample rates = [" << listSupport.size() << "]";
//	    for (int var = 0; var < listSupport.size(); ++var) {
//	    	qDebug() << "  RRR SUPPORT Sample rates = " << listSupport[var];
//		}
//
//	    listSupport = info.supportedSampleSizes();
//		qDebug() << "  RRR SUPPORT Sample size = [" << listSupport.size() << "]";
//		for (int var = 0; var < listSupport.size(); ++var) {
//			qDebug() << "  RRR SUPPORT Sample size = " << listSupport[var];
//		}
//
//	    listSupport = info.supportedChannels();
//		qDebug() << "  RRR SUPPORT Channels = [" << listSupport.size() << "]";
//		for (int var = 0; var < listSupport.size(); ++var) {
//			qDebug() << "  RRR SUPPORT Channels = " << listSupport[var];
//		}
//
//	    return format;
//}
//
///**
// * If the model is empty, we have no recorded tracks
// */
//bool ApplicationUI::hasRecordedTracks() const
//{
//    
//    return (m_model->childCount(QVariantList()) != 0);
//}
//
///**
// * Updating track list and update track list view
// */
//void ApplicationUI::updateTrackInformation()
//{
//    const bool oldHasRecordedTracks = hasRecordedTracks();
//    
//    // Clear the content of the tracks model
//    m_model->clear();
//    
//    // Reset the internal track counter
//    m_trackCounter = 0;
//    
//    qDebug() << "updateTrackInformation url: " << trackStorageLocation();
//    
//    WaveFile tempWaveFile;
//    QByteArray bstrTemp = trackTempStorageLocation().toLocal8Bit() + "tmp_file.wav";
//    const char* writePath = bstrTemp.data();
//    
//    bool succes = tempWaveFile.openWrite(writePath);
//    tempWaveFile.setupInfo(16000, 16, 1);
//    
//    // Iterate over the track storage directory and fill the model
//    QDirIterator it(trackStorageLocation(), QDir::Files | QDir::NoDotAndDotDot);
//    while (it.hasNext()) {
//        it.next();
//        
//        qDebug() << "updateTrackInformation pars file: "<< it.filePath();
//        
//        WaveFile waveFile;
//    	QByteArray bstr = it.filePath().toLocal8Bit();
//    	const char* outputPath = bstr.data();
//    	//assets:///accounts/1000/appdata/com.wiseapps.davacon.blackberry.SoundRecorder.testDev_undRecorder9a02e415/data/tracks/track006.wav
//        
//    	bool successfully = waveFile.openRead(outputPath);
//        
//		if(successfully) {
//			waveFile.showInfo();
//            
//			QVariantMap entry;
//			float dur = round(( waveFile.getDuration() / 1000.00 ) * 100 ) / 100.;
//			QString strDur = QString::number(dur, 'g', 4);
//            
//			entry["title"] = "Duration:" + strDur; // used as title in the ListView
//			entry["name"] = it.fileName(); // real track name
//			entry["url"] = it.fileInfo().absoluteFilePath(); // url
//            
//			m_model->insert(entry);
//			m_trackCounter++;
//            
//			// concat files
//			long nSampl= waveFile.getNumberOfSamples();
//			int var = 0;
//			for (var = 0; var < nSampl; ++var) {
//                
//				short sample;
//				//qDebug() << sample << " intSS " << intSS;
//				waveFile.readSample(sample);
//                
//                
//                //TODO
//                
//                
//				tempWaveFile.writeSample(sample);
//                
//			}
//            
//        } else {
//            
//        	qDebug() << "updateTrackInformation pars error";
//        }
//        
//        waveFile.close();
//        
//    }
//    
//    tempWaveFile.setupInfo(16000, 16, 1);
//    tempWaveFile.showInfo();
//    tempWaveFile.close();
//    
//    
//    QByteArray bstrSpx = trackTempStorageLocation().toLocal8Bit() + "tmp_file_spx.wav";
//    const char* writePathSpx = bstrSpx.data();
//    
//    qDebug() << "RRRRRRRRRR ";
//    
//    encodeWavToSpeexACM(writePath, writePathSpx);
//    
//    // Emit change notification signal if the hasRecordedTracks property has changed
//    if (oldHasRecordedTracks != hasRecordedTracks())
//    emit hasRecordedTracksChanged();
//}
//
///**
// * This method is invoked when the user clicks on the 'Clear All' button
// */
//void ApplicationUI::onClearAllTracks()
//{
//	qDebug("onActionClearAll clicked");
//    
//    // Iterate over all files in the track directory and delete them
//    QDirIterator it(trackStorageLocation(), QDir::Files | QDir::NoDotAndDotDot);
//    while (it.hasNext()) {
//        it.next();
//        QFile::remove(it.fileInfo().absoluteFilePath());
//    }
//    
//    updateTrackInformation();
//    
//}
//
///**
// * This method is invoked when the user clicks on the 'Delete' button
// */
//void ApplicationUI::deleteTrack(QString path)
//{
//	qDebug("deleteTrack button clicked ");
//	QFile::remove(path);
//    
//	updateTrackInformation();
//    
//}
