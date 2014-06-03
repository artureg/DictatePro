/*
 * SESoundPlayer.cpp
 *
 *  Created on: 06.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SESoundPlayer.h"

#include "src/core/SPEEXConverter/WaveSpeexFile.h"
#include "utils/Loger.h"

SESoundPlayer::SESoundPlayer() : card(-1), is_running(false) {}

SESoundPlayer::~SESoundPlayer() {}

bool SESoundPlayer::initWithAudioStream(SEAudioStream *audioStream)
{
	Loger::Debug(typeid(this).name(), "initWithAudioStream");
    this->audioStream = audioStream;
	return true;
}

bool SESoundPlayer::setup_snd()
{
	Loger::Debug(typeid(this).name(), "setup_snd()");

    int snd_error;

    if ((snd_error = snd_pcm_open_name(&pcm_handle, "pcmPreferred", SND_PCM_OPEN_PLAYBACK)) < 0) {
        errorOccurred("snd_pcm_open_name failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    if ((snd_error = snd_pcm_info(pcm_handle, &info)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_info failed failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    card = info.card;

    // disabling mmap
    if ((snd_error = snd_pcm_plugin_set_disable(pcm_handle, PLUGIN_DISABLE_MMAP)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_plugin_set_disable failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    memset(&pi, 0, sizeof(pi));
    pi.channel = SND_PCM_CHANNEL_PLAYBACK;
    if ((snd_error = snd_pcm_plugin_info(pcm_handle, &pi)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_plugin_info failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    memset(&pp, 0, sizeof(pp));
    pp.mode = SND_PCM_MODE_BLOCK;
    pp.channel = SND_PCM_CHANNEL_PLAYBACK;
    pp.start_mode = SND_PCM_START_FULL;
    pp.stop_mode = SND_PCM_STOP_STOP;
    pp.buf.block.frag_size = pi.max_fragment_size;
    pp.buf.block.frags_max = -1;
    pp.buf.block.frags_min = 1;
    pp.format.interleave = 1;
    pp.format.rate = 16000;
    pp.format.voices = 1;
    pp.format.format = SND_PCM_SFMT_S16_LE;

    strcpy(pp.sw_mixer_subchn_name, "Wave playback channel");

    if ((snd_error = snd_pcm_plugin_params(pcm_handle, &pp)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_plugin_params failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    if ((snd_error = snd_pcm_plugin_prepare(pcm_handle, SND_PCM_CHANNEL_PLAYBACK)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_plugin_prepare failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    memset(&setup, 0, sizeof(setup));
    memset(&group, 0, sizeof(group));
    setup.channel = SND_PCM_CHANNEL_PLAYBACK;
    setup.mixer_gid = &group.gid;

    if ((snd_error = snd_pcm_plugin_setup(pcm_handle, &setup)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_plugin_setup failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    if (group.gid.name[0] == 0) {
    	close_snd();
    	errorOccurred("Mixer Pcm Group [" + QString(group.gid.name) + "] Not Set");
        return false;
    }

    if ((snd_error = snd_mixer_open(&mixer_handle, card, setup.mixer_device)) < 0) {
    	close_snd();
        errorOccurred("snd_mixer_open failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    Loger::Debug(typeid(this).name(), "setup_snd() - successfully");

    return true;
}

/**
 * method to return the directory path where
 * temp recorded are stored.
 */
static QString trackTempStorageLocation()
{
	QString workingDir = QDir::currentPath();
    QString a =  QString(workingDir +  "/data/devacon/rec_spx.wav");
	const char *cMsg =a.toStdString().c_str();

	Loger::Debug("@@@@@", cMsg);
	return a;
}

bool SESoundPlayer::close_snd() {
	if(snd_pcm_close(pcm_handle) < 0) {
		errorOccurred("snd_pcm_close failed");
		return false;
	}

	return true;
}

