/*
 * SESoundRecorder.cpp
 *
 *  Created on: 06.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SESoundRecorder.h"

#include "core/SPEEXConverter/WaveFile.h"
#include "utils/Loger.h"

SESoundRecorder::SESoundRecorder() : card(-1), is_running(false) {}

SESoundRecorder::~SESoundRecorder() {}

bool SESoundRecorder::initWithAudioStream(SEAudioStream &audioStream)
{
	Loger::Debug(typeid(this).name(), "initWithAudioStream");
	this->audioStream = &audioStream;
	return true;
}

bool SESoundRecorder::setup_snd()
{
	Loger::Debug(typeid(this).name(), "setup_snd()");

    int snd_error;

    if ((snd_error = snd_pcm_open_name(&pcm_handle, "pcmPreferred", SND_PCM_OPEN_CAPTURE)) < 0) {
        errorOccurred("snd_pcm_open_name failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    if ((snd_error = snd_pcm_info(pcm_handle, &info)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_info failed failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    card = info.card;

    memset(&pi, 0, sizeof(pi));
    pi.channel = SND_PCM_CHANNEL_CAPTURE;
    if ((snd_error = snd_pcm_plugin_info(pcm_handle, &pi)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_plugin_info failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    memset(&pp, 0, sizeof(pp));
    pp.mode = SND_PCM_MODE_BLOCK;
    pp.channel = SND_PCM_CHANNEL_CAPTURE;
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

    if ((snd_error = snd_pcm_plugin_prepare(pcm_handle, SND_PCM_CHANNEL_CAPTURE)) < 0) {
    	close_snd();
        errorOccurred("snd_pcm_plugin_prepare failed: " + QString(snd_strerror(snd_error)));
        return false;
    }

    memset(&setup, 0, sizeof(setup));
    memset(&group, 0, sizeof(group));
    setup.channel = SND_PCM_CHANNEL_CAPTURE;
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
	QString a =  QString(workingDir +  "/data/devacon/records/dev_sam.wav");
	const char *cMsg =a.toStdString().c_str();

	Loger::Debug("@@@@@", cMsg);
	return a;
}

bool SESoundRecorder::close_snd() {
	if(snd_pcm_close(pcm_handle) < 0) {
		errorOccurred("snd_pcm_close failed");
		return false;
	}

	return true;
}

void SESoundRecorder::work() {

	is_running = true;

	WaveFile wavTestl;

	QByteArray bstrTemp = trackTempStorageLocation().toLocal8Bit();
	const char* writePath = bstrTemp.data();

	if (!wavTestl.openWrite(writePath)) {
		qDebug() << "wav can not be open = " << writePath;
	}

	wavTestl.setupInfo(16000, 8, 1);

	FILE *file;
	int samples;
	char *sample_buffer;

	int rtn;
	int exit_application = 0;

	int bsize, bytes_read, total_written = 0;
	fd_set rfds, wfds;

	bps_initialize();

	if (!setup_snd()) {
		return;
	}

	bsize = 1024;
	samples = wavTestl.getDataSize();
	sample_buffer = (char*) malloc(bsize);
	if (!sample_buffer) {
		goto fail3;
	}

	FD_ZERO(&rfds);
	FD_ZERO(&wfds);
	bytes_read = 1;

	emit started();

	while (is_running) {

		bps_event_t *event = NULL;
		Loger::Debug(typeid(this).name(), "work() processing...");

//		while (BPS_SUCCESS == bps_get_event(&event, 0) && event) {
//			/*
//			 * If it is a NAVIGATOR_EXIT event then we are done so stop
//			 * processing events, clean up and exit
//			 */
//			if (bps_event_get_domain(event) == navigator_get_domain()) {
//				if (NAVIGATOR_EXIT == bps_event_get_code(event)) {
//					exit_application = 1;
//					goto success;
//				}
//			}
//
//			if (bps_event_get_domain(event) == audiodevice_get_domain()) {
//				/*
//				 * If it is a audio device event then it means a new audio device
//				 * has been enabled and a switch is required.  We close the old,
//				 * open the new audio device using the path and get the card number so
//				 * that we can close and re-open the mixer with the new card
//				 * number.
//				 */
////
////	                const char * audiodevice_path = audiodevice_event_get_path(event);
////
////	                if (NULL == audiodevice_path) {
////	                    snprintf(msg, MSG_SIZE, "audiodevice_event_get_path failed: %s\n", snd_strerror(rtn));
////	                    //show_dialog_message(msg);
////	                    goto fail5;
////	                }
////
////	                if ((rtn = snd_mixer_close(mixer_handle)) < 0) {
////	                    snprintf(msg, MSG_SIZE, "snd_mixer_close failed: %s\n", snd_strerror(rtn));
////	                  //  show_dialog_message(msg);
////	                    goto fail4;
////	                }
////
////	                if ((rtn = snd_pcm_close(pcm_handle)) < 0) {
////	                    snprintf(msg, MSG_SIZE, "snd_pcm_close failed: %s\n", snd_strerror(rtn));
////	                   // show_dialog_message(msg);
////	                    goto fail3;
////	                }
////
////	                if (setup_snd(audiodevice_path)) {
////	                    /*
////	                     * setup_snd() closes pcm and mixer handles in the case of error so we
////	                     * skip clean up of the handles in the case of failure.
////	                     */
////	                    goto fail3;
////	                }
//	            }
//	        }

		if (tcgetpgrp(0) == getpid()) {
			FD_SET(STDIN_FILENO, &rfds);
		}
		FD_SET(snd_mixer_file_descriptor(mixer_handle), &rfds);
		FD_SET(snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_CAPTURE), &wfds);

		Loger::Debug(typeid(this).name(), "work() processing 1111...");

		rtn = std::max(snd_mixer_file_descriptor(mixer_handle),
		snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_CAPTURE));

        Loger::Debug(typeid(this).name(), "work() processing 2...");
        snd_pcm_plugin_read(pcm_handle, sample_buffer, 1024);

        Loger::Debug(typeid(this).name(), "work() processing 3...");

        wavTestl.writeRaw(sample_buffer, 1024);

        Loger::Debug(typeid(this).name(), "work() processing 4...");

		emit positionChanged(125);
		Loger::Debug(typeid(this).name(), "work() processing end");
	}

	wavTestl.close();

	success:
	    bytes_read = snd_pcm_plugin_flush(pcm_handle, SND_PCM_CHANNEL_CAPTURE);
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

int SESoundRecorder::errorOccurred(QString message)
{
	lastError = message;
	const char *cMsg = message.toStdString().c_str();
	Loger::Debug(typeid(this).name(), cMsg);
	emit error(cMsg);
    return -1;
}

void SESoundRecorder::stop()
{
	Loger::Debug(typeid(this).name(), "stop");
	is_running = false;
}

void SESoundRecorder::setPosition(unsigned int position)
{
	char c[20];
	sprintf(c, "%d", position);
	Loger::Debug(typeid(this).name(), c);
}
