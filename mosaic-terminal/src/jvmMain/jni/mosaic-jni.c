#include "cutils.h"
#include "jni.h"
#include "mosaic.h"
#include <stdlib.h>
#include <string.h>

void throwIse(JNIEnv *env, uint32_t error, const char *prefix) {
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
		sprintf(message + prefixLength + colonSpaceLength, "%u", error);
		(*env)->ThrowNew(env, ise, message);
	}
}

typedef struct JniEventCallback {
	JNIEnv *env;
	jobject instance;
	jmethodID onFocus;
	jmethodID onKey;
	jmethodID onMouse;
	jmethodID onResize;
} JniEventCallback;

void invokeOnFocusHandler(void *opaque, bool focused) {
	JniEventCallback *handler = (JniEventCallback *) opaque;
	(*handler->env)->CallVoidMethod(
		handler->env,
		handler->instance,
		handler->onFocus,
		focused
	);
}

void invokeOnKeyHandler(void *opaque) {
	JniEventCallback *handler = (JniEventCallback *) opaque;
	(*handler->env)->CallVoidMethod(
		handler->env,
		handler->instance,
		handler->onKey
	);
}

void invokeOnMouseHandler(void *opaque) {
	JniEventCallback *handler = (JniEventCallback *) opaque;
	(*handler->env)->CallVoidMethod(
		handler->env,
		handler->instance,
		handler->onMouse
	);
}

