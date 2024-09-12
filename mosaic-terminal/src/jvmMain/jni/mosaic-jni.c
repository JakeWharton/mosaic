#include "cutils.h"
#include "jni.h"
#include "mosaic.h"
#include <stdlib.h>

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_enterRawMode(JNIEnv *env, jclass type) {
	rawModeResult result = enterRawMode();
	if (unlikely(result.error)) {
		jclass ise = (*env)->FindClass(env, "java/lang/IllegalStateException");
		char *message = malloc(50 * sizeof(char));
		if (message) {
			sprintf(message, "Unable to enable raw mode: %lu", result.error);
			// This throw can fail, but the only condition that should cause that is OOM which
			// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
			(*env)->ThrowNew(env, ise, message);
		}
		return 0;
	}
	return (jlong) result.saved;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_exitRawMode(JNIEnv *env, jclass type, jlong ptr) {
	return exitRawMode((rawModeConfig *) ptr);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderInit(JNIEnv *env, jclass type) {
	stdinReaderResult result = stdinReader_init();
	if (unlikely(result.error)) {
		jclass ise = (*env)->FindClass(env, "java/lang/IllegalStateException");
		char *message = malloc(54 * sizeof(char));
		if (message) {
			sprintf(message, "Unable to create stdin reader: %lu", result.error);
			// This throw can fail, but the only condition that should cause that is OOM which
			// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
			(*env)->ThrowNew(env, ise, message);
		}
		return 0;
	}
	return (jlong) result.reader;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderRead(
	JNIEnv *env,
	jclass type,
	jlong ptr,
	jbyteArray buffer,
	jint offset,
	jint length
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;
	stdinRead read = stdinReader_read((stdinReader *) ptr, nativeBufferAtOffset, length);
	(*env)->ReleaseByteArrayElements(env, buffer, nativeBuffer, 0);
	if (unlikely(read.error)) {
		jclass ise = (*env)->FindClass(env, "java/lang/IllegalStateException");
		char *message = malloc(44 * sizeof(char));
		if (message) {
			sprintf(message, "Unable to read stdin: %lu", read.error);
			// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
			// and should cause the program to try and exit cleanly. 0 is a valid return value.
			(*env)->ThrowNew(env, ise, message);
		}
		return -1;
	}
	return read.count;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderInterrupt(JNIEnv *env, jclass type, jlong ptr) {
	return stdinReader_interrupt((stdinReader *) ptr);
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_stdinReaderFree(JNIEnv *env, jclass type, jlong ptr) {
	return stdinReader_free((stdinReader *) ptr);
}
