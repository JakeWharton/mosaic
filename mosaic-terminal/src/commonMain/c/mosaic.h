#ifndef MOSAIC_H
#define MOSAIC_H

#include <stdbool.h>
#include <stdint.h>

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
	uint32_t error;
} platformInputResult;

typedef struct platformInputWriterResult {
	platformInputWriter *writer;
	uint32_t error;
} platformInputWriterResult;

typedef struct stdinRead {
	int count;
	uint32_t error;
} stdinRead;

typedef struct terminalSize {
	int columns;
	int rows;
	int width;
	int height;
} terminalSize;

typedef struct terminalSizeResult {
	terminalSize size;
	uint32_t error;
} terminalSizeResult;

platformInputResult platformInput_init(platformEventHandler *handler);
stdinRead platformInput_read(platformInput *input, char *buffer, int count);
stdinRead platformInput_readWithTimeout(platformInput *input, char *buffer, int count, int timeoutMillis);
uint32_t platformInput_interrupt(platformInput *input);
uint32_t platformInput_enableRawMode(platformInput *input);
uint32_t platformInput_enableWindowResizeEvents(platformInput *input);
terminalSizeResult platformInput_currentTerminalSize(platformInput *input);
uint32_t platformInput_free(platformInput *input);

platformInputWriterResult platformInputWriter_init(platformEventHandler *handler);
platformInput *platformInputWriter_getPlatformInput(platformInputWriter *writer);
uint32_t platformInputWriter_write(platformInputWriter *writer, char *buffer, int count);
uint32_t platformInputWriter_focusEvent(platformInputWriter *writer, bool focused);
uint32_t platformInputWriter_keyEvent(platformInputWriter *writer);
uint32_t platformInputWriter_mouseEvent(platformInputWriter *writer);
uint32_t platformInputWriter_resizeEvent(platformInputWriter *writer, int columns, int rows, int width, int height);
uint32_t platformInputWriter_free(platformInputWriter *writer);

#endif // MOSAIC_H