void invokeOnResizeHandler(void *opaque, uint16_t columns, uint16_t rows, uint16_t width, uint16_t height) {
	JniEventCallback *handler = (JniEventCallback *) opaque;
	(*handler->env)->CallVoidMethod(
		handler->env,
		handler->instance,
		handler->onResize,
		(jint) columns,
		(jint) rows,
		(jint) width,
		(jint) height
	);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_terminalEventCallbackInit(
	JNIEnv *env,
	jclass type,
	jobject instance
) {
	jobject globalInstance = (*env)->NewGlobalRef(env, instance);
	if (unlikely(globalInstance == NULL)) {
		return 0;
	}
	jclass clazz = (*env)->FindClass(env, "com/jakewharton/mosaic/terminal/RawTerminal$EventCallback");
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

	JniEventCallback *jniCallback = malloc(sizeof(JniEventCallback));
	if (unlikely(!jniCallback)) {
		return 0;
	}
	jniCallback->env = env;
	jniCallback->instance = globalInstance;
	jniCallback->onFocus = onFocus;
	jniCallback->onKey = onKey;
	jniCallback->onMouse = onMouse;
	jniCallback->onResize = onResize;

	MosaicTerminalEventCallback *callback = malloc(sizeof(MosaicTerminalEventCallback));
	if (unlikely(!callback)) {
		return 0;
	}
	callback->opaque = jniCallback;
	callback->onFocus = invokeOnFocusHandler;
	callback->onKey = invokeOnKeyHandler;
	callback->onMouse = invokeOnMouseHandler;
	callback->onResize = invokeOnResizeHandler;

	return (jlong) callback;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_terminalEventCallbackFree(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	MosaicTerminalEventCallback *callback = (MosaicTerminalEventCallback *) callbackOpaque;
	JniEventCallback *jniCallback = callback->opaque;
	jobject instance = jniCallback->instance;
	free(callback);
	free(jniCallback);
	(*env)->DeleteGlobalRef(env, instance);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_terminalInit(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	MosaicTerminalEventCallback *callback = (MosaicTerminalEventCallback *) callbackOpaque;
	MosaicTerminalInitResult result = MosaicTerminalInit(callback);
	if (likely(!result.error)) {
		return (jlong) result.terminal;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to create");
	return 0;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_terminalRead(
	JNIEnv *env,
	jclass type,
	jlong terminalOpaque,
	jbyteArray buffer,
	jint offset,
	jint count
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	MosaicTerminal *terminal = (MosaicTerminal *) terminalOpaque;
	MosaicTerminalResult read = MosaicTerminalRead(terminal, nativeBufferAtOffset, count);

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
Java_com_jakewharton_mosaic_terminal_Jni_terminalReadWithTimeout(
	JNIEnv *env,
	jclass type,
	jlong terminalOpaque,
	jbyteArray buffer,
	jint offset,
	jint count,
	jint timeoutMillis
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	MosaicTerminal *terminal = (MosaicTerminal *) terminalOpaque;
	MosaicTerminalResult read = MosaicTerminalReadWithTimeout(
		terminal,
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
Java_com_jakewharton_mosaic_terminal_Jni_terminalInterruptRead(
	JNIEnv *env,
	jclass type,
	jlong terminalOpaque
) {
	MosaicTerminal *terminal = (MosaicTerminal *) terminalOpaque;
	uint32_t error = MosaicTerminalInterrupt(terminal);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to interrupt");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_terminalEnableRawMode(
	JNIEnv *env,
	jclass type,
	jlong terminalOpaque
) {
	MosaicTerminal *terminal = (MosaicTerminal *) terminalOpaque;
	uint32_t result = MosaicTerminalEnableRawMode(terminal);
	if (unlikely(result)) {
		throwIse(env, result, "Unable to enable raw mode");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_terminalEnableWindowResizeEvents(
	JNIEnv *env,
	jclass type,
	jlong terminalOpaque
) {
	MosaicTerminal *terminal = (MosaicTerminal *) terminalOpaque;
	uint32_t error = MosaicTerminalEnableResizeEvents(terminal);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to enable window resize events");
	}
}

JNIEXPORT jintArray JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_terminalCurrentSize(
	JNIEnv *env,
	jclass type,
	jlong terminalOpaque
) {
	MosaicTerminal *terminal = (MosaicTerminal *) terminalOpaque;
	MosaicTerminalSizeResult result = MosaicTerminalCurrentSize(terminal);
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
Java_com_jakewharton_mosaic_terminal_Jni_terminalFree(
	JNIEnv *env,
	jclass type,
	jlong terminalOpaque
) {
	MosaicTerminal *terminal = (MosaicTerminal *) terminalOpaque;
	uint32_t error = MosaicTerminalFree(terminal);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to free terminal");
	}
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTerminalInit(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	MosaicTerminalEventCallback *callback = (MosaicTerminalEventCallback *) callbackOpaque;
	MosaicTestTerminalInitResult result = MosaicTestTerminalInit(callback);
	if (likely(!result.error)) {
		return (jlong) result.testTerminal;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to create test terminal");
	return 0;
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTerminalGetTerminal(
	JNIEnv *env,
	jclass type,
	jlong testTerminalOpaque
) {
	MosaicTestTerminal *testTerminal = (MosaicTestTerminal *) testTerminalOpaque;
	return (jlong) MosaicTestTerminalGetTerminal(testTerminal);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTerminalWrite(
	JNIEnv *env,
	jclass type,
	jlong testTerminalOpaque,
	jbyteArray buffer
) {
	jsize count = (*env)->GetArrayLength(env, buffer);
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);

	MosaicTestTerminal *testTerminal = (MosaicTestTerminal *) testTerminalOpaque;
	uint32_t error = MosaicTestTerminalWrite(testTerminal, nativeBuffer, count);

	(*env)->ReleaseByteArrayElements(env, buffer, nativeBuffer, 0);

	if (unlikely(error)) {
		// This throw can fail, but the only condition that should cause that is OOM. Oh well.
		throwIse(env, error, "Unable to write stdin");
	}
}

//JNIEXPORT void JNICALL
//Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterFocusEvent(
//	JNIEnv *env,
//	jclass type,
//	jlong testTerminalOpaque,
//	bool focused
//) {
//	MosaicTestTerminal *testTerminal = (MosaicTestTerminal *) testTerminalOpaque;
//	platformInputWriter_focusEvent(testTerminal, focused);
//}
//
//JNIEXPORT void JNICALL
//Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterKeyEvent(
//	JNIEnv *env,
//	jclass type,
//	jlong testTerminalOpaque
//) {
//	MosaicTestTerminal *testTerminal = (MosaicTestTerminal *) testTerminalOpaque;
//	platformInputWriter_keyEvent(testTerminal);
//}
//
//JNIEXPORT void JNICALL
//Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterMouseEvent(
//	JNIEnv *env,
//	jclass type,
//	jlong testTerminalOpaque
//) {
//	MosaicTestTerminal *testTerminal = (MosaicTestTerminal *) testTerminalOpaque;
//	platformInputWriter_mouseEvent(testTerminal);
//}
//
//JNIEXPORT void JNICALL
//Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterResizeEvent(
//	JNIEnv *env,
//	jclass type,
//	jlong testTerminalOpaque,
//	jint columns,
//	jint rows,
//	jint width,
//	jint height
//) {
//	MosaicTestTerminal *testTerminal = (MosaicTestTerminal *) testTerminalOpaque;
//	platformInputWriter_resizeEvent(testTerminal, columns, rows, width, height);
//}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTerminalFree(
	JNIEnv *env,
	jclass type,
	jlong testTerminalOpaque
) {
	MosaicTestTerminal *testTerminal = (MosaicTestTerminal *) testTerminalOpaque;
	uint32_t error = MosaicTestTerminalFree(testTerminal);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to free test terminal");
	}
}
