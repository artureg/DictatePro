/**
 * Copyright (c) 2014 wise-apps.com
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 22.04.2014
 */

#include <jni.h>
/**
 * Header for class com_wiseapps_davacon_speex_NativeInputStream
 */

#define JNI(X) JNIEXPORT Java_com_wiseapps_davacon_speex_NativeInputStream_##X

#ifndef _Included_com_wiseapps_davacon_speex_NativeInputStream
#define _Included_com_wiseapps_davacon_speex_NativeInputStream
#ifdef __cplusplus
extern "C" {
#endif

/**
 * Class:  com_wiseapps_davacon_speex_NativeInputStream
 * Method: open
 */
jlong JNI(open)
(JNIEnv *, jclass, jstring filePath, jint format);

/**
 * Class:  com_wiseapps_davacon_speex_NativeInputStream
 * Method: close
 */
jint JNI(close)
(JNIEnv *, jclass, jlong nativeId);

/**
 * Class:  com_wiseapps_davacon_speex_NativeInputStream
 * Method: close
 */
jint JNI(getSampleRate)
(JNIEnv *, jclass, jlong nativeId);

/**
 * Class:  com_wiseapps_davacon_speex_NativeInputStream
 * Method: read
 */
jbyteArray JNI(readOne)
(JNIEnv *, jclass, jlong nativeId, jint length);

/**
 * Class:  com_wiseapps_davacon_speex_NativeInputStream
 * Method: read
 */
jbyteArray JNI(read)
(JNIEnv *, jclass, jlong nativeId, jint offset, jint duration);

/**
 * Class:  com_wiseapps_davacon_speex_NativeInputStream
 * Method: read
 */
jint JNI(skip)
(JNIEnv *, jclass, jlong nativeId, jint bytes);

#ifdef __cplusplus
}
#endif
#endif
