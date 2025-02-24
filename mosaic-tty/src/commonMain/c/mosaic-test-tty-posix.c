#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-tty-posix.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>

typedef struct MosaicTestTtyImpl {
	int pipe[2];
	MosaicTty *tty;
} MosaicTestTtyImpl;

MosaicTestTtyInitResult testTty_init(MosaicTtyCallback *callback) {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(pipe(testTty->pipe)) != 0) {
		result.error = errno;
		goto err;
	}

	MosaicTtyInitResult ttyInitResult = tty_initWithFd(testTty->pipe[0], callback);
	if (unlikely(ttyInitResult.error)) {
		result.error = ttyInitResult.error;
		goto err;
	}
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

uint32_t testTty_write(MosaicTestTty *testTty, char *buffer, int count) {
	int pipeOut = testTty->pipe[1];
	while (count > 0) {
		int result = write(pipeOut, buffer, count);
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
	callback->onResize(callback->opaque, columns, rows, width, height);
	return 0;
}

uint32_t testTty_free(MosaicTestTty *testTty) {
	int *pipe = testTty->pipe;

	int result = 0;
	if (unlikely(close(pipe[0]) != 0)) {
		result = errno;
	}
	if (unlikely(close(pipe[1]) != 0 && result != 0)) {
		result = errno;
	}
	free(testTty);
	return result;
}

#endif
