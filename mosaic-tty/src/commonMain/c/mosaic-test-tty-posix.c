#if defined(__APPLE__) || defined(__linux__)

#define _XOPEN_SOURCE 600

#include "mosaic-streams-posix.h"
#include "mosaic-test-tty.h"
#include "mosaic-tty-posix.h"
#include "mosaic-utils-posix.h"

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

	int tty_parent_fd;
	int tty_parent_interrupt_reader_fd;
	int tty_parent_interrupt_writer_fd;

	int stdin_writer_fd;
	int stdin_reader_fd;

	int stdout_writer_fd;
	int stdout_reader_fd;
	int stdout_interrupt_reader_fd;
	int stdout_interrupt_writer_fd;

	int stderr_writer_fd;
	int stderr_reader_fd;
	int stderr_interrupt_reader_fd;
	int stderr_interrupt_writer_fd;
} MosaicTestTtyImpl;

static uint32_t mosaic_test_resize_internal(int parentFd, int columns, int rows, int width, int height) {
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
	int ttyParentFd = posix_openpt(O_RDWR | O_NOCTTY);
	if (unlikely(ttyParentFd == -1)) {
		result.error = errno;
		goto err_free;
	}
	if (unlikely(grantpt(ttyParentFd) || unlockpt(ttyParentFd))) {
		goto err_tty_parent;
	}

	char *ttyChildName = ptsname(ttyParentFd);
	int ttyChildFd = open(ttyChildName, O_RDWR | O_NOCTTY);
	if (unlikely(ttyChildFd == -1)) {
		result.error = errno;
		goto err_tty_parent;
	}

	// Give the TTY a reasonable "default" size.
	uint32_t sizeResult = mosaic_test_resize_internal(ttyParentFd, 80, 24, 0, 0);
	if (unlikely(sizeResult)) {
		result.error = sizeResult;
		goto err_tty_child;
	}

	int ttyParentInterruptPipe[2];
	if (unlikely(pipe(ttyParentInterruptPipe) != 0)) {
		result.error = errno;
		goto err_tty_child;
	}

	int stdinPipe[2];
	if (stdinIsTty) {
		stdinPipe[0] = ttyChildFd;
		stdinPipe[1] = ttyParentFd;
	} else {
		if (unlikely(pipe(stdinPipe) != 0)) {
			result.error = errno;
			goto err_tty_parent_interrupt_pipe;
		}
	}

	int stdoutPipe[2];
	int stdoutInterruptPipe[2];
	if (stdoutIsTty) {
		stdoutPipe[0] = ttyParentFd;
		stdoutPipe[1] = ttyChildFd;
		stdoutInterruptPipe[0] = ttyParentInterruptPipe[0];
		stdoutInterruptPipe[1] = ttyParentInterruptPipe[1];
	} else {
		if (unlikely(pipe(stdoutPipe) != 0)) {
			result.error = errno;
			goto err_stdin_pipe;
		}
		if (unlikely(pipe(stdoutInterruptPipe) != 0)) {
			result.error = errno;
			goto err_stdout_pipe;
		}
	}

	int stderrPipe[2];
	int stderrInterruptPipe[2];
	if (stderrIsTty) {
		stderrPipe[0] = ttyParentFd;
		stderrPipe[1] = ttyChildFd;
		stderrInterruptPipe[0] = ttyParentInterruptPipe[0];
		stderrInterruptPipe[1] = ttyParentInterruptPipe[1];
	} else {
		if (unlikely(pipe(stderrPipe) != 0)) {
			result.error = errno;
			goto err_stdout_interrupt_pipe;
		}
		if (unlikely(pipe(stderrInterruptPipe) != 0)) {
			result.error = errno;
			goto err_stderr_pipe;
		}
	}

	MosaicTtyInitResult ttyInitResult = mosaic_tty_init_with_fd(ttyChildFd);
	if (unlikely(!ttyInitResult.tty)) {
		result.error = ttyInitResult.error;
		result.already_bound = ttyInitResult.already_bound;
		goto err_stderr_interrupt_pipe;
	}

	MosaicStreamsInitResult streamsInitResult = mosaic_streams_init_internal(stdinPipe[0], stdoutPipe[1], stderrPipe[1]);
	if (unlikely(!streamsInitResult.streams)) {
		result.error = streamsInitResult.error;
		goto err_tty;
	}

	testTty->streams = streamsInitResult.streams;
	testTty->tty = ttyInitResult.tty;
	testTty->tty_parent_fd = ttyParentFd;
	testTty->tty_parent_interrupt_reader_fd = ttyParentInterruptPipe[0];
	testTty->tty_parent_interrupt_writer_fd = ttyParentInterruptPipe[1];
	testTty->stdin_reader_fd = stdinPipe[0];
	testTty->stdin_writer_fd = stdinPipe[1];
	testTty->stdout_reader_fd = stdoutPipe[0];
	testTty->stdout_writer_fd = stdoutPipe[1];
	testTty->stdout_interrupt_reader_fd = stdoutInterruptPipe[0];
	testTty->stdout_interrupt_writer_fd = stdoutInterruptPipe[1];
	testTty->stderr_reader_fd = stderrPipe[0];
	testTty->stderr_writer_fd = stderrPipe[1];
	testTty->stderr_interrupt_reader_fd = stderrInterruptPipe[0];
	testTty->stderr_interrupt_writer_fd = stderrInterruptPipe[1];

	result.testTty = testTty;

	ret:
	return result;

	err_tty:
	mosaic_tty_free(ttyInitResult.tty);

	err_stderr_interrupt_pipe:
	if (!stderrIsTty) {
		close(stderrInterruptPipe[0]);
		close(stderrInterruptPipe[1]);
	}

	err_stderr_pipe:
	if (!stderrIsTty) {
		close(stderrPipe[0]);
		close(stderrPipe[1]);
	}

	err_stdout_interrupt_pipe:
	if (!stdoutIsTty) {
		close(stdoutInterruptPipe[0]);
		close(stdoutInterruptPipe[1]);
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

	err_tty_parent_interrupt_pipe:
	close(ttyParentInterruptPipe[0]);
	close(ttyParentInterruptPipe[1]);

	err_tty_child:
	close(ttyChildFd);

	err_tty_parent:
	close(ttyParentFd);

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

MosaicIoResult mosaic_test_write_tty(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_utils_write(testTty->tty_parent_fd, buffer, count);
}

MosaicIoResult mosaic_test_read_tty(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_utils_read(testTty->tty_parent_fd, testTty->tty_parent_interrupt_reader_fd, buffer, count, NULL);
}

MosaicIoResult mosaic_test_read_tty_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return mosaic_utils_read(testTty->tty_parent_fd, testTty->tty_parent_interrupt_reader_fd, buffer, count, &timeout);
}

uint32_t mosaic_test_interrupt_tty_read(MosaicTestTty *testTty) {
	uint8_t space = ' ';
	MosaicIoResult result = mosaic_utils_write(testTty->tty_parent_interrupt_writer_fd, &space, 1);
	return result.error;
}

MosaicIoResult mosaic_test_write_input(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_utils_write(testTty->stdin_writer_fd, buffer, count);
}

MosaicIoResult mosaic_test_read_output(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_utils_read(testTty->stdout_reader_fd, testTty->stdout_interrupt_reader_fd, buffer, count, NULL);
}

MosaicIoResult mosaic_test_read_output_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return mosaic_utils_read(testTty->stdout_reader_fd, testTty->stdout_interrupt_reader_fd, buffer, count, &timeout);
}

