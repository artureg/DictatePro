/**
 * Copyright (c) 2014 wise-apps.com
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 22.04.2014
 */

#include "com_wiseapps_davacon_speex_NativeOutputStream.h"
#include <iostream>

#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "-JNI-",__VA_ARGS__)

#undef __cplusplus
#include <string.h>
#include <android/log.h>
#include <jni.h>
#include "NativeOutputStream.h"

jlong JNI(open)
(JNIEnv *env, jclass, jstring filePath, jint format, jint sample_rate, jint bits_per_sample, jint channel) {

	const char* filePathChar = env->GetStringUTFChars(filePath, 0);

	jlong nativeId = -1;

	NativeOutputStream* stream = new NativeOutputStream();
	int result = stream->open(filePathChar, (int)format, (int)sample_rate, (int)bits_per_sample, (int)channel);

	if(result != -1) {
		nativeId = (jlong)(intptr_t) stream;
	}

	//LOGD("open output stream = %d", nativeId);
	env->ReleaseStringUTFChars(filePath, filePathChar);

	return nativeId;
}

jint JNI(close)
(JNIEnv *env, jclass, jlong nativeId) {

	//LOGD("close output stream = %d", nativeId);

	if(nativeId == -1) return -1;

	NativeOutputStream* stream;
	stream = (NativeOutputStream*)nativeId;
	if(stream == NULL) {
		return -1;
	}

	int result = stream->close();
	free(stream);

	return result;
}

jint JNI(write)
(JNIEnv *env, jclass, jlong nativeId, jbyteArray byteArray) {

	//LOGD("write AAAAAAAA");

	if(nativeId == -1) return -1;

	jsize len = env->GetArrayLength(byteArray);
	jbyte* data = (jbyte *) malloc(len);
	env->GetByteArrayRegion(byteArray, 0, len, data);

	char buf[len];
	for (int i = 0; i < len; i++) {
		char byte = data[i];
		buf[i] = byte;
	}

	NativeOutputStream* stream;
	stream = (NativeOutputStream*) nativeId;
	if(stream == NULL) {
		return -1;
	}

	int result = stream->write(buf, len);

	//LOGD("write output stream len= %d", result);

	env->ReleaseByteArrayElements(byteArray, data, 0);
	free(data);

	return result;
}
