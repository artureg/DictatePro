#include "SEProjectAudioStream.h"
#include "SEProject.h"

SEProjectAudioStream::SEProjectAudioStream(SEProject* project, QObject *parent) :
		SEAudioStream("/accounts/1000/shared/misc/1.wav", parent) {
	this->project = project;
//    TSEAudioStreamDesc desc;
//    desc.audioFormat = 0;
//    desc.bitsPerSample = 8;
//    desc.bytesPerSample = 1;
//    desc.numberOfChannels = 1;
//    desc.sampleRate = 8000;
//    desc.bytesPerSecond = desc.bitsPerSample*desc.sampleRate*desc.numberOfChannels;
//    this->setDescription(desc);
}

//bool SEProjectAudioStream::readData(QByteArray &byteArray, long position, long duration) {
//    return true;
//}
