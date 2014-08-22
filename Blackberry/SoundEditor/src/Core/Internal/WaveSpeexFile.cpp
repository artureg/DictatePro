//
//  WaveSpeexFile.cpp
//  SPEEXConverter
//
//  Created by Igor on 3/20/14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#include "WaveSpeexFile.h"

#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <strings.h>
#include <stdio.h>
#include <string.h>
#include "CSource/speex/speex.h"

const int WAVE_FRAME_SIZES[3][2][11] = {
    {
        { 8, 8, 8, 1, 1, 2, 2, 2, 2, 2, 2 }, // NB mono
        { 2, 1, 1, 7, 7, 8, 8, 8, 8, 3, 3 }  // NB stereo
    },
    {
        { 8, 8, 8, 2, 1, 1, 2, 2, 2, 2, 2 }, // WB mono
        { 1, 2, 2, 8, 7, 6, 3, 3, 3, 3, 3 }  // WB stereo
    },
    {
        { 8, 8, 8, 1, 2, 2, 1, 1, 1, 1, 1 }, // UWB mono
        { 2, 1, 1, 7, 8, 3, 6, 6, 5, 5, 5 }  // UWB stereo
    }
};

static const int kSpeexInfoFullLength   = 132;

WaveSpeexFile::WaveSpeexFile()
    : WaveFile() {
        p_writeState = 0;
        p_readState = 0;
}

bool WaveSpeexFile::openRead(const char *filePath) {
    if ((p_writeFile)||(p_riffFile)) {
        close();
    }
    try {
        p_riffFile = new RiffFile(filePath);
        if (!p_riffFile->filep()) {
            throw p_error = "Couldn't Open File";
        }
        if (strcmp(p_riffFile->subType(), "WAVE")) {
            throw p_error = "Not Wave File";
        }
        if (!p_riffFile->push("fmt ")) {
            throw p_error = "No FMT Chunk";
        }
        try {
            fread(&p_fmtInfo, sizeof(WaveFMTInfo), 1, p_riffFile->filep());
            fread(&p_spxInfo, sizeof(WaveSpeexFileInfo), 1, p_riffFile->filep());
            p_riffFile->pop();

            if (!p_riffFile->push("data")) {
                throw p_error = "Couldn't Find Data Chunk";
            }

            p_dataSize = p_riffFile->chunkSize();
            p_duration = (float)p_dataSize/(float)p_fmtInfo.bytesPerSecond;
            switch (p_spxInfo.bandMode) {
                case 0:
                    p_readState = speex_decoder_init(&speex_nb_mode);
                    break;
                case 1:
                    p_readState = speex_decoder_init(&speex_wb_mode);
                    break;
                case 2:
                    p_readState = speex_decoder_init(&speex_uwb_mode);
                    break;
                default:
                    return false;
            }
            p_coding.vbr = p_spxInfo.vbr;
            p_coding.enh = 1;
            p_coding.frameSize = p_spxInfo.frameSize;
            p_coding.framesPerPacket = p_spxInfo.framesPerPacket;
            p_coding.highpass = 0;
            p_coding.complexity = 0;
//            speex_decoder_ctl(p_readState, SPEEX_SET_ENH, &p_coding.enh);
        } catch (...) {
            throw p_error;
        }
        p_filePath = filePath;
    } catch (...) {
        close();
        return false;
    }
    return true;
}

bool WaveSpeexFile::openWrite(const char *filePath) {
    return WaveFile::openWrite(filePath);
}

void WaveSpeexFile::close() {
    if (p_writeState) {
        speex_encoder_destroy(p_writeState);
        p_writeState = 0;
    }
    if (p_readState) {
        speex_decoder_destroy(p_readState);
        p_readState = 0;
    }
    WaveFile::close();
}

WaveFMTInfo& WaveSpeexFile::getFMTInfo() {
    return WaveFile::getFMTInfo();
}

double WaveSpeexFile::getDuration() {
    return p_duration;
}

void WaveSpeexFile::showInfo() {
    WaveFile::showInfo();
}

