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


typedef struct platformInputImpl platformInput;
typedef struct platformInputWriterImpl platformInputWriter;

typedef struct platformInputResult {
	platformInput *reader;
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

platformInputResult platformInput_init(platformEventHandler *handler);
stdinRead platformInput_read(platformInput *reader, void *buffer, int count);
stdinRead platformInput_readWithTimeout(platformInput *reader, void *buffer, int count, int timeoutMillis);
platformError platformInput_interrupt(platformInput* reader);
platformError platformInput_free(platformInput *reader);

platformInputWriterResult platformInputWriter_init(platformEventHandler *handler);
platformInput *platformInputWriter_getReader(platformInputWriter *writer);
platformError platformInputWriter_write(platformInputWriter *writer, void *buffer, int count);
void platformInputWriter_focusEvent(platformInputWriter *writer, bool focused);
void platformInputWriter_keyEvent(platformInputWriter *writer);
void platformInputWriter_mouseEvent(platformInputWriter *writer);
void platformInputWriter_resizeEvent(platformInputWriter *writer, int columns, int rows, int width, int height);
platformError platformInputWriter_free(platformInputWriter *writer);

#endif // MOSAIC_H
