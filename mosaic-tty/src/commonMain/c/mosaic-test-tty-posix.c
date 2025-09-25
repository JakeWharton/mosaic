#if defined(__APPLE__) || defined(__linux__)

#define _XOPEN_SOURCE 600

#include "mosaic-streams-posix.h"
#include "mosaic-test-tty.h"
#include "mosaic-tty-posix.h"
#include "mosaic-utils.h"

#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <termios.h>
#include <unistd.h>

typedef struct MosaicTestTtyImpl {
	MosaicStreams *streams;
	MosaicTty *tty;
	int parent_fd;
	int parent_fd_interrupt_reader;
	int parent_fd_interrupt_writer;
} MosaicTestTtyImpl;

uint32_t mosaic_test_resize_internal(int parentFd, int columns, int rows, int width, int height) {
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

MosaicTestTtyInitResult mosaic_test_init(bool stdinIsTty, bool stdoutIsTty, bool stderrIsTty) {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	// Note: the terms of art here appear to be "master" and "slave".
	// We use "parent" and "child" instead, respectively.
	int parentFd = posix_openpt(O_RDWR | O_NOCTTY);
	if (unlikely(parentFd == -1)) {
		result.error = errno;
		goto err_free;
	}
	if (unlikely(grantpt(parentFd) || unlockpt(parentFd))) {
		goto err_parent;
	}

	char *childName = ptsname(parentFd);
	int childFd = open(childName, O_RDWR | O_NOCTTY);
	if (unlikely(childFd == -1)) {
		result.error = errno;
		goto err_parent;
	}

	// Give the TTY a reasonable "default" size.
	uint32_t sizeResult = mosaic_test_resize_internal(parentFd, 80, 24, 0, 0);
	if (unlikely(sizeResult)) {
		result.error = sizeResult;
		goto err_child;
	}

	int interruptPipe[2];
	if (unlikely(pipe(interruptPipe) != 0)) {
		result.error = errno;
		goto err_child;
	}

	// Any non-TTY FD will do.
	int stdin = stdinIsTty ? childFd : interruptPipe[0];
	int stdout = stdoutIsTty ? childFd : interruptPipe[0];
	int stderr = stderrIsTty ? childFd : interruptPipe[0];

	MosaicTtyInitResult ttyInitResult = mosaic_tty_init_with_fd(childFd);
	if (unlikely(!ttyInitResult.tty)) {
		result.error = ttyInitResult.error;
		result.already_bound = ttyInitResult.already_bound;
		goto err_interrupt_pipe;
	}

	MosaicStreamsInitResult streamsInitResult = mosaic_streams_init_internal(stdin, stdout, stderr);
	if (unlikely(!streamsInitResult.streams)) {
		result.error = streamsInitResult.error;
		goto err_interrupt_pipe;
	}

	testTty->streams = streamsInitResult.streams;
	testTty->tty = ttyInitResult.tty;
	testTty->parent_fd = parentFd;
	testTty->parent_fd_interrupt_reader = interruptPipe[0];
	testTty->parent_fd_interrupt_writer = interruptPipe[1];

	result.testTty = testTty;

	ret:
	return result;

	err_interrupt_pipe:
	close(interruptPipe[0]);
	close(interruptPipe[1]);

	err_child:
	close(childFd);

	err_parent:
	close(parentFd);

	err_free:
	free(testTty);
	goto ret;
}

MosaicTty *mosaic_test_get_tty(MosaicTestTty *testTty) {
	return testTty->tty;
}

MosaicStreams *mosaic_test_get_streams(MosaicTestTty *testTty) {
	return testTty->streams;
}

MosaicIoResult mosaic_test_write(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_tty_write_internal(testTty->parent_fd, buffer, count);
}

MosaicIoResult mosaic_test_read(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_tty_read_internal(testTty->parent_fd, testTty->parent_fd_interrupt_reader, buffer, count, NULL);
}

MosaicIoResult mosaic_test_read_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return mosaic_tty_read_internal(testTty->parent_fd, testTty->parent_fd_interrupt_reader, buffer, count, &timeout);
}

uint32_t mosaic_test_interrupt_read(MosaicTestTty *testTty) {
	uint8_t space = ' ';
	MosaicIoResult result = mosaic_tty_write_internal(testTty->parent_fd_interrupt_writer, &space, 1);
	return result.error;
}

uint32_t mosaic_test_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height) {
	uint32_t sizeResult = mosaic_test_resize_internal(testTty->parent_fd, columns, rows, width, height);
	if (unlikely(sizeResult)) {
		return sizeResult;
	}

	// TODO Why can't I reference SIGWINCH here but I can in mosaic-tty-posix.c?
	if (unlikely(raise(28))) {
		return errno;
	}

	return 0;
}

uint32_t mosaic_test_send_focus_event(MosaicTestTty *testTty UNUSED, bool focused UNUSED) {
	// Focus events are delivered through VT sequences.
	return 0;
}

uint32_t mosaic_test_send_key_event(MosaicTestTty *testTty UNUSED) {
	// Key events are delivered through VT sequences.
	return 0;
}

uint32_t mosaic_test_send_mouse_event(MosaicTestTty *testTty UNUSED) {
	// Mouse events are delivered through VT sequences.
	return 0;
}

uint32_t mosaic_test_free(MosaicTestTty *testTty) {
	uint32_t result = 0;

	if (unlikely(close(testTty->parent_fd) != 0)) {
		result = errno;
	}
	if (unlikely(close(testTty->parent_fd_interrupt_reader) != 0 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(testTty->parent_fd_interrupt_writer) != 0 && result == 0)) {
		result = errno;
	}

	free(testTty);

	return result;
}

#endif