void WaveSpeexFile::setupInfo(int sampleRate, int bytesPerSample, int quality) {
    if (p_writeState) {
        return;
    }
    p_coding.quality = quality;
    p_coding.complexity = 0;
    p_bytesPerSample = bytesPerSample;
    switch (sampleRate) {
        case 8000:
            p_writeState = speex_encoder_init(&speex_nb_mode);
            p_coding.mode = 0;
            break;
        case 16000:
            p_writeState = speex_encoder_init(&speex_wb_mode);
            p_coding.mode = 1;
            break;
        case 32000:
            p_writeState = speex_encoder_init(&speex_uwb_mode);
            p_coding.mode = 2;
            break;
        default:
            return;
    }

    p_coding.vbr = 0;

    p_fmtInfo.audioFormat = 41225;
    p_fmtInfo.numberOfChannels = 1;
    p_fmtInfo.sampleRate = sampleRate;
    p_fmtInfo.numberOfChannels = 1;

    speex_encoder_ctl(p_writeState, SPEEX_GET_FRAME_SIZE, &p_coding.frameSize);
    p_coding.framesPerPacket = WAVE_FRAME_SIZES[p_coding.mode][p_fmtInfo.numberOfChannels - 1][p_coding.quality];
    p_fmtInfo.bytesPerSample = 0;
    p_fmtInfo.bytesPerSecond = 0;

    p_fmtInfo.bitsPerSample = quality;

    p_spxInfo.sampleRate = sampleRate;
    p_spxInfo.bandMode = p_coding.mode;
    p_spxInfo.numberOfChannels = 1;
    p_spxInfo.frameSize = p_coding.frameSize;
    p_spxInfo.vbr = p_coding.vbr;
    p_spxInfo.framesPerPacket = p_coding.framesPerPacket;

    int bitrate = 0;
    speex_encoder_ctl(p_writeState, SPEEX_GET_BITRATE, &bitrate);
    p_fmtInfo.bytesPerSecond = bitrate/8;

}

double WaveSpeexFile::getDurationForBufferSize(unsigned long size) {
    double buffSize = p_spxInfo.frameSize*p_spxInfo.framesPerPacket;
    double secondsPerPacket = (double)buffSize/(double)p_spxInfo.sampleRate;
    return secondsPerPacket*size/p_fmtInfo.bytesPerSample;
}

unsigned int WaveSpeexFile::expectedPacketSize() {
    if (p_writeState) {
        return p_coding.frameSize*p_bytesPerSample*p_coding.framesPerPacket;
    } else {
        return 0;
    }
}

bool WaveSpeexFile::encodeWavData(const void *data, int numberOfPackets) {
    if (numberOfPackets == 0) {
        return false;
    }
    if (!p_writeState) {
        return false;
    }
    SpeexBits bits;
    speex_bits_init(&bits);
    int packetOffset = expectedPacketSize()/p_bytesPerSample;
    for (int packet = 0; packet < numberOfPackets; packet++) {
        switch (p_bytesPerSample) {
            case 1: {
                char* cData = (char*)data;
                for (int i = 0; i < p_coding.framesPerPacket; i++) {
                    short samples[p_coding.frameSize];
                    bzero(samples, p_coding.frameSize);
                    for (int j = 0; j < p_coding.frameSize; j++) {
                        int offset = packetOffset*packet + p_coding.framesPerPacket*i + j;
                        unsigned char sample = cData[offset];
                        samples[j] = (sample - 128) << 8;
                    }
                    speex_encode_int(p_writeState, (short*)samples, &bits);
                }
            }break;
            case 2: {
                short* sData = (short*)data;
                for (int i = 0; i < p_coding.framesPerPacket; i++) {
                    short samples[p_coding.frameSize];
                    bzero(samples, p_coding.frameSize);
                    for (int j = 0; j < p_coding.frameSize; j++) {
                        int offset = packetOffset*packet + p_coding.framesPerPacket*i + j;
                        short sample = sData[offset];
                        samples[j] = sample;
                    }
                    speex_encode_int(p_writeState, (short*)samples, &bits);
                }
            }break;
            default:
                break;
        }
        int encodedSize = speex_bits_nbytes(&bits);
        if (p_fmtInfo.bytesPerSample == 0) {
            p_fmtInfo.bytesPerSample = encodedSize;
        }
        char encoded[encodedSize];
        speex_bits_write(&bits, encoded, encodedSize);
        fwrite(encoded, sizeof(char), encodedSize, p_writeFile);
        p_dataSize += encodedSize;
        speex_bits_reset(&bits);
    }
    speex_bits_destroy(&bits);
    return true;
}

