#include <jni.h>
#include <mosaic.h>
#include <stdlib.h>
#include "cutils.h"

JNIEXPORT jlong JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_enterRawMode(JNIEnv *env, jclass type) {
	rawModeResult result = enterRawMode();
	if (unlikely(result.error)) {
		jclass ise = (*env)->FindClass(env, "java/lang/IllegalStateException");
		char *message = malloc(40 * sizeof(char));
		if (message) {
			sprintf(message, "Unable to enable raw mode: %i", result.error);
			// This throw can fail, but the only condition that should cause that is OOM which
			// will occur from returning 0 (which is otherwise ignored if the throw succeeds).
			(*env)->ThrowNew(env, ise, message);
		}
		return 0;
	}
	return (jlong) result.saved;
}

JNIEXPORT int JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_exitRawMode(JNIEnv *env, jclass type, jlong ptr) {
	return exitRawMode((rawModeConfig*)ptr);
}
