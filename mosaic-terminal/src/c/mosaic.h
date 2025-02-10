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


typedef void PlatformEventHandlerOnRead(void *opaque);
typedef void PlatformEventHandlerOnFocus(void *opaque, bool focused);
typedef void PlatformEventHandlerOnKey(void *opaque); // TODO params
typedef void PlatformEventHandlerOnMouse(void *opaque); // TODO params
typedef void PlatformEventHandlerOnResize(void *opaque, int columns, int rows, int width, int height);

typedef struct platformEventHandler {
	void *opaque;
	PlatformEventHandlerOnFocus *onFocus;
	PlatformEventHandlerOnKey *onKey;
	PlatformEventHandlerOnMouse *onMouse;
	PlatformEventHandlerOnResize *onResize;
} platformEventHandler;


typedef struct platformInputImpl platformInput;
typedef struct platformInputWriterImpl platformInputWriter;

typedef struct platformInputResult {
	platformInput *input;
	platformError error;
} platformInputResult;

typedef struct platformInputWriterResult {
	platformInputWriter *writer;
	platformError error;
} platformInputWriterResult;

typedef struct stdinRead {
	int count;
	platformError error;
} stdinRead;

typedef struct terminalSize {
	int columns;
	int rows;
	int width;
	int height;
} terminalSize;

typedef struct terminalSizeResult {
	terminalSize size;
	platformError error;
} terminalSizeResult;

platformInputResult platformInput_init(platformEventHandler *handler);
stdinRead platformInput_read(platformInput *input, char *buffer, int count);
stdinRead platformInput_readWithTimeout(platformInput *input, char *buffer, int count, int timeoutMillis);
platformError platformInput_interrupt(platformInput *input);
platformError platformInput_enableWindowResizeEvents(platformInput *input);
terminalSizeResult platformInput_currentTerminalSize(platformInput *input);
platformError platformInput_free(platformInput *input);

platformInputWriterResult platformInputWriter_init(platformEventHandler *handler);
platformInput *platformInputWriter_getPlatformInput(platformInputWriter *writer);
platformError platformInputWriter_write(platformInputWriter *writer, char *buffer, int count);
platformError platformInputWriter_focusEvent(platformInputWriter *writer, bool focused);
platformError platformInputWriter_keyEvent(platformInputWriter *writer);
platformError platformInputWriter_mouseEvent(platformInputWriter *writer);
platformError platformInputWriter_resizeEvent(platformInputWriter *writer, int columns, int rows, int width, int height);
platformError platformInputWriter_free(platformInputWriter *writer);

#endif // MOSAIC_H
