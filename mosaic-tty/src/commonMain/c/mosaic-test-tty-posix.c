#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-tty-posix.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>

typedef struct MosaicTestTtyImpl {
	int stdin_write_fd;
	MosaicTty *tty;
} MosaicTestTtyImpl;

MosaicTestTtyInitResult testTty_init() {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	int stdinPipe[2];
	if (unlikely(pipe(stdinPipe)) != 0) {
		result.error = errno;
		goto err;
	}
	int stdinReadFd = stdinPipe[0];
	int stdinWriteFd = stdinPipe[1];

	int stdoutWriteFd = STDOUT_FILENO;
	int stderrWriteFd = STDERR_FILENO;

	MosaicTtyInitResult ttyInitResult = tty_initWithFds(stdinReadFd, stdoutWriteFd, stderrWriteFd);
	if (unlikely(ttyInitResult.error)) {
		result.error = ttyInitResult.error;
		goto err;
	}

	testTty->stdin_write_fd = stdinWriteFd;
	testTty->tty = ttyInitResult.tty;

	result.testTty = testTty;

	ret:
	return result;

	err:
	free(testTty);
	goto ret;
}

MosaicTty *testTty_getTty(MosaicTestTty *testTty) {
	return testTty->tty;
}

uint32_t testTty_write(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	int stdinWriteFd = testTty->stdin_write_fd;
	while (count > 0) {
		int result = write(stdinWriteFd, buffer, count);
		if (unlikely(result == -1)) {
			goto err;
		}
		count = count - result;
	}
	return 0;

	err:
	return errno;
}

uint32_t testTty_focusEvent(MosaicTestTty *testTty UNUSED, bool focused UNUSED) {
	// Focus events are delivered through VT sequences.
	return 0;
}

uint32_t testTty_keyEvent(MosaicTestTty *testTty UNUSED) {
	// Key events are delivered through VT sequences.
	return 0;
}

uint32_t testTty_mouseEvent(MosaicTestTty *testTty UNUSED) {
	// Mouse events are delivered through VT sequences.
	return 0;
}

uint32_t testTty_resizeEvent(MosaicTestTty *testTty, int columns, int rows, int width, int height) {
	MosaicTtyCallback *callback = testTty->tty->callback;
	if (callback) {
		callback->onResize(callback->opaque, columns, rows, width, height);
	}
	return 0;
}

uint32_t testTty_free(MosaicTestTty *testTty) {
	uint32_t result = 0;

	if (unlikely(close(testTty->stdin_write_fd) != 0)) {
		result = errno;
	}
	if (unlikely(close(testTty->tty->stdin_read_fd) != 0 && result != 0)) {
		result = errno;
	}

	free(testTty);

	return result;
}

#endif
