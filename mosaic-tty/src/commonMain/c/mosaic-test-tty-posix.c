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
	MosaicTty *tty;
} MosaicTestTtyImpl;

MosaicTestTtyInitResult testTty_init() {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	// Note: the terms of art here appear to be "master" and "slave".
	// We use "parent" and "child" instead, respectively.
	int parentFd = posix_openpt(O_RDWR | O_NOCTTY);
	if (unlikely(parentFd == -1 || grantpt(parentFd) || unlockpt(parentFd))) {
		result.error = errno;
		goto err;
	}

	char *childName = ptsname(parentFd);
	int childFd = open(childName, O_RDWR | O_NOCTTY);

	if (unlikely(childFd == -1)) {
		result.error = errno;
		goto err;
	}

	MosaicTtyInitResult ttyInitResult = tty_initWithFd(childFd);
	if (unlikely(ttyInitResult.error)) {
		result.error = ttyInitResult.error;
		goto err;
	}

	testTty->fd = parentFd;
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

MosaicTtyIoResult testTty_writeInput(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	MosaicTtyIoResult result = {};

	int written = write(testTty->fd, buffer, count);
	if (written != -1) {
		result.count = written;
	} else {
		result.error = errno;
	}

	return result;
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

	free(testTty);

	return result;
}

#endif
