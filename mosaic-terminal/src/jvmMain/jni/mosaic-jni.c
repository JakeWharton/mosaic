#include <jni.h>
#include <mosaic.h>

JNIEXPORT jboolean JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_save(JNIEnv* env, jclass type) {
	return tty_save();
}

JNIEXPORT jboolean JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_restore(JNIEnv* env, jclass type) {
	return tty_restore();
}

JNIEXPORT jboolean JNICALL
Java_com_jakewharton_mosaic_terminal_Tty_setRawMode(JNIEnv* env, jclass type) {
	return tty_setRawMode();
}
