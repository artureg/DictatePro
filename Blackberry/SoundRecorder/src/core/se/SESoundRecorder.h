/*
 * SESoundRecorder.h
 *
 *  Created on: 06.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SESoundRecorder_H_
#define SESoundRecorder_H_

#include <qobject.h>

#include <errno.h>
#include <fcntl.h>
#include <gulliver.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/slogcodes.h>
#include <ctype.h>
#include <limits.h>
#include <screen/screen.h>
#include <sys/asoundlib.h>

#include <bps/bps.h>
#include <bps/audiomixer.h>
#include <bps/audiodevice.h>
#include <bps/dialog.h>
#include <bps/navigator.h>

#include <bb/multimedia/MediaPlayer>

#include "SEAudioStream.h"
//#include <QAudioOutput>
//#include <QByteArray>
//#include <QComboBox>

#define CONTEXT_NAME "wise-apps-player"

#define WAV_RELATIVE_PATH "/data/dev_sam.wav" // sample
#define WAV_RELATIVE_PATH_S "dev_sam.wav"

/**
 * Player stream worker
 */
class SESoundRecorder: public QObject {

	Q_OBJECT

	private:

		// snd
		snd_pcm_t *pcm_handle;
		snd_pcm_info_t info;
		snd_pcm_channel_params_t pp;
		snd_pcm_channel_setup_t setup;
		snd_pcm_channel_info_t pi;
		snd_mixer_t *mixer_handle;
		snd_mixer_group_t group;
		int card;

		bool is_running;

		QString lastError;

		/** general setup of the libasound audio mixer and pcm components */
		bool setup_snd();

		/** close pcm components */
		bool close_snd();

		int errorOccurred(QString message);

		int sample_rate;
		int sample_channels;
		int sample_bits;

		SEAudioStream *audioStream;

//		QAudioOutput *audioOutput;

	public:
		SESoundRecorder();
		virtual ~SESoundRecorder();

		/** initialization of player */
		bool initWithAudioStream(SEAudioStream &audioStream);

	public Q_SLOTS:
		/** start playing */
		void work();

		/** stop playing */
		void stop();

		/** set position */
		void setPosition(unsigned int position);

	Q_SIGNALS:

		/** signal is raised when the player has been terminated */
		void started();

		/** signal is raised when the player has been terminated */
		void finished();

		/** signal is raised when an error occurred */
		void error(const char *message);

		/** signal is raised when an position is changed */
		void positionChanged(unsigned int position);
};

#endif /* SESoundRecorder_H_ */
