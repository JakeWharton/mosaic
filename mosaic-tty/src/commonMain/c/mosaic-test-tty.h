#ifndef MOSAIC_TEST_TTY_H
#define MOSAIC_TEST_TTY_H

#include "mosaic-tty.h"
#include <stdbool.h>
#include <stdint.h>

typedef struct MosaicTestTtyImpl MosaicTestTty;

typedef struct MosaicTestTtyInitResult {
	MosaicTestTty *testTty;
	uint32_t error;
	bool already_bound;
} MosaicTestTtyInitResult;

MosaicTestTtyInitResult testTty_init();
MosaicTty *testTty_getTty(MosaicTestTty *testTty);
MosaicTtyIoResult testTty_write(MosaicTestTty *testTty, uint8_t *buffer, int count);
MosaicTtyIoResult testTty_read(MosaicTestTty *testTty, uint8_t *buffer, int count);
uint32_t testTty_interruptRead(MosaicTestTty *testTty);
uint32_t testTty_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height);
uint32_t testTty_sendFocusEvent(MosaicTestTty *testTty, bool focused);
uint32_t testTty_sendKeyEvent(MosaicTestTty *testTty);
uint32_t testTty_sendMouseEvent(MosaicTestTty *testTty);
uint32_t testTty_free(MosaicTestTty *testTty);

#endif // MOSAIC_TEST_TTY_H
