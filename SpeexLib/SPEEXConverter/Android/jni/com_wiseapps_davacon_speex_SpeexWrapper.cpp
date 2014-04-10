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

/*
 * Class:     com_wiseapps_davacon_speex_SpeexWrapper
 * Method:    read
 * Signature: (Ljava/lang/String;JJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_read(JNIEnv* env, jclass, jstring compressedFilePathStr, jlong positionInMilliseconds, jlong durationInMilliseconds) {
    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return false;
    }
    double duration = (double)durationInMilliseconds/1000.0f;
    const int size = (duration + 1)*file->getFMTInfo().sampleRate*4;
    char data[size];
    int length;
    if (!file->decodeToData((double)positionInMilliseconds/1000.0f, (double)durationInMilliseconds/1000.0f, (short*)data, &length)) {
        file->close();
        return false;
    }
    file->close();
    jbyteArray bArray=env->NewByteArray(length);
    env->SetByteArrayRegion(bArray, 0, length, (jbyte*)data);
    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
    file->close();
    
    return bArray;
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
