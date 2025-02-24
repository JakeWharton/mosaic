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

typedef struct jniTtyCallback {
	JNIEnv *env;
	jobject instance;
	jmethodID onFocus;
	jmethodID onKey;
	jmethodID onMouse;
	jmethodID onResize;
} jniTtyCallback;

void invokeOnFocusCallback(void *opaque, bool focused) {
	jniTtyCallback *callback = (jniTtyCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onFocus,
		focused
	);
}

void invokeOnKeyCallback(void *opaque) {
	jniTtyCallback *callback = (jniTtyCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onKey
	);
}

void invokeOnMouseCallback(void *opaque) {
	jniTtyCallback *callback = (jniTtyCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onMouse
	);
}

void invokeOnResizeCallback(void *opaque, int columns, int rows, int width, int height) {
	jniTtyCallback *callback = (jniTtyCallback *) opaque;
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
Java_com_jakewharton_mosaic_terminal_Jni_ttyCallbackInit(
	JNIEnv *env,
	jclass type,
	jobject instance
) {
	jobject globalInstance = (*env)->NewGlobalRef(env, instance);
	if (unlikely(globalInstance == NULL)) {
		return 0;
	}
	jclass clazz = (*env)->FindClass(env, "com/jakewharton/mosaic/terminal/Tty$Callback");
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

	jniTtyCallback *jniCallback = malloc(sizeof(jniTtyCallback));
	if (unlikely(!jniCallback)) {
		return 0;
	}
	jniCallback->env = env;
	jniCallback->instance = globalInstance;
	jniCallback->onFocus = onFocus;
	jniCallback->onKey = onKey;
	jniCallback->onMouse = onMouse;
	jniCallback->onResize = onResize;

	MosaicTtyCallback *callback = malloc(sizeof(MosaicTtyCallback));
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
Java_com_jakewharton_mosaic_terminal_Jni_ttyCallbackFree(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	MosaicTtyCallback *callback = (MosaicTtyCallback *) callbackOpaque;
	jniTtyCallback *jniCallback = callback->opaque;
	jobject instance = jniCallback->instance;
	free(callback);
	free(jniCallback);
	(*env)->DeleteGlobalRef(env, instance);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_ttyInit(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	MosaicTtyCallback *callback = (MosaicTtyCallback *) callbackOpaque;
	MosaicTtyInitResult result = tty_init(callback);
	if (likely(!result.error)) {
		return (jlong) result.tty;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to create");
	return 0;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_ttyRead(
	JNIEnv *env,
	jclass type,
	jlong ttyOpaque,
	jbyteArray buffer,
	jint offset,
	jint count
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	MosaicTtyIoResult read = tty_read(tty, nativeBufferAtOffset, count);

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
Java_com_jakewharton_mosaic_terminal_Jni_ttyReadWithTimeout(
	JNIEnv *env,
	jclass type,
	jlong ttyOpaque,
	jbyteArray buffer,
	jint offset,
	jint count,
	jint timeoutMillis
) {
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *nativeBufferAtOffset = nativeBuffer + offset;

	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	MosaicTtyIoResult read = tty_readWithTimeout(
		tty,
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
Java_com_jakewharton_mosaic_terminal_Jni_ttyInterrupt(
	JNIEnv *env,
	jclass type,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_interrupt(tty);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to interrupt");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_ttyEnableRawMode(
	JNIEnv *env,
	jclass type,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_enableRawMode(tty);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to enable raw mode");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_ttyEnableWindowResizeEvents(
	JNIEnv *env,
	jclass type,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_enableWindowResizeEvents(tty);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to enable window resize events");
	}
}

JNIEXPORT jintArray JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_ttyCurrentSize(
	JNIEnv *env,
	jclass type,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	MosaicTtyTerminalSizeResult result = tty_currentTerminalSize(tty);
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
Java_com_jakewharton_mosaic_terminal_Jni_ttyFree(
	JNIEnv *env,
	jclass type,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_free(tty);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to free");
	}
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTtyInit(
	JNIEnv *env,
	jclass type,
	jlong callbackOpaque
) {
	MosaicTtyCallback *callback = (MosaicTtyCallback *) callbackOpaque;
	MosaicTestTtyInitResult result = testTty_init(callback);
	if (likely(!result.error)) {
		return (jlong) result.testTty;
	}

	// This throw can fail, but the only condition that should cause that is OOM which
	// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
	throwIse(env, result.error, "Unable to initialize");
	return 0;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTtyWrite(
	JNIEnv *env,
	jclass type,
	jlong testTtyOpaque,
	jbyteArray buffer
) {
	jsize count = (*env)->GetArrayLength(env, buffer);
	jbyte *nativeBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);

	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	uint32_t error = testTty_write(testTty, nativeBuffer, count);

	(*env)->ReleaseByteArrayElements(env, buffer, nativeBuffer, 0);

	if (unlikely(error)) {
		// This throw can fail, but the only condition that should cause that is OOM. Oh well.
		throwIse(env, error, "Unable to write stdin");
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTtyFocusEvent(
	JNIEnv *env,
	jclass type,
	jlong testTtyOpaque,
	bool focused
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	testTty_focusEvent(testTty, focused);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTtyKeyEvent(
	JNIEnv *env,
	jclass type,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	testTty_keyEvent(testTty);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTtyMouseEvent(
	JNIEnv *env,
	jclass type,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	testTty_mouseEvent(testTty);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTtyResizeEvent(
	JNIEnv *env,
	jclass type,
	jlong testTtyOpaque,
	jint columns,
	jint rows,
	jint width,
	jint height
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	testTty_resizeEvent(testTty, columns, rows, width, height);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTtyGetTty(
	JNIEnv *env,
	jclass type,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	return (jlong) testTty_getTty(testTty);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_testTtyFree(
	JNIEnv *env,
	jclass type,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	uint32_t error = testTty_free(testTty);
	if (unlikely(error)) {
		throwIse(env, error, "Unable to free");
	}
}
