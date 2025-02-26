#ifndef MOSAIC_TEST_TTY_H
#define MOSAIC_TEST_TTY_H

#include "mosaic-tty.h"
#include <stdbool.h>
#include <stdint.h>

typedef struct MosaicTestTtyImpl MosaicTestTty;

typedef struct MosaicTestTtyInitResult {
	MosaicTestTty *testTty;
	uint32_t error;
} MosaicTestTtyInitResult;

MosaicTestTtyInitResult testTty_init(MosaicTtyCallback *callback);
MosaicTty *testTty_getTty(MosaicTestTty *testTty);
uint32_t testTty_write(MosaicTestTty *testTty, uint8_t *buffer, int count);
uint32_t testTty_focusEvent(MosaicTestTty *testTty, bool focused);
uint32_t testTty_keyEvent(MosaicTestTty *testTty);
uint32_t testTty_mouseEvent(MosaicTestTty *testTty);
uint32_t testTty_resizeEvent(MosaicTestTty *testTty, int columns, int rows, int width, int height);
uint32_t testTty_free(MosaicTestTty *testTty);

#endif // MOSAIC_TEST_TTY_H
