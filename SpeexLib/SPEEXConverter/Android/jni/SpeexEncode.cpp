#include <jni.h>
#include "../../SPEEXConverter/WaveSpeexFile.h"

jint JNICALL Java_com_speex_encode(JNIEnv *env, jstring wavFilePathStr, jstring compressedFilePathStr) {
    const char *wavFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
    const char *compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
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

jint JNICALL Java_com_speex_test(JNIEnv *env, jint a, jint b) {
     return (a + b);
}
