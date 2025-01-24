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

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_enterRawMode(JNIEnv *env, jclass type) {
	rawModeResult result = enterRawMode();
	if (likely(!result.error)) {
		return (jlong) result.saved;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to enable raw mode");
	return 0;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_exitRawMode(JNIEnv *env, jclass type, jlong ptr) {
	platformError error = exitRawMode((rawModeConfig *) ptr);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to exit raw mode");
	}
}

typedef struct jniPlatformEventHandler {
	JNIEnv *env;
	jobject instance;
	jclass clazz;
	jmethodID onFocus;
	jmethodID onKey;
	jmethodID onMouse;
	jmethodID onResize;
} jniPlatformEventHandler;

void invokeOnFocusHandler(void *opaque, bool focused) {
	jniPlatformEventHandler *handler = (jniPlatformEventHandler *) opaque;
	(*handler->env)->CallNonvirtualVoidMethod(
		handler->env,
		handler->instance,
		handler->clazz,
		handler->onFocus,
		focused
	);
}

void invokeOnKeyHandler(void *opaque) {
	jniPlatformEventHandler *handler = (jniPlatformEventHandler *) opaque;
	(*handler->env)->CallNonvirtualVoidMethod(
		handler->env,
		handler->instance,
		handler->clazz,
		handler->onKey
	);
}

void invokeOnMouseHandler(void *opaque) {
	jniPlatformEventHandler *handler = (jniPlatformEventHandler *) opaque;
	(*handler->env)->CallNonvirtualVoidMethod(
		handler->env,
		handler->instance,
		handler->clazz,
		handler->onMouse
	);
}

void invokeOnResizeHandler(void *opaque, int columns, int rows, int width, int height) {
	jniPlatformEventHandler *handler = (jniPlatformEventHandler *) opaque;
	(*handler->env)->CallNonvirtualVoidMethod(
		handler->env,
		handler->instance,
		handler->clazz,
		handler->onResize,
		columns,
		rows,
		width,
		height
	);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformEventHandlerInit(
	JNIEnv *env,
	jclass type,
	jobject instance
) {
	jobject globalInstance = (*env)->NewGlobalRef(env, instance);
	if (unlikely(globalInstance == NULL)) {
		return 0;
	}
	jclass clazz = (*env)->FindClass(env, "com/jakewharton/mosaic/terminal/PlatformEventHandler");
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

	jniPlatformEventHandler *jniHandler = malloc(sizeof(jniPlatformEventHandler));
	if (unlikely(!jniHandler)) {
		return 0;
	}
	jniHandler->env = env;
	jniHandler->instance = globalInstance;
	jniHandler->clazz = clazz;
	jniHandler->onFocus = onFocus;
	jniHandler->onKey = onKey;
	jniHandler->onMouse = onMouse;
	jniHandler->onResize = onResize;

	platformEventHandler *handler = malloc(sizeof(platformEventHandler));
	if (unlikely(!handler)) {
		return 0;
	}
	handler->opaque = jniHandler;
	handler->onFocus = invokeOnFocusHandler;
	handler->onKey = invokeOnKeyHandler;
	handler->onMouse = invokeOnMouseHandler;
	handler->onResize = invokeOnResizeHandler;

	return (jlong) handler;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformEventHandlerFree(
	JNIEnv *env,
	jclass type,
	jlong handlerOpaque
) {
	platformEventHandler *handler = (platformEventHandler *) handlerOpaque;
	jniPlatformEventHandler *jniHandler = handler->opaque;
	jobject instance = jniHandler->instance;
	free(handler);
	free(jniHandler);
	(*env)->DeleteGlobalRef(env, instance);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputInit(JNIEnv *env, jclass type, jlong handlerOpaque) {
	platformEventHandler *handler = (platformEventHandler *) handlerOpaque;
	platformInputResult result = platformInput_init(handler);
	if (likely(!result.error)) {
		return (jlong) result.reader;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to create stdin reader");
	return 0;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputRead(
	JNIEnv *env,
	jclass type,
	jlong readerOpaque,
	jbyteArray buffer,
	jint offset,
	jint count
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	platformInput *reader = (platformInput *) readerOpaque;
	stdinRead read = platformInput_read(reader, nativeBufferAtOffset, count);

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
	jlong readerOpaque,
	jbyteArray buffer,
	jint offset,
	jint count,
	jint timeoutMillis
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	platformInput *reader = (platformInput *) readerOpaque;
	stdinRead read = platformInput_readWithTimeout(
		reader,
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
Java_com_jakewharton_mosaic_terminal_Jni_platformInputInterrupt(JNIEnv *env, jclass type, jlong readerOpaque) {
	platformInput *reader = (platformInput *) readerOpaque;
	platformError error = platformInput_interrupt(reader);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to interrupt stdin reader");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputFree(JNIEnv *env, jclass type, jlong readerOpaque) {
	platformInput *reader = (platformInput *) readerOpaque;
	platformError error = platformInput_free(reader);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to free stdin reader");
	}
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterInit(JNIEnv *env, jclass type, jlong handlerOpaque) {
	platformEventHandler *handler = (platformEventHandler *) handlerOpaque;
	platformInputWriterResult result = platformInputWriter_init(handler);
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
	platformError error = platformInputWriter_write(writer, nativeBuffer, count);

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
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterGetReader(JNIEnv *env, jclass type, jlong ptr) {
	return (jlong) platformInputWriter_getReader((platformInputWriter *) ptr);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterFree(JNIEnv *env, jclass type, jlong ptr) {
	platformError error = platformInputWriter_free((platformInputWriter *) ptr);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to free stdin writer");
	}
}
