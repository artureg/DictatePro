/**
 * Copyright (c) 2014 wise-apps.com
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 22.04.2014
 */

#include "com_wiseapps_davacon_speex_NativeInputStream.h"
#include <iostream>

#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "-JNI-",__VA_ARGS__)

#undef __cplusplus
#include <string.h>
#include <android/log.h>
#include <jni.h>
#include "SPEEXConverter/SpeexLib.h"
#include "NativeInputStream.h"

jlong JNI(open)
(JNIEnv *env, jclass, jstring filePath, jint format) {

	const char* filePathChar = env->GetStringUTFChars(filePath, 0);

	jlong nativeId = -1;

	NativeInputStream* stream = new NativeInputStream();
	stream->open(filePathChar, (int)format);

	nativeId = (jlong)(intptr_t) stream;

	//LOGD("open input stream = %d", nativeId);
	env->ReleaseStringUTFChars(filePath, filePathChar);

	return nativeId;

}

jint JNI(close)
(JNIEnv *env, jclass, jlong nativeId) {

	//LOGD("close input stream = %d", nativeId);

	NativeInputStream* stream;
	stream = (NativeInputStream*)nativeId;
	int result = stream->close();
	free(stream);

	return result;
}

jint JNI(getSampleRate)
(JNIEnv *, jclass, jlong nativeId) {

	//LOGD("close input stream = %d", nativeId);

	NativeInputStream* stream;
	stream = (NativeInputStream*)nativeId;

	return stream->getSampleRate();

}

jbyteArray JNI(readOne)
(JNIEnv *env, jclass, jlong nativeId, jint length) {

	NativeInputStream* stream;
	stream = (NativeInputStream*) nativeId;

	char data[48000];
	int size = stream->read(data, length);

	//LOGD("read input READone() size @ = %d", size);

	jbyteArray bArray = env->NewByteArray(size);

	env->SetByteArrayRegion(bArray, 0, size, (jbyte*)data);
	//env->ReleaseByteArrayElements(bArray, (jbyte*)data, 0);

	return bArray;
}

jbyteArray JNI(read)
(JNIEnv *env, jclass, jlong nativeId, jint offset, jint duration) {

	NativeInputStream* stream;
	stream = (NativeInputStream*) nativeId;

	char data[48000];
	int size = stream->read(data, offset, duration);

	//LOGD("read input size READ( ....) = %d", size);

	jbyteArray bArray = env->NewByteArray(size);

	env->SetByteArrayRegion(bArray, 0, size, (jbyte*)data);
	//env->ReleaseByteArrayElements(bArray, (jbyte*)data, 0);

	return bArray;

}

jint JNI(skip)
(JNIEnv *env, jclass, jlong nativeId, jint bytes) {

	NativeInputStream* stream;
	stream = (NativeInputStream*) nativeId;
	unsigned int b= (unsigned int) bytes;
	unsigned int result = stream->skip(b);

	return (jlong)(unsigned long long)result;
}
