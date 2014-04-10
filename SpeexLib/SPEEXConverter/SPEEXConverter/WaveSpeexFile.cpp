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
#include "speex.h"

const int WAVE_BITS_PER_FRAME[3][2][11] = {
    {
        { 43, 79, 119, 160, 160, 220, 220, 300, 300, 364, 492 }, // NB mono
        { 60, 96, 136, 177, 177, 237, 237, 317, 317, 381, 509 }  // NB stereo
    },
    {
        { 79, 115, 155, 196, 256, 336, 412, 476, 556, 684, 844 }, // WB mono
        { 96, 132, 172, 213, 273, 353, 429, 493, 573, 701, 861 }  // WB stereo
    },
    {
        { 83, 151, 191, 232, 292, 372, 448, 512, 592, 720, 880 }, // UWB mono
        { 100, 168, 208, 249, 309, 389, 465, 529, 609, 737, 897 } // UWB stereo
    }
};

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
            int buffSize = p_spxInfo.frameSize*p_spxInfo.framesPerPacket;
            double secondsPerPacket = (double)buffSize/(double)p_spxInfo.sampleRate;
            p_duration = secondsPerPacket*p_dataSize/p_fmtInfo.bytesPerSample;
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
    WaveFile::close();
}

WaveFMTInfo& WaveSpeexFile::getFMTInfo() {
    return WaveFile::getFMTInfo();
}

unsigned long WaveSpeexFile::getDuration() {
    return p_duration;
}

void WaveSpeexFile::showInfo() {
    WaveFile::showInfo();
}

bool WaveSpeexFile::encodeWavFile(const char* filePath, short quality) {
    if (!p_writeFile) {
        p_error = "File is not Opened for Writing";
        return false;
    }
    WaveFile* wavFile = new WaveFile();
    if (!wavFile->openRead(filePath)) {
        p_error = wavFile->getError();
        wavFile->close();
        return false;
    }
    p_dataSize = 0;
    unsigned short numberOfChannels = wavFile->getFMTInfo().numberOfChannels;
        
    SpeexBits bits;
    void* state = 0;
    speex_bits_init(&bits);
    int mode = 1;
    switch ((int)wavFile->getFMTInfo().sampleRate) {
        case 8000:
            mode = 0;
            break;
        case 16000:
            mode = 1;
            break;
        case 32000:
            mode = 2;
            break;
        default:
            p_error = "Sample rate has to be 8000, 16000, 32000";
            wavFile->close();
            return false;
    }
    switch (mode) {
        case 0:
            state = speex_encoder_init(&speex_nb_mode);
            break;
        case 1:
            state = speex_encoder_init(&speex_wb_mode);
            break;
        case 2:
            state = speex_encoder_init(&speex_uwb_mode);
            break;
    }
    speex_encoder_ctl(state, SPEEX_SET_QUALITY, &quality);
    
    int frameSize;
    speex_encoder_ctl(state, SPEEX_GET_FRAME_SIZE, &frameSize);
    
    int framesPerPacket = WAVE_FRAME_SIZES[mode][numberOfChannels - 1][quality];
    
    FILE* rFile = wavFile->getFile();
    
    size_t bufSize = frameSize*framesPerPacket*wavFile->getFMTInfo().bitsPerSample/8;
    char buf[bufSize];
    
    int test = 0;
    
    while (!feof(rFile)) {
        bzero(buf, bufSize);
        fread(buf, sizeof(char), bufSize, rFile);
        
        for (int j = 0; j < framesPerPacket; ++j) {
            short samples[frameSize];
            bzero(samples, frameSize);
            if (wavFile->getFMTInfo().bitsPerSample == 8) {
                for (int i = 0; i < frameSize; i++) {
                    samples[i] = (buf[i + j * frameSize] - 128) << 8; // convert 8u to 16s
                }
            } else {
                for (int i = 0; i < frameSize; i++) {
                    samples[i] = buf[i + j * frameSize];
                }
            }
            
            speex_encode_int(state, (short*)samples, &bits);
        }
        int encodedSize = speex_bits_nbytes(&bits);
        test = encodedSize;
        char encoded[encodedSize];
        speex_bits_write(&bits, encoded, encodedSize);
        fwrite(encoded, sizeof(char), encodedSize, p_writeFile);
        p_dataSize += encodedSize;
        speex_bits_reset(&bits);
    }
    
    speex_bits_destroy(&bits);
    speex_encoder_destroy(state);
    
    p_fmtInfo.audioFormat = 41225;
    p_fmtInfo.numberOfChannels = numberOfChannels;
    p_fmtInfo.sampleRate = wavFile->getFMTInfo().sampleRate;
    
    p_fmtInfo.bitsPerSample = quality;
    p_fmtInfo.bytesPerSample = (short)(((framesPerPacket*WAVE_BITS_PER_FRAME[mode][numberOfChannels - 1][quality]) + 7) >> 3);
    p_fmtInfo.bytesPerSecond = p_fmtInfo.bytesPerSample*50/framesPerPacket;
    
    p_spxInfo.sampleRate = wavFile->getFMTInfo().sampleRate;
    p_spxInfo.bandMode = mode;
    p_spxInfo.numberOfChannels = numberOfChannels;
    p_spxInfo.frameSize = frameSize;
    p_spxInfo.vbr = 0;
    p_spxInfo.framesPerPacket = framesPerPacket;
    
    wavFile->close();
    writeHeader();
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
    return true;
}

