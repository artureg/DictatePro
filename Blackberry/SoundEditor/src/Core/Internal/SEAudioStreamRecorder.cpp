#include "SEAudioStreamRecorder.h"
#include "src/Core/SEAudioStream.h"
#include <bps/bps.h>
#include <bps/audiomixer.h>
#include <bps/audiodevice.h>
#include <bps/dialog.h>
#include <bps/navigator.h>
#include <errno.h>
#include <QDebug>

void debugHex(QByteArray bytes) {
	QString string;
	for (int i = 0; i < bytes.length(); i++) {
		string += QString::number(bytes[i]);
	}
	qDebug() << string;
}

SEAudioStreamRecorder::SEAudioStreamRecorder(SEAudioStream* stream,
		QObject* parent) :
		QThread(parent) {
	is_running = false;
	this->stream = stream;
}

void SEAudioStreamRecorder::run() {
	this->stream->close();
	if (!this->stream->open(kSEAudioStreamModeWrite)) {
		emit errorOccurred("Can't open file");
		return;
	}
	TSEAudioStreamDesc desc;
	desc.audioFormat = 0;
	desc.bitsPerSample = 8;
	desc.bytesPerSample = 1;
	desc.numberOfChannels = 1;
	desc.sampleRate = 16000;
	desc.bytesPerSecond = desc.bitsPerSample * desc.sampleRate
			* desc.numberOfChannels;
	this->stream->setDescription(desc);

	char sample_buffer[1024];

	int bsize = 1024;
	fd_set rfds, wfds;

	bps_initialize();

	if (!setup_snd()) {
		this->stream->close();
		emit errorOccurred("Failed to start recording");
		emit finished();
		return;
	}
	is_running = true;

	FD_ZERO(&rfds);
	FD_ZERO(&wfds);

	emit started();

	if (tcgetpgrp(0) == getpid()) {
		FD_SET(STDIN_FILENO, &rfds);
	}
	FD_SET(snd_mixer_file_descriptor(mixer_handle), &rfds);
	FD_SET(snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_CAPTURE), &wfds);

	std::max(snd_mixer_file_descriptor(mixer_handle),
			snd_pcm_file_descriptor(pcm_handle, SND_PCM_CHANNEL_CAPTURE));

	while (is_running) {
		ssize_t size = snd_pcm_plugin_read(pcm_handle, sample_buffer, bsize);
		QByteArray data(sample_buffer, size);
		stream->writeData(data);

		emit positionChanged(stream->getDuration());
	}
	close_snd();

	this->stream->close();

	is_running = false;
	emit finished();

	bps_shutdown();
}

void SEAudioStreamRecorder::stop() {
	this->stream->close();
	is_running = false;
	terminate();
	close_snd();
}

bool SEAudioStreamRecorder::setup_snd() {
	int snd_error;

	if ((snd_error = snd_pcm_open_name(&pcm_handle, "pcmPreferred",
			SND_PCM_OPEN_CAPTURE)) < 0) {
		emit errorOccurred(
				"snd_pcm_open_name failed: "
						+ QString(snd_strerror(snd_error)));
		return false;
	}

	if ((snd_error = snd_pcm_info(pcm_handle, &info)) < 0) {
		close_snd();
		emit errorOccurred(
				"snd_pcm_info failed failed: "
						+ QString(snd_strerror(snd_error)));
		return false;
	}

	card = info.card;

	memset(&channel_info, 0, sizeof(channel_info));
	channel_info.channel = SND_PCM_CHANNEL_CAPTURE;
	if ((snd_error = snd_pcm_plugin_info(pcm_handle, &channel_info)) < 0) {
		close_snd();
		emit errorOccurred(
				"snd_pcm_plugin_info failed: "
						+ QString(snd_strerror(snd_error)));
		return false;
	}

	memset(&channel_params, 0, sizeof(channel_params));
	channel_params.mode = SND_PCM_MODE_BLOCK;
	channel_params.channel = SND_PCM_CHANNEL_CAPTURE;
	channel_params.start_mode = SND_PCM_START_FULL;
	channel_params.stop_mode = SND_PCM_STOP_STOP;
	channel_params.buf.block.frag_size = channel_info.max_fragment_size;

	channel_params.buf.block.frags_max = -1;
	channel_params.buf.block.frags_min = 1;
	channel_params.format.interleave = 1;
	channel_params.format.rate = stream->getDescription().sampleRate;
	channel_params.format.voices = stream->getDescription().numberOfChannels;
	if (stream->getDescription().bitsPerSample == 8) {
		channel_params.format.format = SND_PCM_SFMT_U8;
	} else {
		channel_params.format.format = SND_PCM_SFMT_S16;
	}

	strcpy(channel_params.sw_mixer_subchn_name, "Wave playback channel");

	if ((snd_error = snd_pcm_plugin_params(pcm_handle, &channel_params)) < 0) {
		close_snd();
		emit errorOccurred(
				"snd_pcm_plugin_params failed: "
						+ QString(snd_strerror(snd_error)));
		return false;
	}

	if ((snd_error = snd_pcm_plugin_prepare(pcm_handle, SND_PCM_CHANNEL_CAPTURE))
			< 0) {
		close_snd();
		emit errorOccurred(
				"snd_pcm_plugin_prepare failed: "
						+ QString(snd_strerror(snd_error)));
		return false;
	}

	memset(&channel_setup, 0, sizeof(channel_setup));
	memset(&group, 0, sizeof(group));
	channel_setup.channel = SND_PCM_CHANNEL_CAPTURE;
	channel_setup.mixer_gid = &group.gid;

	if ((snd_error = snd_pcm_plugin_setup(pcm_handle, &channel_setup)) < 0) {
		close_snd();
		emit errorOccurred(
				"snd_pcm_plugin_setup failed: "
						+ QString(snd_strerror(snd_error)));
		return false;
	}

	if (group.gid.name[0] == 0) {
		close_snd();
		emit errorOccurred(
				"Mixer PCM Group [" + QString(group.gid.name) + "] Not Set");
		return false;
	}

	if ((snd_error = snd_mixer_open(&mixer_handle, card,
			channel_setup.mixer_device)) < 0) {
		close_snd();
		emit errorOccurred(
				"snd_mixer_open failed: " + QString(snd_strerror(snd_error)));
		return false;
	}

	return true;
}

bool SEAudioStreamRecorder::close_snd() {
	if (snd_pcm_close(pcm_handle) < 0) {
		emit errorOccurred("snd_pcm_close failed");
		return false;
	}
	return true;
}
