#ifndef MOSAIC_TTY_JNI_H
#define MOSAIC_TTY_JNI_H

#include "mosaic-tty.h"

void throwIse(JNIEnv *env, unsigned int error, const char *prefix);

#endif // MOSAIC_TTY_JNI_H