bool WaveSpeexFile::decodeToData(double offsetSeconds, double durationSeconds, short* data, int* size) {
    int buffSize = p_spxInfo.frameSize*p_spxInfo.framesPerPacket;
    
    float output[buffSize];
    char cbits[p_fmtInfo.bytesPerSample];
    void* state;
    SpeexBits bits;
    
    switch (p_spxInfo.bandMode) {
        case 0:
            state = speex_decoder_init(&speex_nb_mode);
            break;
        case 1:
            state = speex_decoder_init(&speex_wb_mode);
            break;
        case 2:
            state = speex_decoder_init(&speex_uwb_mode);
            break;
        default:
            return false;
    }
    
    speex_decoder_ctl(state, SPEEX_SET_QUALITY, &p_fmtInfo.bitsPerSample);
    
    speex_bits_init(&bits);
    
    double secondsPerPacket = (double)buffSize/(double)p_spxInfo.sampleRate;
    int pos = offsetSeconds/secondsPerPacket;
    int length = durationSeconds/secondsPerPacket*2;
    
    int dataIndex = 0;
    int packetIndex = 0;
    fseek(p_riffFile->filep(), (pos - 0)*p_fmtInfo.bytesPerSample, SEEK_CUR);
    
    while ((packetIndex <= length)&&(!feof(p_riffFile->filep()))) {
        int size = fread(cbits, 1, p_fmtInfo.bytesPerSample, p_riffFile->filep());
        if ((size == 0)&&(dataIndex == 0)) {
            return false;
        }
        speex_bits_read_from(&bits, cbits, size);
        speex_decode(state, &bits, output);
        for (int i = 0; i < buffSize; i++) {
            short sample = output[i];
            data[dataIndex] = sample;
            dataIndex++;
        }
        speex_bits_reset(&bits);
        packetIndex++;
    }
    (*size) = dataIndex;
    return true;
}

bool WaveSpeexFile::decodeToWavFile(const char* filePath) {
    WaveFile* wavFile = new WaveFile();
    if (!wavFile->openWrite(filePath)) {
        p_error = wavFile->getError();
        return false;
    }
    
    wavFile->setupInfo(p_spxInfo.sampleRate, 16, p_spxInfo.numberOfChannels);
    
    int frameSize = p_spxInfo.frameSize;
    
    float output[frameSize];
    char cbits[frameSize];
    void *state;
    SpeexBits bits;
    
    switch (p_spxInfo.bandMode) {
        case 0:
            state = speex_decoder_init(&speex_nb_mode);
            break;
        case 1:
            state = speex_decoder_init(&speex_wb_mode);
            break;
        case 2:
            state = speex_decoder_init(&speex_uwb_mode);
            break;
        default:
            return false;
    }
    
    speex_decoder_ctl(state, SPEEX_SET_QUALITY, &p_fmtInfo.bitsPerSample);
    
    speex_bits_init(&bits);
    
    while (!feof(p_riffFile->filep())) {
        int size = fread(cbits, 1, p_fmtInfo.bytesPerSample, p_riffFile->filep());
        speex_bits_read_from(&bits, cbits, size);
        speex_decode(state, &bits, output);
        for (int i = 0; i < frameSize; i++) {
            short sample = output[i];
            wavFile->writeSample(sample);
        }
        speex_bits_reset(&bits);
    }
    speex_decoder_destroy(state);
    speex_bits_destroy(&bits);
    wavFile->close();
    return true;
}