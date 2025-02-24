#ifndef MOSAIC_TTY_H
#define MOSAIC_TTY_H

#include <stdbool.h>
#include <stdint.h>

typedef void MosaicTtyCallbackOnFocus(void *opaque, bool focused);
typedef void MosaicTtyCallbackOnKey(void *opaque); // TODO params
typedef void MosaicTtyCallbackOnMouse(void *opaque); // TODO params
typedef void MosaicTtyCallbackOnResize(void *opaque, int columns, int rows, int width, int height);

typedef struct MosaicTtyCallback {
	void *opaque;
	MosaicTtyCallbackOnFocus *onFocus;
	MosaicTtyCallbackOnKey *onKey;
	MosaicTtyCallbackOnMouse *onMouse;
	MosaicTtyCallbackOnResize *onResize;
} MosaicTtyCallback;


typedef struct MosaicTtyImpl MosaicTty;

typedef struct MosaicTtyInitResult {
	MosaicTty *tty;
	uint32_t error;
} MosaicTtyInitResult;

typedef struct MosaicTtyIoResult {
	int count;
	uint32_t error;
} MosaicTtyIoResult;

typedef struct MosaicTtyTerminalSizeResult {
	int columns;
	int rows;
	int width;
	int height;
	uint32_t error;
} MosaicTtyTerminalSizeResult;

MosaicTtyInitResult tty_init(MosaicTtyCallback *callback);
MosaicTtyIoResult tty_read(MosaicTty *tty, char *buffer, int count);
MosaicTtyIoResult tty_readWithTimeout(MosaicTty *tty, char *buffer, int count, int timeoutMillis);
uint32_t tty_interrupt(MosaicTty *tty);
uint32_t tty_enableRawMode(MosaicTty *tty);
uint32_t tty_enableWindowResizeEvents(MosaicTty *tty);
MosaicTtyTerminalSizeResult tty_currentTerminalSize(MosaicTty *tty);
uint32_t tty_free(MosaicTty *tty);

#endif // MOSAIC_TTY_H
