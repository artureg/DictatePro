#include "../../SPEEXConverter/WaveSpeexFile.h"
#include "com_wiseapps_davacon_speex_SpeexWrapper.h"
#include <iostream>

JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_getSampleRate(JNIEnv* env, jclass, jstring compressedFilePathStr) {
    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return 0;
    }
    int sampleRate = file->getFMTInfo().sampleRate;
    file->close();
    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
    return sampleRate;
}

JNIEXPORT jlong JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_getDuration(JNIEnv* env, jclass, jstring compressedFilePathStr) {
    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return 0;
    }
    double duration = file->getDuration()*1000;
    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
    file->close();
    return duration;
}

JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_getFormat(JNIEnv *, jclass, jstring wavFilePathStr) {
    const char* wavFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
    WaveFile* file = new WaveFile();
    if (!file->openRead(wavFilePath)) {
        file->close();
        return -1;
    }
    int format = file->getFMTInfo().audioFormat;
    file->close();
    env->ReleaseStringUTFChars(wavFilePathStr, wavFilePath);
}

JNIEXPORT jbyteArray JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_read(JNIEnv* env, jclass, jstring compressedFilePathStr, jlong positionInMilliseconds, jlong durationInMilliseconds) {
    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
    WaveFile* wavFile = new WaveFile();
    if (!wavFile->openRead(compressedFilePath)) {
        return false;
    }
    int length = 0;
    if (wavFile->getFMTInfo().audioFormat == 41225) {
        wavFile->close();
        WaveSpeexFile* file = new WaveSpeexFile();
        if (!file->openRead(compressedFilePath)) {
            file->close();
            return false;
        }
        short data[duration/1000*file->getFMTInfo.sampleRate*2];
        if (!file->decodeToData(positionInMilliseconds/1000.0f, durationInMilliseconds/1000.0f, (short*)data, &length)) {
            file->close();
            return false;
        }
        file->close();
        jbyteArray bArray=env->NewByteArray(length);
        env->SetByteArrayRegion(bArray, 0, length, (jbyte*)data);
        env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
        file->close();
        return bArray;
    } else {
        int offset = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*positionInMilliseconds/1000.0f;
        int duration = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*durationInMilliseconds/1000.0f;
        FILE* rFile = wavFile->getFile();
        fseek(rFile, offset, SEEK_CUR);
        void* data;
        switch (wavFile->getFMTInfo().bytesPerSample) {
            case 1: {
                char samples[duration];
//                char* samples = (char*)data;
                int size = 0;
                for (int i = 0; i < duration; i++) {
                    size = i;
                    if (feof(rFile)) {
                        break;
                    }
                    unsigned char sample;
                    wavFile->readSample(sample);
                    samples[i] = sample;
                }
                length = size;
                data = (void*)samples;
            }break;
            case 2: {
                short samples[duration];
//                short* samples = (short*)data;
                int size = 0;
                for (int i = 0; i < duration/2; i++) {
                    size = i*2;
                    if (feof(rFile)) {
                        break;
                    }
                    short sample;
                    wavFile->readSample(sample);
                    samples[i] = sample;
                }
                length = size;
                data = (void*)samples;
            }break;
            default:
                break;
        }
        jbyteArray bArray=env->NewByteArray(length);
        env->SetByteArrayRegion(bArray, 0, length, (jbyte*)data);
        env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
        file->close();
        return bArray;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_encode(JNIEnv* env, jclass, jstring wavFilePathStr, jstring compressedFilePathStr) {
    const char* wavFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
	WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openWrite(compressedFilePath)) {
        file->close();
        return 1;
    }
    if (!file->encodeWavFile(wavFilePath)) {
        file->close();
        return 2;
    }
    file->close();
    env->ReleaseStringUTFChars(wavFilePathStr, wavFilePath);
    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
    return 0;
}

JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_decode(JNIEnv* env, jclass, jstring compressedFilePathStr, jstring wavFilePathStr) {
    const char* wavFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return 1;
    }
    if (!file->decodeToWavFile(wavFilePath)) {
        file->close();
        return 2;
    }
    file->close();
    env->ReleaseStringUTFChars(wavFilePathStr, wavFilePath);
    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
    return 0;
}

JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_test(JNIEnv *, jclass, jint a, jint b) {
    return (a + b);
}

