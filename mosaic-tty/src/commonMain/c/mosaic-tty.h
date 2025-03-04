#ifndef MOSAIC_TTY_H
#define MOSAIC_TTY_H

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

MosaicTtyInitResult tty_init();
void tty_setCallback(MosaicTty *tty, MosaicTtyCallback *callback);
MosaicTtyIoResult tty_readInput(MosaicTty *tty, uint8_t *buffer, int count);
MosaicTtyIoResult tty_readInputWithTimeout(MosaicTty *tty, uint8_t *buffer, int count, int timeoutMillis);
uint32_t tty_interruptRead(MosaicTty *tty);
MosaicTtyIoResult tty_writeOutput(MosaicTty *tty, uint8_t *buffer, int count);
MosaicTtyIoResult tty_writeError(MosaicTty *tty, uint8_t *buffer, int count);
uint32_t tty_enableRawMode(MosaicTty *tty);
uint32_t tty_enableWindowResizeEvents(MosaicTty *tty);
MosaicTtyTerminalSizeResult tty_currentTerminalSize(MosaicTty *tty);
uint32_t tty_free(MosaicTty *tty);

#endif // MOSAIC_TTY_H
