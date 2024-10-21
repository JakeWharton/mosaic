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


typedef struct stdinReaderImpl stdinReader;

typedef struct stdinReaderResult {
	stdinReader* reader;
	platformError error;
} stdinReaderResult;

typedef struct stdinRead {
	int count;
	platformError error;
} stdinRead;

stdinReaderResult stdinReader_init(const char *path);
stdinRead stdinReader_read(stdinReader *reader, void *buffer, int count);
stdinRead stdinReader_readWithTimeout(stdinReader *reader, void *buffer, int count, int timeoutMillis);
platformError stdinReader_interrupt(stdinReader* reader);
platformError stdinReader_free(stdinReader *reader);

#endif // MOSAIC_H
