#ifndef MOSAIC_TEST_TTY_H
#define MOSAIC_TEST_TTY_H

#include "mosaic-tty.h"
#include "mosaic-utils.h"

#include <stdbool.h>
#include <stdint.h>

typedef struct MosaicTestTerminalImpl MosaicTestTty;

typedef struct MosaicTestTerminalInitResult {
	MosaicTestTty *test_tty;
	uint32_t error;
	bool already_bound;
} MosaicTestTerminalInitResult;

MOSAIC_EXPORT MosaicTestTerminalInitResult mosaic_test_init(bool stdinIsTty, bool stdoutIsTty, bool stderrIsTty);
MOSAIC_EXPORT MosaicTty *mosaic_test_get_tty(MosaicTestTty *testTty);
MOSAIC_EXPORT MosaicStreams *mosaic_test_get_streams(MosaicTestTty *testTty);

MOSAIC_EXPORT MosaicIoResult mosaic_test_write_tty(MosaicTestTty *testTty, uint8_t *buffer, int count);

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_tty(MosaicTestTty *testTty, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult mosaic_test_read_tty_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis);
MOSAIC_EXPORT uint32_t mosaic_test_interrupt_tty_read(MosaicTestTty *testTty);

MOSAIC_EXPORT MosaicIoResult mosaic_test_write_input(MosaicTestTty *testTty, uint8_t *buffer, int count);

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_output(MosaicTestTty *testTty, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult mosaic_test_read_output_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis);
MOSAIC_EXPORT uint32_t mosaic_test_interrupt_output_read(MosaicTestTty *testTty);

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_error(MosaicTestTty *testTty, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult mosaic_test_read_error_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis);
MOSAIC_EXPORT uint32_t mosaic_test_interrupt_error_read(MosaicTestTty *testTty);

MOSAIC_EXPORT uint32_t mosaic_test_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height);

MOSAIC_EXPORT uint32_t mosaic_test_send_focus_event(MosaicTestTty *testTty, bool focused);
MOSAIC_EXPORT uint32_t mosaic_test_send_key_event(MosaicTestTty *testTty);
MOSAIC_EXPORT uint32_t mosaic_test_send_mouse_event(MosaicTestTty *testTty);

MOSAIC_EXPORT uint32_t mosaic_test_free(MosaicTestTty *testTty);

#endif // MOSAIC_TEST_TTY_H
