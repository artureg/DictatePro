#include "SPEEXConverter/WaveSpeexFile.h"
#include "com_wiseapps_davacon_speex_SpeexWrapper.h"
#include <iostream>

#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "-JNI-",__VA_ARGS__)

#undef __cplusplus
#include <string.h>
#include <android/log.h>
#include <jni.h>
#include "SPEEXConverter/SpeexLib.h"

//JNIEXPORT jlong JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_getInputStream
//(JNIEnv *env, jclass, jstring wavFilePathStr, jint format)
//{
//	jclass clsInputStream = env->FindClass("java/io/InputStream");
//
//
//}
//
//
//JNIEXPORT jobject JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_getOutputStream
//(JNIEnv *env, jclass, jstring wavFilePathStr, jint format)
//{
//	jclass cls = env->FindClass("java/io/OutputStream");
//	//jmethodID methodID = (*env)->GetMethodID(env, cls, "<write>", "(I)V");
////	   (*env)->NewObject(cls, methodID, number);
//
//}

//JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_getSampleRate(JNIEnv* env, jclass, jstring compressedFilePathStr, jint format) {
//    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
//
//    WaveFile* wavFile = new WaveFile();
//    int sampleRate;
//    if (wavFile->getFMTInfo().audioFormat == 41225) {
//        wavFile->close();
//
//		WaveSpeexFile* file = new WaveSpeexFile();
//		if (!file->openRead(compressedFilePath)) {
//			file->close();
//			return 0;
//		}
//		sampleRate = file->getFMTInfo().sampleRate;
//		file->close();
//
//	} else {
//    	if (!wavFile->openRead(compressedFilePath)) {
//    		wavFile->close();
//			return 0;
//		}
//		sampleRate = wavFile->getFMTInfo().sampleRate;
//		wavFile->close();
//	}
//    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
//    return sampleRate;
//}
//
//JNIEXPORT jdouble JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_getDuration(JNIEnv* env, jclass, jstring compressedFilePathStr, jint format) {
//    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
//
//	WaveFile* wavFile = new WaveFile();
//	double duration;
//    if (wavFile->getFMTInfo().audioFormat == 41225) {
//        wavFile->close();
//
//		WaveSpeexFile* file = new WaveSpeexFile();
//		if (!file->openRead(compressedFilePath)) {
//			file->close();
//			return 0;
//		}
//		duration = file->getDuration(); // file->getDuration()*1000
//		file->close();
//
//    } else {
//    	if (!wavFile->openRead(compressedFilePath)) {
//    		wavFile->close();
//			return -1;
//		}
//
//		double duration = wavFile->getDuration();
//		wavFile->close();
//	}
//
//    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
//    return duration;
//
//}
//
//JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_getFormat(JNIEnv* env, jclass, jstring wavFilePathStr) {
//    const char* wavFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
//    WaveFile* file = new WaveFile();
//    if (!file->openRead(wavFilePath)) {
//        file->close();
//        return -1;
//    }
//    int format = file->getFMTInfo().audioFormat;
//    file->close();
//    env->ReleaseStringUTFChars(wavFilePathStr, wavFilePath);
//
//    return format;
//}
//
/*
 * Class:     com_wiseapps_davacon_speex_SpeexWrapper
 * Method:    read
 * Signature: (Ljava/lang/String;DDI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_read
  (JNIEnv *env, jclass, jstring wavFilePathStr, jdouble offset, jdouble duration, jint format){

	const char* compressedFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
	int length = 0;
	char data[48000];

	readPCMFile(compressedFilePath, offset, duration, data, &length);

	jbyteArray bArray = env->NewByteArray(length);
	env->SetByteArrayRegion(bArray, 0, length, (jbyte*)data);

	env->ReleaseByteArrayElements(bArray, (jbyte*)data, 0);
	env->ReleaseStringUTFChars(wavFilePathStr, compressedFilePath);

	return bArray;
}
//
///*
// * Class:     com_wiseapps_davacon_speex_SpeexWrapper
// * Method:    write
// * Signature: (Ljava/lang/String;[BI)I
// */
//JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_write
//  (JNIEnv *env, jclass, jstring wavFilePathStr, jbyteArray byteArray, jint format){
//
//	const char* compressedFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
//
//	jsize len  = env->GetArrayLength(byteArray);
//
////	jbyte* cData = (jbyte *)malloc(len * sizeof(jbyte));
//	jbyte* cData = (jbyte *)malloc(len);
//	env->GetByteArrayRegion(byteArray, 0, len, cData);
//
//	char a[len];
//	for(int i = 0; i < len; i++) {
//		char  byte = cData[i];
//		a[i] = byte;
//	}
//
//	int result = writePCMFile(compressedFilePath, a, len);
//
//	env->ReleaseByteArrayElements(byteArray, cData, 0);
//	free(cData);
//
//	return result;
//}
//
//JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_encode(JNIEnv* env, jclass, jstring wavFilePathStr, jstring compressedFilePathStr) {
//    const char* wavFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
//    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
//	WaveSpeexFile* file = new WaveSpeexFile();
//    if (!file->openWrite(compressedFilePath)) {
//        file->close();
//        return 1;
//    }
//    if (!file->encodeWavFile(wavFilePath, 8)) {
//        file->close();
//        return 2;
//    }
//    file->close();
//    env->ReleaseStringUTFChars(wavFilePathStr, wavFilePath);
//    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
//    return 0;
//}
//
//JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_decode(JNIEnv* env, jclass, jstring compressedFilePathStr, jstring wavFilePathStr) {
//    const char* wavFilePath = env->GetStringUTFChars(wavFilePathStr, 0);
//    const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
//    WaveSpeexFile* file = new WaveSpeexFile();
//    if (!file->openRead(compressedFilePath)) {
//        file->close();
//        return 1;
//    }
//    if (!file->decodeToWavFile(wavFilePath)) {
//        file->close();
//        return 2;
//    }
//    file->close();
//    env->ReleaseStringUTFChars(wavFilePathStr, wavFilePath);
//    env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
//    return 0;
//}
//
//JNIEXPORT jint JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_test(JNIEnv *, jclass, jint a, jint b) {
//    return (a + b);
//}

