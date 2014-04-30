/**
 * Copyright (c) 2014 wise-apps.com
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 22.04.2014
 */

#include <jni.h>
/**
 * Header for class com_wiseapps_davacon_speex_NativeOutputStream
 *
 */

#define JNI(X) JNIEXPORT Java_com_wiseapps_davacon_speex_NativeOutputStream_##X

#ifndef _Included_com_wiseapps_davacon_speex_NativeOutputStream
#define _Included_com_wiseapps_davacon_speex_NativeOutputStream
#ifdef __cplusplus
extern "C" {
#endif

/**
 * Class:  com_wiseapps_davacon_speex_NativeOutputStream
 * Method: open
 */
jlong JNI(open)
(JNIEnv *, jclass, jstring filePath, jint format, jint sample_rate, jint bits_per_sample, jint channel);

/**
 * Class:  com_wiseapps_davacon_speex_NativeOutputStream
 * Method: close
 */
jint JNI(close)
(JNIEnv *, jclass, jlong nativeId);

/**
 * Class:  com_wiseapps_davacon_speex_NativeOutputStream
 * Method: write
 */
jint JNI(write)
(JNIEnv *, jclass, jlong nativeId, jbyteArray byteArray);

#ifdef __cplusplus
}
#endif
#endif
