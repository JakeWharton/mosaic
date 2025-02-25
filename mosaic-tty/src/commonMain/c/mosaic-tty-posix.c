#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-tty-posix.h"

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <termios.h>
#include <time.h>
#include <unistd.h>

MosaicTtyInitResult tty_initWithFds(
	int stdinReadFd,
	int stdoutWriteFd,
	int stderrWriteFd,
	MosaicTtyCallback *callback
) {
	MosaicTtyInitResult result = {};

	MosaicTtyImpl *tty = calloc(1, sizeof(MosaicTtyImpl));
	if (unlikely(tty == NULL)) {
		// result.tty is set to 0 which will trigger OOM.
		goto ret;
	}

	int interrupt_pipe[2];
	if (unlikely(pipe(interrupt_pipe)) != 0) {
		result.error = errno;
		goto err;
	}

	tty->stdin_read_fd = stdinReadFd;
	tty->stdout_write_fd = stdoutWriteFd;
	tty->stderr_write_fd = stderrWriteFd;
	tty->interrupt_read_fd = interrupt_pipe[0];
	tty->interrupt_write_fd = interrupt_pipe[1];
	tty->callback = callback;

	result.tty = tty;

	ret:
	return result;

	err:
	free(tty);
	goto ret;
}

MosaicTtyInitResult tty_init(MosaicTtyCallback *callback) {
	return tty_initWithFds(STDIN_FILENO, STDOUT_FILENO, STDERR_FILENO, callback);
}

MosaicTtyIoResult tty_readInputInternal(
	MosaicTty *tty,
	char *buffer,
	int count,
	struct timeval *timeout
) {
	MosaicTtyIoResult result = {};

	int stdinReadFd = tty->stdin_read_fd;
	int interruptReadFd = tty->interrupt_read_fd;

	fd_set fds;
	FD_ZERO(&fds);
	FD_SET(stdinReadFd, &fds);
	FD_SET(interruptReadFd, &fds);

	int nfds = 1 + ((stdinReadFd > interruptReadFd) ? stdinReadFd : interruptReadFd);
	if (likely(select(nfds, &fds, NULL, NULL, timeout) >= 0)) {
		if (likely(FD_ISSET(stdinReadFd, &fds) != 0)) {
			int c = read(stdinReadFd, buffer, count);
			if (likely(c > 0)) {
				result.count = c;
			} else if (c == 0) {
				result.count = -1; // EOF
			} else {
				goto err;
			}
		} else if (unlikely(FD_ISSET(interruptReadFd, &fds) != 0)) {
			// Consume the single notification byte to clear the ready state for the next call.
			int c = read(interruptReadFd, buffer, 1);
			if (unlikely(c < 0)) {
				goto err;
			}
		}
		// Otherwise if the interrupt pipe was selected or we timed out, return a count of 0.
	} else {
		goto err;
	}

	ret:
	return result;

	err:
	result.error = errno;
	goto ret;
}

MosaicTtyIoResult tty_readInput(MosaicTty *tty, char *buffer, int count) {
	return tty_readInputInternal(tty, buffer, count, NULL);
}

MosaicTtyIoResult tty_readInputWithTimeout(
	MosaicTty *tty,
	char *buffer,
	int count,
	int timeoutMillis
) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return tty_readInputInternal(tty, buffer, count, &timeout);
}

MosaicTtyIoResult tty_writeInternal(int writeFd, char *buffer, int count) {
	MosaicTtyIoResult result = {};

	int written = write(writeFd, buffer, count);
	if (written != -1) {
		result.count = written;
	} else {
		result.error = errno;
	}

	return result;
}

uint32_t tty_interruptRead(MosaicTty *tty) {
	MosaicTtyIoResult result = tty_writeInternal(tty->interrupt_write_fd, " ", 1);
	return result.error;
}

MosaicTtyIoResult tty_writeOutput(MosaicTty *tty, char *buffer, int count) {
	return tty_writeInternal(tty->stdout_write_fd, buffer, count);
}

