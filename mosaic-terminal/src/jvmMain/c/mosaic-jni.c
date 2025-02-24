#include "cutils.h"
#include "jni.h"
#include "mosaic.h"
#include <stdlib.h>
#include <string.h>

void throwIse(JNIEnv *env, unsigned int error, const char *prefix) {
	jclass ise = (*env)->FindClass(env, "java/lang/IllegalStateException");

	int prefixLength = strlen(prefix);
	int colonSpaceLength = 2;
	int maxLengthUnsignedDigit = 10;
	int extraNullByte = 1;
	int messageLength = prefixLength + colonSpaceLength + maxLengthUnsignedDigit + extraNullByte;

	char *message = malloc(messageLength * sizeof(char));
	if (message) {
		memcpy(message, prefix, prefixLength);
		message[prefixLength] = ':';
		message[prefixLength + 1] = ' ';
		// Offset the location of the formatted number by the prefix and colon+space lengths.
		sprintf(message + prefixLength + colonSpaceLength, "%lu", error);
		(*env)->ThrowNew(env, ise, message);
	}
}

typedef struct jniPlatformInputCallback {
	JNIEnv *env;
	jobject instance;
	jmethodID onFocus;
	jmethodID onKey;
	jmethodID onMouse;
	jmethodID onResize;
} jniPlatformInputCallback;

void invokeOnFocusCallback(void *opaque, bool focused) {
	jniPlatformInputCallback *callback = (jniPlatformInputCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onFocus,
		focused
	);
}

void invokeOnKeyCallback(void *opaque) {
	jniPlatformInputCallback *callback = (jniPlatformInputCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onKey
	);
}

void invokeOnMouseCallback(void *opaque) {
	jniPlatformInputCallback *callback = (jniPlatformInputCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onMouse
	);
}