///**
// * Stream reader
// */
//JNIEXPORT jbyteArray JNICALL Java_com_wiseapps_davacon_speex_SpeexWrapper_read(JNIEnv* env, jclass,
//		jstring compressedFilePathStr, jdouble positionInMilliseconds, jdouble durationInMilliseconds, jint index) {
//	const char* compressedFilePath = env->GetStringUTFChars(compressedFilePathStr, 0);
//
//	WaveFile* wavFile = new WaveFile();
//
//	//FIXME !!!
////	long positionInMillisecondsD = positionInMillisecondsD*1000;
////	long durationInMillisecondsD = durationInMillisecondsD *1000;
////    if (!wavFile->openRead(compressedFilePath)) {
////        return false;
////    }
//    int length = 0;
////
////    if(index == 0) { //FIXME chagen format, 0 - wav
////    } else {
////    }
//
//    if (wavFile->getFMTInfo().audioFormat == 41225) {
//
//        wavFile->close();
//        WaveSpeexFile* file = new WaveSpeexFile();
//        if (!file->openRead(compressedFilePath)) {
//            file->close();
//            return false;
//        }
//
//        int datasize = durationInMilliseconds*file->getFMTInfo().sampleRate*2;
//
//        short data[datasize];
//        if (!file->decodeToData(positionInMilliseconds/1000.0f, durationInMilliseconds/1000.0f, (short*)data, &length)) {
//            file->close();
//            return false;
//        }
//        file->close();
//        jbyteArray bArray=env->NewByteArray(length);
//        env->SetByteArrayRegion(bArray, 0, length, (jbyte*)data);
//        env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
//        file->close();
//        return bArray;
//
//    } else {
//
//        int offset = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*positionInMilliseconds/1000.0f;
//        int duration = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*durationInMilliseconds/1000.0f;
//        FILE* rFile = wavFile->getFile();
//        fseek(rFile, offset, SEEK_CUR);
//        void* data;
//        switch (wavFile->getFMTInfo().bytesPerSample) {
//            case 1: {
//                char samples[duration];
////                char* samples = (char*)data;
//                int size = 0;
//                for (int i = 0; i < duration; i++) {
//                    size = i;
//                    if (feof(rFile)) {
//                        break;
//                    }
//                    unsigned char sample;
//                    wavFile->readSample(sample);
//                    samples[i] = sample;
//                }
//                length = size;
//                data = (void*)samples;
//            } break;
//            case 2: {
//                short samples[duration];
////                short* samples = (short*)data;
//                int size = 0;
//                for (int i = 0; i < duration/2; i++) {
//                    size = i*2;
//                    if (feof(rFile)) {
//                        break;
//                    }
//                    short sample;
//                    wavFile->readSample(sample);
//                    samples[i] = sample;
//                }
//                length = size;
//                data = (void*)samples;
//            }break;
//            default:
//                break;
//        }
//        jbyteArray bArray=env->NewByteArray(length);
//        env->SetByteArrayRegion(bArray, 0, length, (jbyte*)data);
//        env->ReleaseStringUTFChars(compressedFilePathStr, compressedFilePath);
//       // file->close();
//        return bArray;
//
//    }
//    return 0;
//}

