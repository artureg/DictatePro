#include "SEAudioStreamPlayer.h"
#include "src/Core/SEAudioStream.h"
#include <bps/bps.h>
#include <bps/audiomixer.h>
#include <bps/audiodevice.h>
#include <bps/dialog.h>
#include <bps/navigator.h>
#include <errno.h>
#include <QDebug>

SEAudioStreamPlayer::SEAudioStreamPlayer(SEAudioStream* stream, QObject *parent) :
		QThread(parent) {
	this->stream = stream;
    position = 0;
}

void SEAudioStreamPlayer::setPosition(uint position) {
    this->position = position;
}

uint SEAudioStreamPlayer::getPosition() {
    return position;
}

void SEAudioStreamPlayer::run() {
	stream->open(kSEAudioStreamModeRead);
	is_running = true;

	int rtn;

	fd_set rfds, wfds;
	bps_initialize();

	if (BPS_SUCCESS != navigator_request_events(0)) {
		fprintf(stderr, "Error requesting navigator events: %s",
				strerror(errno));
		exit(-1);
	}

	if (BPS_SUCCESS != audiodevice_request_events(0)) {
		fprintf(stderr, "Error requesting audio device events: %s",
				strerror(errno));
		exit(-1);
	}

	if (!setup_snd()) {
		return;
	}

	FD_ZERO(&rfds);
	FD_ZERO(&wfds);

	emit started();

	while (is_running) { //positionS < durationS &&
		bps_event_t *event = NULL;

		while (BPS_SUCCESS == bps_get_event(&event, 0) && event) {
			if (bps_event_get_domain(event) == navigator_get_domain()) {
				if (NAVIGATOR_EXIT == bps_event_get_code(event)) {
					goto success;
				}
			}
		}

		if (tcgetpgrp(0) == getpid()) {
			FD_SET(STDIN_FILENO, &rfds);
		}
		FD_SET(snd_mixer_file_descriptor(mixer_handle), &rfds);
		FD_SET(snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_PLAYBACK),
				&wfds);

		rtn = std::max(snd_mixer_file_descriptor(mixer_handle),
				snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_PLAYBACK));

		if (select(rtn + 1, &rfds, &wfds, NULL, NULL) == -1) {
			errorOccurred("select");
			goto fail5;
		}

		if (FD_ISSET(
				snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_PLAYBACK),
				&wfds)) {
			QByteArray bytes;
            stream->readData(bytes, position, 100);
			snd_pcm_plugin_write(pcm_handle, bytes.data(), bytes.length());
            position += 100;
            qDebug() << stream->getDuration();
            if (position > (int)stream->getDuration()) {
				is_running = false;
            } else {
                emit positionChanged(position);
            }
		}
	}
	success: snd_pcm_plugin_flush(pcm_handle, SND_PCM_CHANNEL_PLAYBACK);
	fail5: snd_mixer_close(mixer_handle);

	emit finished();

	bps_shutdown();
}

bool SEAudioStreamPlayer::setup_snd() {
	int snd_error;

	if ((snd_error = snd_pcm_open_name(&pcm_handle, "pcmPreferred",
			SND_PCM_OPEN_PLAYBACK)) < 0) {
        emit errorOccurred("snd_pcm_open_name failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	if ((snd_error = snd_pcm_info(pcm_handle, &info)) < 0) {
		close_snd();
        emit errorOccurred("snd_pcm_info failed failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	card = info.card;

	// disabling mmap
	if ((snd_error = snd_pcm_plugin_set_disable(pcm_handle, PLUGIN_DISABLE_MMAP))
			< 0) {
		close_snd();
        emit errorOccurred("snd_pcm_plugin_set_disable failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	memset(&channel_info, 0, sizeof(channel_info));
	channel_info.channel = SND_PCM_CHANNEL_PLAYBACK;
	if ((snd_error = snd_pcm_plugin_info(pcm_handle, &channel_info)) < 0) {
		close_snd();
        emit errorOccurred("snd_pcm_plugin_info failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	memset(&channel_params, 0, sizeof(channel_params));
	channel_params.mode = SND_PCM_MODE_BLOCK;
	channel_params.channel = SND_PCM_CHANNEL_PLAYBACK;
	channel_params.start_mode = SND_PCM_START_FULL;
	channel_params.stop_mode = SND_PCM_STOP_STOP;
	channel_params.buf.block.frag_size = channel_info.max_fragment_size;
	channel_params.buf.block.frags_max = -1;
	channel_params.buf.block.frags_min = 1;
	channel_params.format.interleave = 1;
	channel_params.format.rate = 8000;
	channel_params.format.voices = 1;
	channel_params.format.format = SND_PCM_SFMT_U8;

	strcpy(channel_params.sw_mixer_subchn_name, "Wave playback channel");

	if ((snd_error = snd_pcm_plugin_params(pcm_handle, &channel_params)) < 0) {
		close_snd();
        emit errorOccurred("snd_pcm_plugin_params failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	if ((snd_error = snd_pcm_plugin_prepare(pcm_handle,
			SND_PCM_CHANNEL_PLAYBACK)) < 0) {
		close_snd();
        emit errorOccurred("snd_pcm_plugin_prepare failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	memset(&channel_setup, 0, sizeof(channel_setup));
	memset(&group, 0, sizeof(group));
	channel_setup.channel = SND_PCM_CHANNEL_PLAYBACK;
	channel_setup.mixer_gid = &group.gid;

	if ((snd_error = snd_pcm_plugin_setup(pcm_handle, &channel_setup)) < 0) {
		close_snd();
        emit errorOccurred("snd_pcm_plugin_setup failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	if (group.gid.name[0] == 0) {
		close_snd();
		emit errorOccurred("Mixer Pcm Group [" + QString(group.gid.name) + "] Not Set");
		return false;
	}

	if ((snd_error = snd_mixer_open(&mixer_handle, card,
			channel_setup.mixer_device)) < 0) {
		close_snd();
		emit errorOccurred("snd_mixer_open failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	return true;
}

bool SEAudioStreamPlayer::close_snd() {
	if (snd_pcm_close(pcm_handle) < 0) {
		emit errorOccurred("snd_pcm_close failed");
		return false;
	}
	return true;
}