void invokeOnResizeCallback(void *opaque, int columns, int rows, int width, int height) {
	jniPlatformInputCallback *callback = (jniPlatformInputCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onResize,
		columns,
		rows,
		width,
		height
	);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputCallbackInit(
	JNIEnv *env,
	jclass type,
	jobject instance
) {
	jobject globalInstance = (*env)->NewGlobalRef(env, instance);
	if (unlikely(globalInstance == NULL)) {
		return 0;
	}
	jclass clazz = (*env)->FindClass(env, "com/jakewharton/mosaic/terminal/PlatformInput$Callback");
	if (unlikely(clazz == NULL)) {
		return 0;
	}
	jmethodID onFocus = (*env)->GetMethodID(env, clazz, "onFocus", "(Z)V");
	if (unlikely(onFocus == NULL)) {
		return 0;
	}
	jmethodID onKey = (*env)->GetMethodID(env, clazz, "onKey", "()V");
	if (unlikely(onKey == NULL)) {
		return 0;
	}
	jmethodID onMouse = (*env)->GetMethodID(env, clazz, "onMouse", "()V");
	if (unlikely(onMouse == NULL)) {
		return 0;
	}
	jmethodID onResize = (*env)->GetMethodID(env, clazz, "onResize", "(IIII)V");
	if (unlikely(onResize == NULL)) {
		return 0;
	}

	jniPlatformInputCallback *jniCallback = malloc(sizeof(jniPlatformInputCallback));
	if (unlikely(!jniCallback)) {
		return 0;
	}
	jniCallback->env = env;
	jniCallback->instance = globalInstance;
	jniCallback->onFocus = onFocus;
	jniCallback->onKey = onKey;
	jniCallback->onMouse = onMouse;
	jniCallback->onResize = onResize;

	platformInputCallback *callback = malloc(sizeof(platformInputCallback));
	if (unlikely(!callback)) {
		return 0;
	}
	callback->opaque = jniCallback;
	callback->onFocus = invokeOnFocusCallback;
	callback->onKey = invokeOnKeyCallback;
	callback->onMouse = invokeOnMouseCallback;
	callback->onResize = invokeOnResizeCallback;

	return (jlong) callback;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputCallbackFree(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	platformInputCallback *callback = (platformInputCallback *) callbackOpaque;
	jniPlatformInputCallback *jniCallback = callback->opaque;
	jobject instance = jniCallback->instance;
	free(callback);
	free(jniCallback);
	(*env)->DeleteGlobalRef(env, instance);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputInit(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	platformInputCallback *callback = (platformInputCallback *) callbackOpaque;
	platformInputResult result = platformInput_init(callback);
	if (likely(!result.error)) {
		return (jlong) result.input;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to create");
	return 0;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputRead(
	JNIEnv *env,
	jclass type,
	jlong inputOpaque,
	jbyteArray buffer,
	jint offset,
	jint count
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	platformInput *input = (platformInput *) inputOpaque;
	stdinRead read = platformInput_read(input, nativeBufferAtOffset, count);

	(*env)->ReleaseByteArrayElements(env, buffer, nativeBuffer, 0);

	if (likely(!read.error)) {
		return read.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly. 0 is a valid return value.
	throwIse(env, read.error, "Unable to read stdin");
	return -1;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputReadWithTimeout(
	JNIEnv *env,
	jclass type,
	jlong inputOpaque,
	jbyteArray buffer,
	jint offset,
	jint count,
	jint timeoutMillis
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	platformInput *input = (platformInput *) inputOpaque;
	stdinRead read = platformInput_readWithTimeout(
		input,
		nativeBufferAtOffset,
		count,
		timeoutMillis
	);

	(*env)->ReleaseByteArrayElements(env, buffer, nativeBuffer, 0);

	if (likely(!read.error)) {
		return read.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly. 0 is a valid return value.
	throwIse(env, read.error, "Unable to read stdin");
	return -1;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputInterrupt(
	JNIEnv *env,
	jclass type,
	jlong inputOpaque
) {
	platformInput *input = (platformInput *) inputOpaque;
	uint32_t error = platformInput_interrupt(input);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to interrupt");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputEnableRawMode(
	JNIEnv *env,
	jclass type,
	jlong inputOpaque
) {
	platformInput *input = (platformInput *) inputOpaque;
	uint32_t error = platformInput_enableRawMode(input);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to enable raw mode");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputEnableWindowResizeEvents(
	JNIEnv *env,
	jclass type,
	jlong inputOpaque
) {
	platformInput *input = (platformInput *) inputOpaque;
	uint32_t error = platformInput_enableWindowResizeEvents(input);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to enable window resize events");
	}
}

JNIEXPORT jintArray JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputCurrentSize(
	JNIEnv *env,
	jclass type,
	jlong inputOpaque
) {
	platformInput *input = (platformInput *) inputOpaque;
	terminalSizeResult result = platformInput_currentTerminalSize(input);
	if (likely(!result.error)) {
		jintArray ints = (*env)->NewIntArray(env, 4);
		jint *intsPtr = (*env)->GetIntArrayElements(env, ints, NULL);
		intsPtr[0] = result.columns;
		intsPtr[1] = result.rows;
		intsPtr[2] = result.width;
		intsPtr[3] = result.height;
		(*env)->ReleaseIntArrayElements(env, ints, intsPtr, 0);
		return ints;
	}

	throwIse(env, result.error, "Unable to get terminal size");
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputFree(
	JNIEnv *env,
	jclass type,
	jlong inputOpaque
) {
	platformInput *input = (platformInput *) inputOpaque;
	uint32_t error = platformInput_free(input);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to free");
	}
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterInit(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	platformInputCallback *callback = (platformInputCallback *) callbackOpaque;
	platformInputWriterResult result = platformInputWriter_init(callback);
	if (likely(!result.error)) {
		return (jlong) result.writer;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to create stdin writer");
	return 0;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterWrite(
	JNIEnv *env,
	jclass type,
	jlong writerOpaque,
	jbyteArray buffer
) {
	jsize count = (*env)->GetArrayLength(env, buffer);
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);

	platformInputWriter *writer = (platformInputWriter *) writerOpaque;
	uint32_t error = platformInputWriter_write(writer, nativeBuffer, count);

	(*env)->ReleaseByteArrayElements(env, buffer, nativeBuffer, 0);

	if (unlikely(error)) {
		// This throw can fail, but the only condition that should cause that is OOM. Oh well.
		throwIse(env, error, "Unable to write stdin");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterFocusEvent(
	JNIEnv *env,
	jclass type,
	jlong writerOpaque,
	bool focused
) {
	platformInputWriter *writer = (platformInputWriter *) writerOpaque;
	platformInputWriter_focusEvent(writer, focused);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterKeyEvent(
	JNIEnv *env,
	jclass type,
	jlong writerOpaque
) {
	platformInputWriter *writer = (platformInputWriter *) writerOpaque;
	platformInputWriter_keyEvent(writer);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterMouseEvent(
	JNIEnv *env,
	jclass type,
	jlong writerOpaque
) {
	platformInputWriter *writer = (platformInputWriter *) writerOpaque;
	platformInputWriter_mouseEvent(writer);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterResizeEvent(
	JNIEnv *env,
	jclass type,
	jlong writerOpaque,
	jint columns,
	jint rows,
	jint width,
	jint height
) {
	platformInputWriter *writer = (platformInputWriter *) writerOpaque;
	platformInputWriter_resizeEvent(writer, columns, rows, width, height);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterGetPlatformInput(
	JNIEnv *env,
	jclass type,
	jlong writerOpaque
) {
	platformInputWriter *writer = (platformInputWriter *) writerOpaque;
	return (jlong) platformInputWriter_getPlatformInput(writer);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterFree(
	JNIEnv *env,
	jclass type,
	jlong writerOpaque
) {
	platformInputWriter *writer = (platformInputWriter *) writerOpaque;
	uint32_t error = platformInputWriter_free(writer);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to free stdin writer");
	}
}