uint32_t mosaic_test_interrupt_output_read(MosaicTestTty *testTty) {
	uint8_t space = ' ';
	MosaicIoResult result = mosaic_utils_write(testTty->stdout_interrupt_writer_fd, &space, 1);
	return result.error;
}

MosaicIoResult mosaic_test_read_error(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_utils_read(testTty->stderr_reader_fd, testTty->stderr_interrupt_reader_fd, buffer, count, NULL);
}

MosaicIoResult mosaic_test_read_error_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return mosaic_utils_read(testTty->stderr_reader_fd, testTty->stderr_interrupt_reader_fd, buffer, count, &timeout);
}

uint32_t mosaic_test_interrupt_error_read(MosaicTestTty *testTty) {
	uint8_t space = ' ';
	MosaicIoResult result = mosaic_utils_write(testTty->stderr_interrupt_writer_fd, &space, 1);
	return result.error;
}

uint32_t mosaic_test_resize(MosaicTestTty *testTty, int columns, int rows, int width, int height) {
	uint32_t sizeResult = mosaic_test_resize_internal(testTty->tty_parent_fd, columns, rows, width, height);
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

	bool stdinIsTty = testTty->stdin_writer_fd == testTty->tty_parent_fd;
	bool stdoutIsTty = testTty->stdout_reader_fd == testTty->tty_parent_fd;
	bool stderrIsTty = testTty->stderr_reader_fd == testTty->tty_parent_fd;

	if (unlikely(close(testTty->tty_parent_fd) != 0)) {
		result = errno;
	}
	if (unlikely(close(testTty->tty_parent_interrupt_reader_fd) != 0 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(testTty->tty_parent_interrupt_writer_fd) != 0 && result == 0)) {
		result = errno;
	}

	if (!stdinIsTty) {
		if (unlikely(close(testTty->stdin_reader_fd) != 0 && result == 0)) {
			result = errno;
		}
		if (unlikely(close(testTty->stdin_writer_fd) != 0 && result == 0)) {
			result = errno;
		}
	}

	if (!stdoutIsTty) {
		if (unlikely(close(testTty->stdout_reader_fd) != 0 && result == 0)) {
			result = errno;
		}
		if (unlikely(close(testTty->stdout_writer_fd) != 0 && result == 0)) {
			result = errno;
		}
		if (unlikely(close(testTty->stdout_interrupt_reader_fd) != 0 && result == 0)) {
			result = errno;
		}
		if (unlikely(close(testTty->stdout_interrupt_writer_fd) != 0 && result == 0)) {
			result = errno;
		}
	}

	if (!stderrIsTty) {
		if (unlikely(close(testTty->stderr_reader_fd) != 0 && result == 0)) {
			result = errno;
		}
		if (unlikely(close(testTty->stderr_writer_fd) != 0 && result == 0)) {
			result = errno;
		}
		if (unlikely(close(testTty->stderr_interrupt_reader_fd) != 0 && result == 0)) {
			result = errno;
		}
		if (unlikely(close(testTty->stderr_interrupt_writer_fd) != 0 && result == 0)) {
			result = errno;
		}
	}

	free(testTty);

	return result;
}

#endif
