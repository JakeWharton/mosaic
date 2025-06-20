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
	bool no_tty;
	bool already_bound;
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

extern MosaicTtyInitResult tty_init();
extern void tty_setCallback(MosaicTty *tty, MosaicTtyCallback *callback);
extern MosaicTtyIoResult tty_read(MosaicTty *tty, uint8_t *buffer, int count);
extern MosaicTtyIoResult tty_readWithTimeout(MosaicTty *tty, uint8_t *buffer, int count, int timeoutMillis);
extern uint32_t tty_interruptRead(MosaicTty *tty);
extern MosaicTtyIoResult tty_write(MosaicTty *tty, uint8_t *buffer, int count);
extern uint32_t tty_enableRawMode(MosaicTty *tty);
extern uint32_t tty_enableWindowResizeEvents(MosaicTty *tty);
extern MosaicTtyTerminalSizeResult tty_currentTerminalSize(MosaicTty *tty);
extern uint32_t tty_reset(MosaicTty *tty);
extern uint32_t tty_free(MosaicTty *tty);

#endif // MOSAIC_TTY_H
