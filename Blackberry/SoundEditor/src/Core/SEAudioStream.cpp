#include "SEAudioStream.h"
#include "Internal/WaveFile.h"

#include <QDebug>

SEAudioStream::SEAudioStream(QString path, QObject *parent) :
		QObject(parent) {
    qDebug() << "Loading File: " + path;
	this->path = path;
	this->file = NULL;
	this->mode = kSEAudioStreamModeNone;
}

SEAudioStream::~SEAudioStream() {
	close();
}

QString SEAudioStream::getPath() {
    return path;
}

long SEAudioStream::getDuration() {
    if (mode == kSEAudioStreamModeNone) {
        open(kSEAudioStreamModeRead);
        long duration = file->getDuration()*1000;
        close();
        return duration;
    } else {
        return file->getDuration()*1000;
    }
}

void SEAudioStream::setDescription(TSEAudioStreamDesc desc) {
	this->desc = desc;
	if (mode == kSEAudioStreamModeWrite) {
		file->setupInfo(desc.sampleRate, desc.bitsPerSample, desc.numberOfChannels);
	}
}

TSEAudioStreamDesc SEAudioStream::getDescription() {
    return desc;
}

TSEAudioStreamMode SEAudioStream::getMode() {
	return mode;
}

bool SEAudioStream::open(TSEAudioStreamMode mode) {
	if (this->mode != kSEAudioStreamModeNone) {
		return false;
	} else {
		switch (mode) {
		case kSEAudioStreamModeRead: {
			file = new WaveFile();
            bool e = file->openRead(path.toUtf8().constData());
			if (e) {
				reloadDesc();
				break;
			} else {
				return false;
			}
        }break;
        case kSEAudioStreamModeWrite: {
            file = new WaveFile();
            bool e = file->openWrite(path.toUtf8().constData());
            if (e) {
                file->setupInfo(desc.sampleRate, desc.bitsPerSample, desc.numberOfChannels);
                break;
            } else {
                return false;
            }
        }break;
		default:
			return false;
		}
		this->mode = mode;
		return true;
	}
}

void SEAudioStream::close() {
	mode = kSEAudioStreamModeNone;
	if (file) {
		file->close();
		delete file;
        file = NULL;
	}
}

void SEAudioStream::reloadDesc() {
	desc.audioFormat = file->getFMTInfo().audioFormat;
	desc.bitsPerSample = file->getFMTInfo().bitsPerSample;
	desc.bytesPerSample = file->getFMTInfo().bytesPerSample;
	desc.bytesPerSecond = file->getFMTInfo().bytesPerSecond;
	desc.numberOfChannels = file->getFMTInfo().numberOfChannels;
	desc.sampleRate = file->getFMTInfo().sampleRate;
}

bool SEAudioStream::readData(QByteArray &byteArray, long position, long duration) {
    long pos = (double) (position * desc.bytesPerSecond)/1000.0f;
    long dur = (double) (duration * desc.bytesPerSecond)/1000.0f;
	char* data = new char[dur];
	fseek(file->getFile(), 44 + pos, SEEK_SET);
	int length = fread(data, sizeof(char), dur, file->getFile());
	if (length > 0) {
		byteArray.append(data, length);
		return true;
	} else {
		return false;
	}
}

bool SEAudioStream::writeData(QByteArray &byteArray) {
    if (this->mode != kSEAudioStreamModeWrite) {
        return false;
    }
    qDebug() << byteArray.size();
    return file->writeRaw(byteArray.data(), byteArray.size());
}

bool SEAudioStream::exportToAudioStream(SEAudioStream *audioStream) {
    audioStream->close();
    close();
    if (!audioStream->open(kSEAudioStreamModeWrite)) {
        return false;
    }
    if (!open(kSEAudioStreamModeRead) ){
        return false;
    }
    long pos = 0;
    int size = 100;
    bool ret = true;
    while ((pos < getDuration())&&(ret)) {
        QByteArray data;
        ret = readData(data, pos, size);
        if (!ret) {
            break;
        }
        ret = audioStream->writeData(data);
        pos += size;
    }
    audioStream->close();
    close();
    return ret;
}
