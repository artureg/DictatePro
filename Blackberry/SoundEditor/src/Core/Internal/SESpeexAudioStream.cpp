#include "SESpeexAudioStream.h"
#include "WaveSpeexFile.h"
#include <QDebug>

SESpeexAudioStream::SESpeexAudioStream(QString path, QObject *parent) :
    SEAudioStream(path, parent) {
    file = NULL;
}

long SESpeexAudioStream::getDuration() {
    if (file) {
        return file->getDuration()*1000;
    } else {
        return duration;
    }
}

bool SESpeexAudioStream::writeData(QByteArray &byteArray) {
    if (this->mode != kSEAudioStreamModeWrite) {
        return false;
    }
    data.append(byteArray);
    return true;
}

bool SESpeexAudioStream::open(TSEAudioStreamMode mode) {
    if (this->mode != kSEAudioStreamModeNone) {
        return false;
    }
    if (file) {
        delete file;
    }
    file = new WaveSpeexFile();
    this->mode = mode;
    switch (mode) {
    case kSEAudioStreamModeRead: {
        bool ret = file->openRead(path.toUtf8().data());
        if (ret) {
            duration = file->getDuration()*1000;
            TSEAudioStreamDesc desc;
            desc.audioFormat = 0;
            desc.bitsPerSample = 16;
            desc.bytesPerSample = 1;
            desc.numberOfChannels = 1;
            desc.sampleRate = file->getFMTInfo().sampleRate;
            desc.bytesPerSecond = desc.bitsPerSample/8*desc.sampleRate*desc.numberOfChannels;
            setDescription(desc);
        }
        return ret;
    }
    case kSEAudioStreamModeWrite: {
        bool ret = file->openWrite(path.toUtf8().data());
        if (ret) {
            file->setupInfo(desc.sampleRate, desc.bitsPerSample/8, 8);
        }
        data.clear();
        return ret;
    }
    default:
        return false;
    }
}

void SESpeexAudioStream::close() {
    if (this->mode == kSEAudioStreamModeNone) {
        return;
    }
    if (file->expectedPacketSize() != 0) {
        qDebug() << "Number Of Speex Packets: " + QString::number(data.length()/file->expectedPacketSize());
        if ((this->mode == kSEAudioStreamModeWrite)&&(data.length()/file->expectedPacketSize() > 0)) {
            file->encodeWavData(data.data(), data.length()/file->expectedPacketSize());
            data.clear();
        }
    }
    file->close();
    delete file;
    file = NULL;
    this->mode = kSEAudioStreamModeNone;
}

bool SESpeexAudioStream::readData(QByteArray &byteArray, long position, long duration) {
    if (this->mode != kSEAudioStreamModeRead) {
        return false;
    }
    char data[100000];
    int size = 0;
    bool ret = file->decodeToData(position, duration, (void*)data, &size);
    byteArray.append(data, size);
    return ret;
}
