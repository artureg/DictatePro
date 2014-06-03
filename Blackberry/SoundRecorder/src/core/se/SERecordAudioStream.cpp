/*
 * SERecordAudioStream.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SERecordAudioStream.h"
#include "utils/Loger.h"

SERecordAudioStream::SERecordAudioStream() {

	waveFile = new WaveFile();
}

SERecordAudioStream::~SERecordAudioStream() {}

void SERecordAudioStream::initWithRecord(const SERecord *_record) {

    Loger::Debug("SERecordAudioStream", "initWithRecord");

    record = _record;

    startPositionInMiils = record->soundRange.start;
    durationInMills = record->soundRange.duration;
    pathFile = record->soundUrl;

    qDebug() << "SERecordAudioStream" << "startPositionInMiils = " << startPositionInMiils;
    qDebug() << "SERecordAudioStream" << "durationInMills = " << durationInMills;
    qDebug() << "SERecordAudioStream" << "pathFile = " << pathFile;

}

bool SERecordAudioStream::open(SEAudioStreamMode _mode) {

    Loger::Debug("SERecordAudioStream", "open");

	mode = _mode;

    if(mode == modeRead) {

        Loger::Debug("SERecordAudioStream", "mode == modeRead");

        waveFile = new WaveFile();

        if (!waveFile->openRead(pathFile)) {
            waveFile->close();
            Loger::Debug("SERecordAudioStream", "cannot open the Wav file for reading");
            return false;
        }

        if(waveFile->getFMTInfo().audioFormat == 41225) {
             waveFile->close();
             Loger::Debug("SERecordAudioStream", "speex format was detected");
             waveSpeexFile = new WaveSpeexFile();
             if (!waveSpeexFile->openRead(pathFile)) {
                waveSpeexFile->close();
                Loger::Debug("SERecordAudioStream", "cannot open the Speex file for reading");
                return false;
            }
            format = formatSpeex;
        } else {
            Loger::Debug("SERecordAudioStream", "wav format was detected");
            format = formatWav;
        }

    } else if(mode == modeWrite) {

        Loger::Debug("SERecordAudioStream", "mode == modeWrite");

        if(format == formatWav) {
            waveFile = new WaveFile();
            if (!waveFile->openWrite(pathFile)) {
                waveFile->close();
                Loger::Debug("SERecordAudioStream", "cannot open the Wav file for writing");
                return false;
            }
            Loger::Debug("SERecordAudioStream", "wav format was detected");
        } else if(format == formatSpeex) {
            waveSpeexFile = new WaveSpeexFile();
            if (!waveSpeexFile->openWrite(pathFile)) {
                waveSpeexFile->close();
                Loger::Debug("SERecordAudioStream", "cannot open the Speex file for writing");
                return false;
            }
            Loger::Debug("SERecordAudioStream", "speex format was detected");
        } else {
            Loger::Debug("SERecordAudioStream", "please, set up the format before writing");
            return false;
        }

    }

    Loger::Debug("SERecordAudioStream", "open - successfully");
	return true;
}

void SERecordAudioStream::close() {

    Loger::Debug("SERecordAudioStream", "close");

    if(format = formatWav) {
        waveFile->close();
        free(waveFile);
    } else if(format = formatSpeex) {
        waveSpeexFile->close();
        free(waveSpeexFile);
    } else {
        Loger::Debug("SERecordAudioStream", "cannot close because the format was not set up");
    }

}

bool SERecordAudioStream::clear() {
    Loger::Debug("SERecordAudioStream", "clear");
    startPositionInMiils = 0;
    durationInMills = 0;
    pathFile = 0;
    mode = modeUnknown;
    format = formatUnknown;
	return true;
}

bool SERecordAudioStream::write(char* data) {

    Loger::Debug("SERecordAudioStream", "write");

    if(mode != modeWrite) {
        Loger::Debug("SERecordAudioStream", "the mode is not WRITE");
        return 0;
    }

    if(format == formatWav) {

        //	//FIXME WAV format
        //	short* samples = (short*) data;

    } else if(format == formatSpeex) {}

	return true;
}

unsigned int SERecordAudioStream::read(char* data, unsigned int position, unsigned int duration) {

    Loger::Debug("SERecordAudioStream", "read");

    if(position >= durationInMills) {
        Loger::Debug("SERecordAudioStream", "finish");
        return 0;
    }

    if(mode != modeRead) {
        Loger::Debug("SERecordAudioStream", "the mode is not READ");
        return 0;
    }

    position+=startPositionInMiils;

    if(position + duration > durationInMills) {
        duration = position + duration - durationInMills;
    }

    qDebug() << "SERecordAudioStream" << "position = " << position;
    qDebug() << "SERecordAudioStream" << "duration = " << duration;
    qDebug() << "SERecordAudioStream" << "currentPositionMills = " << currentPositionMills;

    //FIXME should be outside
    if(format == formatWav) {

        unsigned int positionInByte = position * (waveFile->getFMTInfo().sampleRate * 2);
        unsigned int durationInByte = duration * (waveFile->getFMTInfo().sampleRate * 2); // - positionInByte;

        qDebug() << "SERecordAudioStream" << "positionInByte = " << positionInByte;
        qDebug() << "SERecordAudioStream" << "durationInByte = " << durationInByte;
        qDebug() << "SERecordAudioStream" << "sampleRate = " << waveFile->getFMTInfo().sampleRate;

        FILE* rFile = waveFile->getFile();
        fseek(rFile, positionInByte, SEEK_SET);
        size_t size = fread(data, sizeof(waveFile->getFMTInfo().bytesPerSample), durationInByte, rFile);

        currenPositionInByte +=size;
        currentPositionMills = position;

        return size;

    } else if(format == formatSpeex) {

      short samples[48000];

        int size = 0;

        bool result = speexFile->decodeToData(position, duration, samples, &size);

        if(!result) {
            return -1;
        }

        char* t_data = (char*) samples;

        for(int i=0; i< size; i++) {
            data[i] = t_data[i];
        }

        if(size > length) {
            curPositionInBytes = curPositionInBytes + length;
            return length;
        } else {
            curPositionInBytes = curPositionInBytes + size;
        }

        return size;

    }

    return 0;
}