bool WaveSpeexFile::writeHeader() {
    if (fseek(p_writeFile, 0, SEEK_SET) != 0) {
        return false;
    }

    // write the file header
    unsigned long wholeLength = kSpeexInfoFullLength + p_dataSize;

    p_spxInfo.acmVersion = 1;
    strncpy(p_spxInfo.speexName, "Speex\0\0\0", 8);
    strncpy(p_spxInfo.speexVersion, "speex-1.0\0\0\0\0\0\0\0\0\0\0\0", 20);
    p_spxInfo.speexVersionID = 1;
    p_spxInfo.headerSize = 80;
    p_spxInfo.modeBitstreamVersion = 4;
    p_spxInfo.reserved1 = -1;
    strncpy(p_spxInfo.homepage, "\tCodec homepage www.openacm.org\0", 32);
    strncpy(p_spxInfo.somedata, "r\0", 2);
    strncpy(p_spxInfo.reserved2, "\0\0\0\0\0\0\0\0\0\0\0\0", 12);

    if ((fputs("RIFF", p_writeFile) == EOF)
        ||(fwrite(&wholeLength, sizeof(wholeLength), 1, p_writeFile) != 1)
        ||(fputs("WAVE", p_writeFile) == EOF)
        ||(fputs("fmt ", p_writeFile) == EOF)
        ||(fwrite(&kSpeexInfoFullLength, sizeof(unsigned long), 1, p_writeFile) != 1)
        ||(fwrite(&p_fmtInfo, sizeof(WaveFMTInfo), 1, p_writeFile) != 1)
        ||(fwrite(&p_spxInfo, sizeof(WaveSpeexFileInfo), 1, p_writeFile) != 1)
        ||(fputs("data", p_writeFile) == EOF)
        ||(fwrite(&p_dataSize, sizeof(p_dataSize), 1, p_writeFile) != 1)) {
        p_error = "Error writing header";
        return false;
    }
    fpos_t pos;
    fgetpos(p_writeFile, &pos);
    return true;
}

bool WaveSpeexFile::decodeToData(int offsetMilliSeconds, int durationMillSeconds, void* data, int* size) {
    char cbits[p_fmtInfo.bytesPerSample];
    SpeexBits bits;

    speex_bits_init(&bits);

    int milliSecondsPerPacket = 20*p_spxInfo.framesPerPacket;
    int pos = offsetMilliSeconds/milliSecondsPerPacket;
    int length = durationMillSeconds/milliSecondsPerPacket;

    int dataIndex = 0;
    int packetIndex = 0;
    spx_int16_t* sData = (spx_int16_t*)data;
    fseek(p_riffFile->filep(), 160 + pos*p_fmtInfo.bytesPerSample, SEEK_SET);

    while ((packetIndex < length)&&(!feof(p_riffFile->filep()))) {
        speex_bits_reset(&bits);
        int size = fread(cbits, 1, p_fmtInfo.bytesPerSample, p_riffFile->filep());
        if ((size == 0)&&(dataIndex == 0)) {
            continue;
        }
        spx_int16_t samples[p_coding.frameSize];
        speex_bits_read_from(&bits, cbits, size);

        for (int i = 0; i < p_spxInfo.framesPerPacket; i++) {
            while(speex_decode_int(p_readState, &bits, samples) == 0) {
                for (int i = 0; i < p_coding.frameSize; i++) {
                    short sample = samples[i];
                    sData[dataIndex] = sample;
                    dataIndex++;
                }
            }
        }
        packetIndex++;
    }
    (*size) = dataIndex*2;
    speex_bits_destroy(&bits);
    return true;
}
