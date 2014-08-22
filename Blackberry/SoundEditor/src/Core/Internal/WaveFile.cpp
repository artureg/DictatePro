#include "WaveFile.h"

// constants for the canonical WAVE format
const int kFMTChunkLength = 16;                        // length of fmt contents
const int kWaveHeaderLength = 4 + 8 + kFMTChunkLength + 8; // from "WAVE" to sample data

WaveFile::WaveFile() {
	p_writeFile = NULL;
	p_riffFile = NULL;
	p_dataSize = 0;
}

WaveFile::~WaveFile() {
	close();
}

#pragma mark - Open/Close

bool WaveFile::openRead(const char *filePath) {
	if ((p_writeFile) || (p_riffFile)) {
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
			p_riffFile->pop();

			if (!p_riffFile->push("data")) {
				throw p_error = "Couldn't Find Data Chunk";
			}

			p_dataSize = p_riffFile->chunkSize();
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

bool WaveFile::openWrite(const char* filePath) {
	if ((p_writeFile) || (p_riffFile)) {
		close();
	}
	try {
		p_writeFile = fopen(filePath, "w");
		if (!p_writeFile) {
			throw p_error = "Can't create file";
		}
	} catch (...) {
		close();
		return false;
	}
	p_filePath = filePath;
	return writeHeader();
}

void WaveFile::close() {
	if (p_writeFile) {
		writeHeader();
		fclose(p_writeFile);
		p_writeFile = NULL;
	}
	if (p_riffFile) {
		delete p_riffFile;
		p_riffFile = NULL;
	}
	p_filePath = NULL;
	p_dataSize = 0;
}

#pragma mark - Info

WaveFMTInfo& WaveFile::getFMTInfo() {
	return p_fmtInfo;
}

unsigned long WaveFile::getNumberOfSamples() {
	if (p_fmtInfo.bytesPerSample > 0) {
		return p_fmtInfo.bitsPerSample ?
				p_dataSize / (p_fmtInfo.bitsPerSample/8) : 0;
	} else {
		return 0;
	}
}

unsigned long WaveFile::getNumberOfFrames() {
	return getNumberOfSamples() / p_fmtInfo.numberOfChannels;
}

double WaveFile::getDuration() {
	return (double) getNumberOfSamples() / (double) p_fmtInfo.sampleRate;
}

unsigned long WaveFile::getDataSize() {
	return p_dataSize;
}

void WaveFile::showInfo() {
	printf("-----------------------\n");
	printf("File: %s\n\n", p_filePath);
	showFMTInfo();
	printf("Data Size: %lu\n", p_dataSize);
	printf("-----------------------\n");
}

void WaveFile::showFMTInfo() {
	printf("      FMT Description:\n");
	printf("Audio Format        : %s\n",
			(p_fmtInfo.audioFormat == 1) ? "PCM" : "compressed");
	switch (p_fmtInfo.numberOfChannels) {
	case 1:
		printf("Number of Channels  : mono\n");
		break;
	case 2:
		printf("Number of Channels  : stereo\n");
		break;
	default:
		printf("Number of Channels  : %hu\n", p_fmtInfo.numberOfChannels);
		break;
	}
	printf("Sample Rate         : %lu\n", p_fmtInfo.sampleRate);
	printf("Bytes per Second    : %lu\n", p_fmtInfo.bytesPerSecond);
	printf("Bytes per Sample    : %hu\n", p_fmtInfo.bytesPerSample);
	printf("Bits per Sample     : %hu\n", p_fmtInfo.bitsPerSample);
	printf("Number of Samples   : %lu\n", getNumberOfSamples());
	printf("Duration            : %f\n", getDuration());
	printf("\n");
}

void WaveFile::setupInfo(int sampleRate, short bitsPerSample, short channels) {
	p_fmtInfo.audioFormat = 1;
	p_fmtInfo.numberOfChannels = channels;
	p_fmtInfo.sampleRate = sampleRate;
	p_fmtInfo.bytesPerSample = bitsPerSample/8;
	p_fmtInfo.bytesPerSecond = sampleRate * p_fmtInfo.bytesPerSample;
	p_fmtInfo.bitsPerSample = bitsPerSample;
	writeHeader();
}

FILE* WaveFile::getFile() {
	if (p_riffFile) {
		return p_riffFile->filep();
	} else if (p_writeFile) {
		return p_writeFile;
	} else {
		return NULL;
	}
}

#pragma mark - Read Data

bool WaveFile::readSample(unsigned char &sample) {
	if (p_fmtInfo.bitsPerSample != 8) {
		p_error = "Sample size mismatch";
		return false;
	}
	return readRaw((char*) &sample);
}

bool WaveFile::readSample(short &sample) {
	if (p_fmtInfo.bitsPerSample != 16) {
		p_error = "Sample size mismatch";
		return false;
	}
	return readRaw((char*) &sample, 2);
}

bool WaveFile::readSample(long &sample) {
	if (p_fmtInfo.bitsPerSample != 32) {
		p_error = "Sample size mismatch";
		return false;
	}
	return readRaw((char*) &sample, 4);
}

bool WaveFile::readRaw(char *buffer, size_t numBytes) {
	if (fread(buffer, 1, numBytes, p_riffFile->filep()) != numBytes) {
		p_error = "Couldn't read samples";
		return false;
	}
	return true;
}

#pragma mark - Write Data

bool WaveFile::writeHeader() {
	if (fseek(p_writeFile, 0, SEEK_SET) != 0) {
		return false;
	}

	// write the file header
	unsigned long wholeLength = kWaveHeaderLength + p_dataSize;
	unsigned long chunkLength = kFMTChunkLength;

	if ((fputs("RIFF", p_writeFile) == EOF)
			|| (fwrite(&wholeLength, sizeof(wholeLength), 1, p_writeFile) != 1)
			|| (fputs("WAVE", p_writeFile) == EOF)
			|| (fputs("fmt ", p_writeFile) == EOF)
			|| (fwrite(&chunkLength, sizeof(chunkLength), 1, p_writeFile) != 1)
			|| (fwrite(&p_fmtInfo, sizeof(WaveFMTInfo), 1, p_writeFile) != 1)
			|| (fputs("data", p_writeFile) == EOF)
			|| (fwrite(&p_dataSize, sizeof(p_dataSize), 1, p_writeFile) != 1)) {
		p_error = "Error writing header";
		return false;
	}
	return true;
}

bool WaveFile::writeRaw(char* buffer, size_t numBytes) {
	if (fwrite(buffer, 1, numBytes, p_writeFile) != numBytes) {
		p_error = "Couldn't write samples";
		return false;
	}

	p_dataSize += numBytes;

	return true;
}

bool WaveFile::writeSample(unsigned char &sample) {
	if (p_fmtInfo.bitsPerSample != 8) {
		p_error = "Sample size mismatch";
		return false;
	}

	return writeRaw((char*) &sample), sizeof(unsigned char);
}

bool WaveFile::writeSample(short& sample) {
	if (p_fmtInfo.bitsPerSample != 16) {
		p_error = "Sample size mismatch";
		return false;
	}
	return writeRaw((char*) &sample, sizeof(short));
}

bool WaveFile::writeSample(long& sample) {
	if (p_fmtInfo.bitsPerSample != 32) {
		p_error = "Sample size mismatch";
		return false;
	}
	return writeRaw((char*) &sample, sizeof(long));
}

