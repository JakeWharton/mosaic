#include "cutils.h"
#include "jni.h"
#include "mosaic-test-tty.h"
#include "mosaic-tty-jni.h"
#include <stdlib.h>
#include <string.h>

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Jni_platformInputWriterInit(
	JNIEnv *env,
	jclass type,
	jlong handlerOpaque
) {
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
