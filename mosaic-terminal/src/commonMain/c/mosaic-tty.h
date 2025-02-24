#ifndef MOSAIC_TTY_H
#define MOSAIC_TTY_H

#include <stdbool.h>
#include <stdint.h>

typedef void PlatformInputCallbackOnFocus(void *opaque, bool focused);
typedef void PlatformInputCallbackOnKey(void *opaque); // TODO params
typedef void PlatformInputCallbackOnMouse(void *opaque); // TODO params
typedef void PlatformInputCallbackOnResize(void *opaque, int columns, int rows, int width, int height);

typedef struct platformInputCallback {
	void *opaque;
	PlatformInputCallbackOnFocus *onFocus;
	PlatformInputCallbackOnKey *onKey;
	PlatformInputCallbackOnMouse *onMouse;
	PlatformInputCallbackOnResize *onResize;
} platformInputCallback;


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

platformInputResult platformInput_init(platformInputCallback *callback);
stdinRead platformInput_read(platformInput *input, char *buffer, int count);
stdinRead platformInput_readWithTimeout(platformInput *input, char *buffer, int count, int timeoutMillis);
uint32_t platformInput_interrupt(platformInput *input);
uint32_t platformInput_enableRawMode(platformInput *input);
uint32_t platformInput_enableWindowResizeEvents(platformInput *input);
terminalSizeResult platformInput_currentTerminalSize(platformInput *input);
uint32_t platformInput_free(platformInput *input);

#endif // MOSAIC_TTY_H
