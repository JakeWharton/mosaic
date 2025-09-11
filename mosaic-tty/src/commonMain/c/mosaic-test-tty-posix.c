#if defined(__APPLE__) || defined(__linux__)

#define _XOPEN_SOURCE 600

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
	MosaicTty *tty;
	int tty_fd_parent;
	int tty_interrupt_fd_reader;
	int tty_interrupt_fd_writer;
	bool stdin_is_tty;
	int stdin_fd_writer;
	bool stdout_is_tty;
	int stdout_fd_reader;
	bool stderr_is_tty;
	int stderr_fd_reader;
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

MosaicTestTtyInitResult testTty_init(bool stdinIsTty, bool stdoutIsTty, bool stderrIsTty) {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	// Note: the terms of art here appear to be "master" and "slave".
	// We use "parent" and "child" instead, respectively.
	int ttyParentFd = posix_openpt(O_RDWR | O_NOCTTY);
	if (unlikely(ttyParentFd == -1)) {
		result.error = errno;
		goto err_free;
	}
	if (unlikely(grantpt(ttyParentFd) || unlockpt(ttyParentFd))) {
		goto err_tty_parent;
	}

	char *childName = ptsname(ttyParentFd);
	int ttyChildFd = open(childName, O_RDWR | O_NOCTTY);
	if (unlikely(ttyChildFd == -1)) {
		result.error = errno;
		goto err_tty_parent;
	}

	// Give the TTY a reasonable "default" size.
	uint32_t sizeResult = testTty_resizeInternal(ttyParentFd, 80, 24, 0, 0);
	if (unlikely(sizeResult)) {
		result.error = sizeResult;
		goto err_tty_child;
	}

	int ttyInterruptPipe[2];
	if (unlikely(pipe(ttyInterruptPipe) != 0)) {
		result.error = errno;
		goto err_tty_child;
	}

	int stdinPipe[2];
	if (stdinIsTty) {
		stdinPipe[0] = ttyParentFd;
		stdinPipe[1] = ttyChildFd;
	} else {
		if (unlikely(pipe(stdinPipe) != 0)) {
			result.error = errno;
			goto err_tty_interrupt_pipe;
		}
	}

	int stdoutPipe[2];
	if (stdoutIsTty) {
		stdoutPipe[0] = ttyParentFd;
		stdoutPipe[1] = ttyChildFd;
	} else {
		if (unlikely(pipe(stdoutPipe) != 0)) {
			result.error = errno;
			goto err_stdin_pipe;
		}
	}

	int stderrPipe[2];
	if (stderrIsTty) {
		stderrPipe[0] = ttyParentFd;
		stderrPipe[1] = ttyChildFd;
	} else {
		if (unlikely(pipe(stderrPipe) != 0)) {
			result.error = errno;
			goto err_stdout_pipe;
		}
	}

	MosaicTtyInitResult ttyInitResult = tty_initWithFd(ttyChildFd, stdinPipe[0], stdoutPipe[1], stderrPipe[1]);
	if (unlikely(!ttyInitResult.tty)) {
		result.error = ttyInitResult.error;
		result.already_bound = ttyInitResult.already_bound;
		goto err_stderr_pipe;
	}

	testTty->tty = ttyInitResult.tty;
	testTty->tty_fd_parent = ttyParentFd;
	testTty->tty_interrupt_fd_reader = ttyInterruptPipe[0];
	testTty->tty_interrupt_fd_writer = ttyInterruptPipe[1];
	testTty->stdin_is_tty = stdinIsTty;
	testTty->stdin_fd_writer = stdinPipe[1];
	testTty->stdout_is_tty = stdoutIsTty;
	testTty->stdout_fd_reader = stdoutPipe[0];
	testTty->stderr_is_tty = stderrIsTty;
	testTty->stderr_fd_reader = stderrPipe[0];

	result.testTty = testTty;

	ret:
	return result;

	err_stderr_pipe:
	if (!stderrIsTty) {
		close(stderrPipe[0]);
		close(stderrPipe[1]);
	}

	err_stdout_pipe:
	if (!stdoutIsTty) {
		close(stdoutPipe[0]);
		close(stdoutPipe[1]);
	}

	err_stdin_pipe:
	if (!stdinIsTty) {
		close(stdinPipe[0]);
		close(stdinPipe[1]);
	}

	err_tty_interrupt_pipe:
	close(ttyInterruptPipe[0]);
	close(ttyInterruptPipe[1]);

	err_tty_child:
	close(ttyChildFd);

	err_tty_parent:
	close(ttyParentFd);

	err_free:
	free(testTty);
	goto ret;
}

MosaicTty *testTty_getTty(MosaicTestTty *testTty) {
	return testTty->tty;
}

MosaicTtyIoResult testTty_write(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return tty_writeInternal(testTty->tty_fd_parent, buffer, count);
}

MosaicTtyIoResult testTty_read(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return tty_readInternal(testTty->tty_fd_parent, testTty->tty_interrupt_fd_reader, buffer, count, NULL);
}

uint32_t testTty_interruptRead(MosaicTestTty *testTty) {
	uint8_t space = ' ';
	MosaicTtyIoResult result = tty_writeInternal(testTty->tty_interrupt_fd_writer, &space, 1);
	return result.error;
}

uint32_t testTty_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height) {
	uint32_t sizeResult = testTty_resizeInternal(testTty->tty_fd_parent, columns, rows, width, height);
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

	if (unlikely(close(testTty->tty_fd_parent) != 0)) {
		result = errno;
	}
	if (unlikely(close(testTty->tty_interrupt_fd_reader) != 0 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(testTty->tty_interrupt_fd_writer) != 0 && result == 0)) {
		result = errno;
	}
	if (!testTty->stdin_is_tty) {
		if (unlikely(close(testTty->stdin_fd_writer) != 0)) {
			result = errno;
		}
	}
	if (!testTty->stdout_is_tty) {
		if (unlikely(close(testTty->stdout_fd_reader) != 0)) {
			result = errno;
		}
	}
	if (!testTty->stderr_is_tty) {
		if (unlikely(close(testTty->stderr_fd_reader) != 0)) {
			result = errno;
		}
	}

	free(testTty);

	return result;
}

#endif
