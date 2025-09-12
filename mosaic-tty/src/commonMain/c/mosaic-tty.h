#ifndef MOSAIC_TTY_H
#define MOSAIC_TTY_H

#include "mosaic-utils.h"

#include <stdbool.h>
#include <stdint.h>

typedef struct MosaicTtyImpl MosaicTty;

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

typedef struct MosaicTtyInitResult {
	MosaicTty *tty;
	uint32_t error;
	bool no_tty;
	bool already_bound;
} MosaicTtyInitResult;

typedef struct MosaicTtyTerminalSizeResult {
	int columns;
	int rows;
	int width;
	int height;
	uint32_t error;
} MosaicTtyTerminalSizeResult;

MOSAIC_EXPORT MosaicTtyInitResult tty_init();

MOSAIC_EXPORT void tty_setCallback(MosaicTty *tty, MosaicTtyCallback *callback);

MOSAIC_EXPORT MosaicIoResult tty_read(MosaicTty *tty, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult tty_readWithTimeout(MosaicTty *tty, uint8_t *buffer, int count, int timeoutMillis);
MOSAIC_EXPORT uint32_t tty_interruptRead(MosaicTty *tty);

MOSAIC_EXPORT MosaicIoResult tty_write(MosaicTty *tty, uint8_t *buffer, int count);

MOSAIC_EXPORT uint32_t tty_enableRawMode(MosaicTty *tty);
MOSAIC_EXPORT uint32_t tty_enableWindowResizeEvents(MosaicTty *tty);
MOSAIC_EXPORT MosaicTtyTerminalSizeResult tty_currentTerminalSize(MosaicTty *tty);

MOSAIC_EXPORT uint32_t tty_reset(MosaicTty *tty);
MOSAIC_EXPORT uint32_t tty_free(MosaicTty *tty);

#endif // MOSAIC_TTY_H
