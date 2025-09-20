#include "com_jakewharton_mosaic_tty_Jni.h"

#include "jni.h"
#include "mosaic.h"
#include "mosaic-utils.h"

#include <stdlib.h>
#include <string.h>

static void throwIoe(JNIEnv *env, uint32_t error) {
	jclass ioe = (*env)->FindClass(env, "java/io/IOException");

	// 11 == max unsigned digit length (10) + null termination byte (1)
	char *message = malloc(11 * sizeof(char));
	if (message) {
		sprintf(message, "%u", error);
	}
	(*env)->ThrowNew(env, ioe, message);
}

static void throwOom(JNIEnv *env) {
	jclass oom = (*env)->FindClass(env, "java/lang/OutOfMemoryException");
	(*env)->ThrowNew(env, oom, NULL);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_tty_Jni_streamsInit(
	JNIEnv *env,
	jclass type UNUSED
) {
	MosaicStreamsInitResult result = mosaic_streams_init();
	if (likely(result.streams)) {
		return (jlong) result.streams;
	}

	if (result.error) {
		throwIoe(env, result.error);
	} else {
		throwOom(env);
	}
	return 0; // Unused.
}

JNIEXPORT jboolean JNICALL
Java_com_jakewharton_mosaic_tty_Jni_streamsInputIsTty(
	JNIEnv *env,
	jclass type UNUSED,
	jlong streamsOpaque
) {
	MosaicStreams *streams = (MosaicStreams *) streamsOpaque;
	MosaicStreamsTtyResult result = mosaic_streams_is_stdin_tty(streams);
	if (likely(result.error == 0)) {
		return result.is_tty;
	}
	throwIoe(env, result.error);
	return false; // Unused.
}

JNIEXPORT jboolean JNICALL
Java_com_jakewharton_mosaic_tty_Jni_streamsOutputIsTty(
	JNIEnv *env,
	jclass type UNUSED,
	jlong streamsOpaque
) {
	MosaicStreams *streams = (MosaicStreams *) streamsOpaque;
	MosaicStreamsTtyResult result = mosaic_streams_is_stdout_tty(streams);
	if (likely(result.error == 0)) {
		return result.is_tty;
	}
	throwIoe(env, result.error);
	return false; // Unused.
}

JNIEXPORT jboolean JNICALL
Java_com_jakewharton_mosaic_tty_Jni_streamsErrorIsTty(
	JNIEnv *env,
	jclass type UNUSED,
	jlong streamsOpaque
) {
	MosaicStreams *streams = (MosaicStreams *) streamsOpaque;
	MosaicStreamsTtyResult result = mosaic_streams_is_stderr_tty(streams);
	if (likely(result.error == 0)) {
		return result.is_tty;
	}
	throwIoe(env, result.error);
	return false; // Unused.
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_streamsFree(
	JNIEnv *env,
	jclass type UNUSED,
	jlong streamsOpaque
) {
	MosaicStreams *streams = (MosaicStreams *) streamsOpaque;
	uint32_t error = mosaic_streams_free(streams);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

typedef struct MosaicJniTtyCallback {
	JNIEnv *env;
	jobject instance;
	jmethodID onFocus;
	jmethodID onKey;
	jmethodID onMouse;
	jmethodID onResize;
} MosaicJniTtyCallback;

static void invokeOnFocusCallback(void *opaque, bool focused) {
	MosaicJniTtyCallback *callback = (MosaicJniTtyCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onFocus,
		focused
	);
}

static void invokeOnKeyCallback(void *opaque) {
	MosaicJniTtyCallback *callback = (MosaicJniTtyCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onKey
	);
}

static void invokeOnMouseCallback(void *opaque) {
	MosaicJniTtyCallback *callback = (MosaicJniTtyCallback *) opaque;
	(*callback->env)->CallVoidMethod(
		callback->env,
		callback->instance,
		callback->onMouse
	);
}

static void invokeOnResizeCallback(void *opaque, int columns, int rows, int width, int height) {
	MosaicJniTtyCallback *callback = (MosaicJniTtyCallback *) opaque;
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
Java_com_jakewharton_mosaic_tty_Jni_ttyCallbackInit(
	JNIEnv *env,
	jclass type UNUSED,
	jobject instance
) {
	jobject globalInstance = (*env)->NewGlobalRef(env, instance);
	if (unlikely(globalInstance == NULL)) {
		return 0;
	}
	jclass clazz = (*env)->FindClass(env, "com/jakewharton/mosaic/tty/Tty$Callback");
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

	MosaicJniTtyCallback *jniCallback = malloc(sizeof(MosaicJniTtyCallback));
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
Java_com_jakewharton_mosaic_tty_Jni_ttyCallbackFree(
	JNIEnv *env,
	jclass type UNUSED,
	jlong callbackOpaque
) {
	MosaicTtyCallback *callback = (MosaicTtyCallback *) callbackOpaque;
	MosaicJniTtyCallback *jniCallback = callback->opaque;
	jobject instance = jniCallback->instance;
	free(callback);
	free(jniCallback);
	(*env)->DeleteGlobalRef(env, instance);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyInit(
	JNIEnv *env,
	jclass type UNUSED
) {
	MosaicTtyInitResult result = tty_init();
	if (likely(result.tty)) {
		return (jlong) result.tty;
	}
	if (result.no_tty) {
		return 0;
	}

	if (result.already_bound) {
		jclass ise = (*env)->FindClass(env, "java/lang/IllegalStateException");
		(*env)->ThrowNew(env, ise, "Tty already bound");
	} else if (result.error) {
		throwIoe(env, result.error);
	} else {
		throwOom(env);
	}
	return 0; // Unused.
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttySetCallback(
	JNIEnv *env UNUSED,
	jclass type UNUSED,
	jlong ttyOpaque,
	jlong callbackOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	MosaicTtyCallback *callback = (MosaicTtyCallback *) callbackOpaque;
	tty_setCallback(tty, callback);
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyRead(
	JNIEnv *env,
	jclass type UNUSED,
	jlong ttyOpaque,
	jbyteArray buffer,
	jint offset,
	jint count
) {
	jbyte *bufferElements = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *bufferElementsAtOffset = bufferElements + offset;
	// Reinterpret JVM signed bytes as unsigned.
	uint8_t *nativeBufferAtOffset = (uint8_t *) bufferElementsAtOffset;

	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	MosaicIoResult result = tty_read(tty, nativeBufferAtOffset, count);

	(*env)->ReleaseByteArrayElements(env, buffer, bufferElements, 0);

	if (likely(!result.error)) {
		return result.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly. 0 is a valid return value.
	throwIoe(env, result.error);
	return -1;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyReadWithTimeout(
	JNIEnv *env,
	jclass type UNUSED,
	jlong ttyOpaque,
	jbyteArray buffer,
	jint offset,
	jint count,
	jint timeoutMillis
) {
	jbyte *bufferElements = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *bufferElementsAtOffset = bufferElements + offset;
	// Reinterpret JVM signed bytes as unsigned.
	uint8_t *nativeBufferAtOffset = (uint8_t *) bufferElementsAtOffset;

	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	MosaicIoResult result = tty_readWithTimeout(
		tty,
		nativeBufferAtOffset,
		count,
		timeoutMillis
	);

	(*env)->ReleaseByteArrayElements(env, buffer, bufferElements, 0);

	if (likely(!result.error)) {
		return result.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly. 0 is a valid return value.
	throwIoe(env, result.error);
	return -1;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyInterruptRead(
	JNIEnv *env,
	jclass type UNUSED,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_interruptRead(tty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyWrite(
	JNIEnv *env,
	jclass type UNUSED,
	jlong ttyOpaque,
	jbyteArray buffer,
	jint offset,
	jint count
) {
	jbyte *bufferElements = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *bufferElementsAtOffset = bufferElements + offset;
	// Reinterpret JVM signed bytes as unsigned.
	uint8_t *nativeBufferAtOffset = (uint8_t *) bufferElementsAtOffset;

	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	MosaicIoResult result = tty_write(tty, nativeBufferAtOffset, count);

	(*env)->ReleaseByteArrayElements(env, buffer, bufferElements, 0);

	if (likely(!result.error)) {
		return result.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly. 0 is a valid return value.
	throwIoe(env, result.error);
	return -1;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyEnableRawMode(
	JNIEnv *env,
	jclass type UNUSED,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_enableRawMode(tty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyEnableWindowResizeEvents(
	JNIEnv *env,
	jclass type UNUSED,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_enableWindowResizeEvents(tty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT jintArray JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyCurrentSize(
	JNIEnv *env,
	jclass type UNUSED,
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

	throwIoe(env, result.error);
	return NULL;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyReset(
	JNIEnv *env,
	jclass type UNUSED,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_reset(tty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_ttyFree(
	JNIEnv *env,
	jclass type UNUSED,
	jlong ttyOpaque
) {
	MosaicTty *tty = (MosaicTty *) ttyOpaque;
	uint32_t error = tty_free(tty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyInit(
	JNIEnv *env,
	jclass type UNUSED,
	jboolean stdinIsTty,
	jboolean stdoutIsTty,
	jboolean stderrIsTty
) {
	MosaicTestTtyInitResult result = testTty_init(stdinIsTty, stdoutIsTty, stderrIsTty);
	if (likely(result.testTty)) {
		return (jlong) result.testTty;
	}

	if (result.already_bound) {
		jclass ise = (*env)->FindClass(env, "java/lang/IllegalStateException");
		(*env)->ThrowNew(env, ise, "TestTty or Tty already bound");
	} else if (result.error) {
		throwIoe(env, result.error);
	} else {
		throwOom(env);
	}
	return 0; // Unused
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyWrite(
	JNIEnv *env,
	jclass type UNUSED,
	jlong testTtyOpaque,
	jbyteArray buffer,
	jint offset,
	jint count
) {
	jbyte *bufferElements = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *bufferElementsAtOffset = bufferElements + offset;
	// Reinterpret JVM signed bytes as unsigned.
	uint8_t *nativeBufferAtOffset = (uint8_t *) bufferElementsAtOffset;

	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	MosaicIoResult result = testTty_write(testTty, nativeBufferAtOffset, count);

	(*env)->ReleaseByteArrayElements(env, buffer, bufferElements, 0);

	if (likely(!result.error)) {
		return result.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly.
	throwIoe(env, result.error);
	return -1;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyRead(
	JNIEnv *env,
	jclass type UNUSED,
	jlong testTtyOpaque,
	jbyteArray buffer,
	jint offset,
	jint count
) {
	jbyte *bufferElements = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *bufferElementsAtOffset = bufferElements + offset;
	// Reinterpret JVM signed bytes as unsigned.
	uint8_t *nativeBufferAtOffset = (uint8_t *) bufferElementsAtOffset;

	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	MosaicIoResult result = testTty_read(testTty, nativeBufferAtOffset, count);

	(*env)->ReleaseByteArrayElements(env, buffer, bufferElements, 0);

	if (likely(!result.error)) {
		return result.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly.
	throwIoe(env, result.error);
	return -1;
}

JNIEXPORT jint JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyReadWithTimeout(
	JNIEnv *env,
	jclass type UNUSED,
	jlong testTtyOpaque,
	jbyteArray buffer,
	jint offset,
	jint count,
	jint timeoutMillis
) {
	jbyte *bufferElements = (*env)->GetByteArrayElements(env, buffer, NULL);
	jbyte *bufferElementsAtOffset = bufferElements + offset;
	// Reinterpret JVM signed bytes as unsigned.
	uint8_t *nativeBufferAtOffset = (uint8_t *) bufferElementsAtOffset;

	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	MosaicIoResult result = testTty_readWithTimeout(testTty, nativeBufferAtOffset, count, timeoutMillis);

	(*env)->ReleaseByteArrayElements(env, buffer, bufferElements, 0);

	if (likely(!result.error)) {
		return result.count;
	}

	// This throw can fail, but the only condition that should cause that is OOM. Return -1 (EOF)
	// and should cause the program to try and exit cleanly.
	throwIoe(env, result.error);
	return -1;
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyInterruptRead(
	JNIEnv *env,
	jclass type UNUSED,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	uint32_t error = testTty_interruptRead(testTty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtySendFocusEvent(
	JNIEnv *env,
	jclass type UNUSED,
	jlong testTtyOpaque,
	jboolean focused
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	uint32_t error = testTty_sendFocusEvent(testTty, focused);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtySendKeyEvent(
	JNIEnv *env,
	jclass type UNUSED,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	uint32_t error = testTty_sendKeyEvent(testTty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtySendMouseEvent(
	JNIEnv *env,
	jclass type UNUSED,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	uint32_t error = testTty_sendMouseEvent(testTty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyResize(
	JNIEnv *env,
	jclass type UNUSED,
	jlong testTtyOpaque,
	jint columns,
	jint rows,
	jint width,
	jint height
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	uint32_t error = testTty_resize(testTty, columns, rows, width, height);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyGetTty(
	JNIEnv *env UNUSED,
	jclass type UNUSED,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	return (jlong) testTty_getTty(testTty);
}

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyGetStreams(
	JNIEnv *env UNUSED,
	jclass type UNUSED,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	return (jlong) testTty_getStreams(testTty);
}

JNIEXPORT void JNICALL
Java_com_jakewharton_mosaic_tty_Jni_testTtyFree(
	JNIEnv *env UNUSED,
	jclass type UNUSED,
	jlong testTtyOpaque
) {
	MosaicTestTty *testTty = (MosaicTestTty *) testTtyOpaque;
	uint32_t error = testTty_free(testTty);
	if (unlikely(error)) {
		throwIoe(env, error);
	}
}