MosaicTtyIoResult tty_writeError(MosaicTty *tty, char *buffer, int count) {
	return tty_writeInternal(tty->stderr_write_fd, buffer, count);
}

// TODO Make sure this is only written once. Probably just one instance per process at a time.
MosaicTty *globalTty = NULL;

void sigwinchHandler(int value UNUSED) {
	struct winsize size;
	if (ioctl(globalTty->stdin_read_fd, TIOCGWINSZ, &size) != -1) {
		MosaicTtyCallback *callback = globalTty->callback;
		callback->onResize(callback->opaque, size.ws_col, size.ws_row, size.ws_xpixel, size.ws_ypixel);
	}
	// TODO Send errno somewhere? Maybe once we get debug logs working.
}

uint32_t tty_enableRawMode(MosaicTty *tty) {
	uint32_t result = 0;

	if (unlikely(!tty->saved)) {
		goto ret; // Already enabled!
	}

	struct termios *saved = calloc(1, sizeof(struct termios));
	if (unlikely(saved == NULL)) {
		result = ENOMEM;
		goto ret;
	}

	if (unlikely(tcgetattr(tty->stdin_read_fd, saved) != 0)) {
		result = errno;
		goto err;
	}

	struct termios current = (*saved);

	// Flags as defined by "Raw mode" section of https://linux.die.net/man/3/termios
	current.c_iflag &= ~(BRKINT | ICRNL | IGNBRK | IGNCR | INLCR | ISTRIP | IXON | PARMRK);
	current.c_oflag &= ~(OPOST);
	// Setting ECHONL should be useless here, but it is what is documented for cfmakeraw.
	current.c_lflag &= ~(ECHO | ECHONL | ICANON | IEXTEN | ISIG);
	current.c_cflag &= ~(CSIZE | PARENB);
	current.c_cflag |= (CS8);

	current.c_cc[VMIN] = 1;
	current.c_cc[VTIME] = 0;

	if (unlikely(tcsetattr(tty->stdin_read_fd, TCSAFLUSH, &current) != 0)) {
		result = errno;
		// Try to restore the saved config.
		tcsetattr(tty->stdin_read_fd, TCSAFLUSH, saved);
		goto err;
	}

	tty->saved = saved;

	ret:
	return result;

	err:
	free(saved);
	goto ret;
}

uint32_t tty_enableWindowResizeEvents(MosaicTty *tty) {
	if (tty->sigwinch) {
		return 0; // Already installed.
	}

	struct sigaction action;
	action.sa_handler = sigwinchHandler;
	sigemptyset(&action.sa_mask);
	action.sa_flags = 0;

	globalTty = tty;
	if (likely(sigaction(SIGWINCH, &action, NULL) == 0)) {
		tty->sigwinch = true;
		return 0;
	}
	globalTty = NULL;
	return errno;
}

MosaicTtyTerminalSizeResult tty_currentTerminalSize(MosaicTty *tty) {
	MosaicTtyTerminalSizeResult result = {};

	struct winsize size;
	if (ioctl(tty->stdin_read_fd, TIOCGWINSZ, &size) != -1) {
		result.columns = size.ws_col;
		result.rows = size.ws_row;
		result.width = size.ws_xpixel;
		result.height = size.ws_ypixel;
	} else {
		result.error = errno;
	}

	return result;
}

uint32_t tty_free(MosaicTty *tty) {
	uint32_t result = 0;

	if (unlikely(close(tty->interrupt_read_fd) != 0)) {
		result = errno;
	}
	if (unlikely(close(tty->interrupt_write_fd) != 0 && result != 0)) {
		result = errno;
	}

	if (tty->sigwinch && signal(SIGWINCH, SIG_DFL) == SIG_ERR && result != 0) {
		result = errno;
	}

	if (tty->saved) {
		if (tcsetattr(tty->stdin_read_fd, TCSAFLUSH, tty->saved) && result != 0) {
			result = errno;
		}
		free(tty->saved);
	}

	free(tty);

	return result;
}

#endif
