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

MOSAIC_EXPORT MosaicTestTtyInitResult mosaic_test_init(bool stdinIsTty, bool stdoutIsTty, bool stderrIsTty);
MOSAIC_EXPORT MosaicTty *mosaic_test_get_tty(MosaicTestTty *testTty);
MOSAIC_EXPORT MosaicStreams *mosaic_test_get_streams(MosaicTestTty *testTty);

MOSAIC_EXPORT MosaicIoResult mosaic_test_write(MosaicTestTty *testTty, uint8_t *buffer, int count);

MOSAIC_EXPORT MosaicIoResult mosaic_test_read(MosaicTestTty *testTty, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult mosaic_test_read_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis);
MOSAIC_EXPORT uint32_t mosaic_test_interrupt_read(MosaicTestTty *testTty);

MOSAIC_EXPORT uint32_t mosaic_test_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height);

MOSAIC_EXPORT uint32_t mosaic_test_send_focus_event(MosaicTestTty *testTty, bool focused);
MOSAIC_EXPORT uint32_t mosaic_test_send_key_event(MosaicTestTty *testTty);
MOSAIC_EXPORT uint32_t mosaic_test_send_mouse_event(MosaicTestTty *testTty);

MOSAIC_EXPORT uint32_t mosaic_test_free(MosaicTestTty *testTty);

#endif // MOSAIC_TEST_TTY_H
