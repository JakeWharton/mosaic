#ifndef MOSAIC_H
#define MOSAIC_H

#if defined(__APPLE__) || defined(__linux__)

#include <termios.h>
#include <time.h>

typedef struct termios rawModeConfig;
typedef unsigned int platformError;

typedef struct timespec *stdinReaderTimeout;

#elif defined(WIN32)

#include <Windows.h>

typedef struct rawModeConfigWindows rawModeConfig;
typedef DWORD platformError;

typedef DWORD stdinReaderTimeout;

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

stdinReaderResult stdinReader_init();
stdinRead stdinReader_read(stdinReader *reader, void *buffer, int count, stdinReaderTimeout timeout);
platformError stdinReader_interrupt(stdinReader* reader);
platformError stdinReader_free(stdinReader *reader);
stdinReaderTimeout stdinReader_noTimeout();
stdinReaderTimeout stdinReader_createTimeout(stdinReader *reader, int timeoutMillis);

#endif // MOSAIC_H
