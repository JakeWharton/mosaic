#ifndef MOSAIC_TEST_TTY_H
#define MOSAIC_TEST_TTY_H

#include "mosaic-tty.h"
#include "mosaic-utils.h"

#include <stdbool.h>
#include <stdint.h>

typedef struct MosaicTestTtyImpl MosaicTestTty;

typedef struct MosaicTestTtyInitResult {
	MosaicTestTty *testTty;
	uint32_t error;
	bool already_bound;
} MosaicTestTtyInitResult;

MOSAIC_EXPORT MosaicTestTtyInitResult testTty_init(bool stdinIsTty, bool stdoutIsTty, bool stderrIsTty);
MOSAIC_EXPORT MosaicTty *testTty_getTty(MosaicTestTty *testTty);
MOSAIC_EXPORT MosaicTtyIoResult testTty_write(MosaicTestTty *testTty, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicTtyIoResult testTty_read(MosaicTestTty *testTty, uint8_t *buffer, int count);
MOSAIC_EXPORT uint32_t testTty_interruptRead(MosaicTestTty *testTty);
MOSAIC_EXPORT uint32_t testTty_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height);
MOSAIC_EXPORT uint32_t testTty_sendFocusEvent(MosaicTestTty *testTty, bool focused);
MOSAIC_EXPORT uint32_t testTty_sendKeyEvent(MosaicTestTty *testTty);
MOSAIC_EXPORT uint32_t testTty_sendMouseEvent(MosaicTestTty *testTty);
MOSAIC_EXPORT uint32_t testTty_free(MosaicTestTty *testTty);

#endif // MOSAIC_TEST_TTY_H
