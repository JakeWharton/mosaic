#if defined(__APPLE__) || defined(__linux__)

#define _XOPEN_SOURCE 600

#include "mosaic-tty-posix.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>

typedef struct MosaicTestTtyImpl {
	int fd;
	int interrupt_read_fd;
	int interrupt_write_fd;
	MosaicTty *tty;
} MosaicTestTtyImpl;

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
