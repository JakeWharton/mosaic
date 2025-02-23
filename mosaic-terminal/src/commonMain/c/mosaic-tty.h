#ifndef MOSAIC_TTY_H
#define MOSAIC_TTY_H

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

typedef struct platformInputResult {
	platformInput *input;
	uint32_t error;
} platformInputResult;

typedef struct stdinRead {
	int count;
	uint32_t error;
} stdinRead;

typedef struct terminalSizeResult {
	int columns;
	int rows;
	int width;
	int height;
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

#endif // MOSAIC_TTY_H
