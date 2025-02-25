#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-tty-posix.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>

static const int INVALID_FD = -1;

typedef struct MosaicTestTtyImpl {
	MosaicTty *tty;
	int stdin_write_fd;
	int stdout_read_fd;
	int stderr_read_fd;
	bool fake_output_and_error;
} MosaicTestTtyImpl;

MosaicTestTtyInitResult testTty_init(MosaicTtyCallback *callback, bool fakeOutputAndError) {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	int aPipe[2];
	if (unlikely(pipe(aPipe)) != 0) {
		result.error = errno;
		goto err;
	}
	int stdinReadFd = aPipe[0];
	int stdinWriteFd = aPipe[1];

	int stdoutReadFd = INVALID_FD;
	int stdoutWriteFd = STDOUT_FILENO;
	int stderrReadFd = INVALID_FD;
	int stderrWriteFd = STDERR_FILENO;
	if (fakeOutputAndError) {
		if (unlikely(pipe(aPipe)) != 0) {
			result.error = errno;
			goto err;
		}
		stdoutReadFd = aPipe[0];
		stdoutWriteFd = aPipe[1];

		if (unlikely(pipe(aPipe)) != 0) {
			result.error = errno;
			goto err;
		}
		stderrReadFd = aPipe[0];
		stderrWriteFd = aPipe[1];
	}

	MosaicTtyInitResult ttyInitResult = tty_initWithFds(stdinReadFd, stdoutWriteFd, stderrWriteFd, callback);
	if (unlikely(ttyInitResult.error)) {
		result.error = ttyInitResult.error;
		goto err;
	}

	testTty->tty = ttyInitResult.tty;
	testTty->stdin_write_fd = stdinWriteFd;
	testTty->stdout_read_fd = stdoutReadFd;
	testTty->stderr_read_fd = stderrReadFd;
	testTty->fake_output_and_error = fakeOutputAndError;

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

MosaicTtyIoResult testTty_writeInput(MosaicTestTty *testTty, char *buffer, int count) {
	return tty_writeInternal(testTty->stdin_write_fd, buffer, count);
}

MosaicTtyIoResult testTty_readInternal(int fd, char *buffer, int count) {
	MosaicTtyIoResult result = {};
	if (fd == INVALID_FD) {
		result.count = -1;
	} else {
		int written = read(fd, buffer, count);
		if (written != -1) {
			result.count = written;
		} else {
			result.error = errno;
		}
	}

	return result;
}

MosaicTtyIoResult testTty_readOutput(MosaicTestTty *testTty, char *buffer, int count) {
	return testTty_readInternal(testTty->stdout_read_fd, buffer, count);
}

MosaicTtyIoResult testTty_readError(MosaicTestTty *testTty, char *buffer, int count) {
	return testTty_readInternal(testTty->stderr_read_fd, buffer, count);
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
	uint32_t result = 0;

	if (unlikely(close(testTty->stdin_write_fd))) {
		result = errno;
	}
	if (unlikely(close(testTty->tty->stdin_read_fd) && !result)) {
		result = errno;
	}

	if (testTty->fake_output_and_error) {
		if (unlikely(close(testTty->stdout_read_fd) && !result)) {
			result = errno;
		}
		if (unlikely(close(testTty->tty->stdout_write_fd) && !result)) {
			result = errno;
		}
		if (unlikely(close(testTty->stderr_read_fd) && !result)) {
			result = errno;
		}
		if (unlikely(close(testTty->tty->stderr_write_fd) && !result)) {
			result = errno;
		}
	}

	free(testTty);

	return result;
}

#endif
