#ifndef MOSAIC_H
#define MOSAIC_H

#include <stdbool.h>

#if defined(__APPLE__) || defined(__linux__)

#include <termios.h>

typedef struct termios rawModeConfig;
typedef unsigned int platformError;

#elif defined(_WIN32)

#include <windows.h>

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

typedef void InputHandlerOnRead(void *opaque);
typedef void InputHandlerOnFocus(void *opaque, bool focused);
typedef void InputHandlerOnKey(void *opaque); // TODO params
typedef void InputHandlerOnMouse(void *opaque); // TODO params
typedef void InputHandlerOnResize(void *opaque, int columns, int rows, int width, int height);

typedef struct inputHandler {
	void *opaque;
	InputHandlerOnFocus *onFocus;
	InputHandlerOnKey *onKey;
	InputHandlerOnMouse *onMouse;
	InputHandlerOnResize *onResize;
} inputHandler;

stdinReaderResult stdinReader_init(inputHandler *handler);
stdinRead stdinReader_read(stdinReader *reader, void *buffer, int count);
stdinRead stdinReader_readWithTimeout(stdinReader *reader, void *buffer, int count, int timeoutMillis);
platformError stdinReader_interrupt(stdinReader* reader);
platformError stdinReader_free(stdinReader *reader);

stdinWriterResult stdinWriter_init(inputHandler *handler);
stdinReader *stdinWriter_getReader(stdinWriter *writer);
platformError stdinWriter_write(stdinWriter *writer, void *buffer, int count);
void stdinWriter_focusEvent(stdinWriter *writer, bool focused);
void stdinWriter_keyEvent(stdinWriter *writer);
void stdinWriter_mouseEvent(stdinWriter *writer);
void stdinWriter_resizeEvent(stdinWriter *writer, int columns, int rows, int width, int height);
platformError stdinWriter_free(stdinWriter *writer);

#endif // MOSAIC_H
