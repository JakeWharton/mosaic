#ifndef MOSAIC_TEST_TTY_H
#define MOSAIC_TEST_TTY_H

#include "cutils.h"
#include "mosaic-tty.h"
#include <stdbool.h>
#include <stdint.h>

typedef struct MosaicTestTtyImpl MosaicTestTty;

typedef struct MosaicTestTtyInitResult {
	MosaicTestTty *testTty;
	uint32_t error;
	bool already_bound;
} MosaicTestTtyInitResult;

MOSAIC_EXPORT MosaicTestTtyInitResult MOSAIC_STDCALL testTty_init();
MOSAIC_EXPORT MosaicTty MOSAIC_STDCALL *testTty_getTty(MosaicTestTty *testTty);
MOSAIC_EXPORT MosaicTtyIoResult MOSAIC_STDCALL testTty_write(MosaicTestTty *testTty, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicTtyIoResult MOSAIC_STDCALL testTty_read(MosaicTestTty *testTty, uint8_t *buffer, int count);
MOSAIC_EXPORT uint32_t MOSAIC_STDCALL testTty_interruptRead(MosaicTestTty *testTty);
MOSAIC_EXPORT uint32_t MOSAIC_STDCALL testTty_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height);
MOSAIC_EXPORT uint32_t MOSAIC_STDCALL testTty_sendFocusEvent(MosaicTestTty *testTty, bool focused);
MOSAIC_EXPORT uint32_t MOSAIC_STDCALL testTty_sendKeyEvent(MosaicTestTty *testTty);
MOSAIC_EXPORT uint32_t MOSAIC_STDCALL testTty_sendMouseEvent(MosaicTestTty *testTty);
MOSAIC_EXPORT uint32_t MOSAIC_STDCALL testTty_free(MosaicTestTty *testTty);

#endif // MOSAIC_TEST_TTY_H
