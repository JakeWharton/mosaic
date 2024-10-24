#ifndef MOSAIC_H
#define MOSAIC_H

#if defined(__APPLE__) || defined(__linux__)

#include <termios.h>

typedef struct termios rawModeConfig;
typedef unsigned int platformError;

#elif defined(_WIN32)

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
typedef struct stdinWriterImpl stdinWriter;

typedef struct stdinReaderResult {
	stdinReader *reader;
	platformError error;
} stdinReaderResult;

typedef struct stdinWriterResult {
	stdinWriter *writer;
	platformError error;
} stdinWriterResult;

typedef struct stdinRead {
	int count;
	platformError error;
} stdinRead;

stdinReaderResult stdinReader_init();
stdinRead stdinReader_read(stdinReader *reader, void *buffer, int count);
stdinRead stdinReader_readWithTimeout(stdinReader *reader, void *buffer, int count, int timeoutMillis);
platformError stdinReader_interrupt(stdinReader* reader);
platformError stdinReader_free(stdinReader *reader);

stdinWriterResult stdinWriter_init();
stdinReader *stdinWriter_getReader(stdinWriter *writer);
platformError stdinWriter_write(stdinWriter *writer, void *buffer, int count);
platformError stdinWriter_free(stdinWriter *writer);

#endif // MOSAIC_H