void SESoundPlayer::work() {

	is_running = true;

//	WaveFile wavTestl;
    WaveSpeexFile waveSpeex;

	QByteArray bstrTemp = trackTempStorageLocation().toLocal8Bit();
	const char* writePath = bstrTemp.data();

//	if (!wavTestl.openRead(writePath)) {
//		qDebug() << "wav can not be open = " << writePath;
//	}

    if (!waveSpeex.openRead(writePath)) {
        qDebug() << "wav can not be open = " << writePath;
    }

//	qDebug() << "SSS dur" << wavTestl.getDuration();
//	qDebug() << "SSS sample rate" << wavTestl.getFMTInfo().sampleRate;
//	qDebug() << "SESoundPlayer::work()";

	FILE *file;
    int samples;
	char *sample_buffer;

	int rtn;
	int exit_application = 0;

	int bsize, bytes_read, total_written = 0;
	fd_set rfds, wfds;
    unsigned int positionS = 0;
    unsigned int durationS = ( waveSpeex.getDuration() * 1000 );

    qDebug() << "SSS INIT position" << positionS;
    qDebug() << "SSS INIT duration" << durationS;
    qDebug() << "SSS INIT sampleRate" << waveSpeex.getFMTInfo().sampleRate;

	bps_initialize();

	if (BPS_SUCCESS != navigator_request_events(0)) {
		fprintf(stderr, "Error requesting navigator events: %s", strerror(errno));
		exit(-1);
	}

	if (BPS_SUCCESS != audiodevice_request_events(0)) {
		fprintf(stderr, "Error requesting audio device events: %s", strerror(errno));
		exit(-1);
	}

	if (!setup_snd()) {
		return;
	}

	bsize = 1024;

	FD_ZERO(&rfds);
	FD_ZERO(&wfds);
	bytes_read = 1;

	emit started();

    while (is_running) { //positionS < durationS &&

        qDebug() << "SSS progress position" << positionS;
        qDebug() << "SSS progress duration" << durationS;

		bps_event_t *event = NULL;
		Loger::Debug(typeid(this).name(), "work() processing...");

		while (BPS_SUCCESS == bps_get_event(&event, 0) && event) {
			/*
			 * If it is a NAVIGATOR_EXIT event then we are done so stop
			 * processing events, clean up and exit
			 */
			if (bps_event_get_domain(event) == navigator_get_domain()) {
				if (NAVIGATOR_EXIT == bps_event_get_code(event)) {
					exit_application = 1;
					goto success;
				}
			}
        }

		if (tcgetpgrp(0) == getpid()) {
			FD_SET(STDIN_FILENO, &rfds);
		}
		FD_SET(snd_mixer_file_descriptor(mixer_handle), &rfds);
		FD_SET(snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_PLAYBACK), &wfds);

		rtn = std::max(snd_mixer_file_descriptor(mixer_handle),
		snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_PLAYBACK));

		if (select(rtn + 1, &rfds, &wfds, NULL, NULL) == -1) {
			errorOccurred("select");
			goto fail5;
		}

		if (FD_ISSET(snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_PLAYBACK), &wfds)) {
			snd_pcm_channel_status_t status;
			int written = 0;

            short samples2[48000];
            int size = 0;
            bool result = waveSpeex.decodeToData(positionS, 200, samples2, &size);

            written = snd_pcm_plugin_write(pcm_handle, samples2,
                    size);
            positionS += 200;
            if(positionS > durationS) {
                is_running = false;
            }

		}
		emit positionChanged(125);
		Loger::Debug(typeid(this).name(), "work() processing end");
	}

//    wavTestl.close();
    waveSpeex.close();
	success:
	    bytes_read = snd_pcm_plugin_flush(pcm_handle, SND_PCM_CHANNEL_PLAYBACK);
	fail5:
	    snd_mixer_close(mixer_handle);
	fail4:
	    snd_pcm_close(pcm_handle);
	fail3:
	    free(sample_buffer);
	    sample_buffer = NULL;

	emit finished();

	bps_shutdown();
}

int SESoundPlayer::errorOccurred(QString message)
{
	lastError = message;
	const char *cMsg = message.toStdString().c_str();
	Loger::Debug(typeid(this).name(), cMsg);
	emit error(cMsg);
    return -1;
}

void SESoundPlayer::stop()
{

	Loger::Debug(typeid(this).name(), "stop");
	is_running = false;
}

void SESoundPlayer::setPosition(unsigned int position)
{
	char c[20];
	sprintf(c, "%d", position);
	Loger::Debug(typeid(this).name(), c);
}
