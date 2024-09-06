#ifndef MOSAIC_H
#define MOSAIC_H

#if defined(__APPLE__) || defined(__linux__)

#include <termios.h>

typedef struct termios rawModeConfig;
typedef unsigned int platformError;

#elif defined(WIN32)

#include <Windows.h>

typedef struct rawModeConfigWindows rawModeConfig;
typedef DWORD platformError;

#endif

typedef struct rawModeResult {
	rawModeConfig* saved;
	platformError error;
} rawModeResult;

rawModeResult enterRawMode();
platformError exitRawMode(rawModeConfig *saved);

#endif // MOSAIC_H
