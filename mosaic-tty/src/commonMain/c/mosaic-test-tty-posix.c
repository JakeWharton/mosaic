#if defined(__APPLE__) || defined(__linux__)

#define _XOPEN_SOURCE 600

#include "mosaic-tty-posix.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <termios.h>
#include <unistd.h>

typedef struct MosaicTestTtyImpl {
	int fd;
	int interrupt_read_fd;
	int interrupt_write_fd;
	MosaicTty *tty;
} MosaicTestTtyImpl;

uint32_t testTty_resizeInternal(int parentFd, int columns, int rows, int width, int height) {
	struct winsize size = {};
	size.ws_col = columns;
	size.ws_row = rows;
	size.ws_xpixel = width;
	size.ws_ypixel = height;
	if (likely(ioctl(parentFd, TIOCSWINSZ, &size) != -1)) {
		return 0;
	}
	return errno;
}

MosaicTestTtyInitResult testTty_init() {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	int interrupt_pipe[2];
	if (unlikely(pipe(interrupt_pipe)) != 0) {
		result.error = errno;
		goto err_free;
	}

	// Note: the terms of art here appear to be "master" and "slave".
	// We use "parent" and "child" instead, respectively.
	int parentFd = posix_openpt(O_RDWR | O_NOCTTY);
	if (unlikely(parentFd == -1 || grantpt(parentFd) || unlockpt(parentFd))) {
		result.error = errno;
		goto err_pipes;
	}

	char *childName = ptsname(parentFd);
	int childFd = open(childName, O_RDWR | O_NOCTTY);
	if (unlikely(childFd == -1)) {
		result.error = errno;
		goto err_parent;
	}

	// Give the TTY a reasonable "default" size.
	uint32_t sizeResult = testTty_resizeInternal(parentFd, 80, 24, 0, 0);
	if (unlikely(sizeResult)) {
		result.error = sizeResult;
		goto err_parent;
	}

	MosaicTtyInitResult ttyInitResult = tty_initWithFd(childFd);
	if (unlikely(!ttyInitResult.tty)) {
		result.error = ttyInitResult.error;
		result.already_bound = ttyInitResult.already_bound;
		goto err_child;
	}

	testTty->fd = parentFd;
	testTty->interrupt_read_fd = interrupt_pipe[0];
	testTty->interrupt_write_fd = interrupt_pipe[1];
	testTty->tty = ttyInitResult.tty;

	result.testTty = testTty;

	ret:
	return result;

	err_child:
	close(childFd);

	err_parent:
	close(parentFd);

	err_pipes:
	close(interrupt_pipe[0]);
	close(interrupt_pipe[1]);

	err_free:
	free(testTty);
	goto ret;
}

MosaicTty *testTty_getTty(MosaicTestTty *testTty) {
	return testTty->tty;
}

MosaicTtyIoResult testTty_write(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return tty_writeInternal(testTty->fd, buffer, count);
}

MosaicTtyIoResult testTty_read(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return tty_readInternal(testTty->fd, testTty->interrupt_read_fd, buffer, count, NULL);
}

uint32_t testTty_interruptRead(MosaicTestTty *testTty) {
	uint8_t space = ' ';
	MosaicTtyIoResult result = tty_writeInternal(testTty->interrupt_write_fd, &space, 1);
	return result.error;
}

uint32_t testTty_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height) {
	uint32_t sizeResult = testTty_resizeInternal(testTty->fd, columns, rows, width, height);
	if (unlikely(sizeResult)) {
		return sizeResult;
	}

	// TODO Why can't I reference SIGWINCH here but I can in mosaic-tty-posix.c?
	if (unlikely(raise(28))) {
		return errno;
	}

	return 0;
}

uint32_t testTty_sendFocusEvent(MosaicTestTty *testTty UNUSED, bool focused UNUSED) {
	// Focus events are delivered through VT sequences.
	return 0;
}

uint32_t testTty_sendKeyEvent(MosaicTestTty *testTty UNUSED) {
	// Key events are delivered through VT sequences.
	return 0;
}

uint32_t testTty_sendMouseEvent(MosaicTestTty *testTty UNUSED) {
	// Mouse events are delivered through VT sequences.
	return 0;
}

uint32_t testTty_free(MosaicTestTty *testTty) {
	uint32_t result = 0;

	if (unlikely(close(testTty->fd) != 0)) {
		result = errno;
	}

	if (unlikely(close(testTty->interrupt_read_fd) != 0 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(testTty->interrupt_write_fd) != 0 && result == 0)) {
		result = errno;
	}

	free(testTty);

	return result;
}

#endif
